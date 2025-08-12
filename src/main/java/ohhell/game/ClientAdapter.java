package ohhell.game;

import ohhell.network.AsyncClient;
import ohhell.network.AsyncMessageClient;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class ClientAdapter {
    private final static Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass().getPackage().getName());

    private final AsyncMessageClient networkClient;

    private final ClientProtocol client;

    private final GameLogger gameLogger;

    private enum ProcessState {
        START,
        WAIT_FOR_LOGIN,
        LOGGED_IN,
        LOGIN_ERROR,
        WAITING_ON_BID_RESPONSE,
        WAITING_ON_CARD_RESPONSE,
        WAITING_ON_BID_FROM_PLAYER,
        WAITING_ON_CARD_FROM_PLAYER,
        END
    }

    private ProcessState state;

    private int numPlayers;

    private int dealer;

    private int trickNum;

    private int numCards;

    private final List<Integer> tricksMade = new ArrayList<>();

    private final List<Integer> scores = new ArrayList<>();

    private final List<String> names = new ArrayList<>();

    private final List<Integer> handCards = new ArrayList<>();

    /**
     * Logger.
     */
    private final static Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass().getPackage().getName());

    public ClientAdapter(AsyncMessageClient networkClient,
                         ClientProtocol client,
                         GameLogger gameLogger) {
        this.networkClient = networkClient;
        this.gameLogger = gameLogger;
        state = ProcessState.START;
        this.client = new ClientValidator(client);
        this.networkClient.setMsgProcessor(this::processMsg);
    }

    private void logError(String msg) {
        logger.severe(msg);
        gameLogger.log(msg);
    }

    private void logInfo(String msg) {
        logger.fine(msg);
        gameLogger.log(msg);
    }

    private void logDebug(String msg) {
        logger.finer(msg);
    }

    public boolean process( long timeout) throws IOException {
        if (state != ProcessState.END) {
            if (state == ProcessState.START || state == ProcessState.LOGIN_ERROR) {
                state = ProcessState.WAIT_FOR_LOGIN;
                networkClient.sendMessage(String.format("%s %s", Commands.LOGIN, client.getName()));
            }
            try {
                networkClient.processRead(timeout);
            } catch( AsyncClient.NoConnectionException ex ) {
                state = ProcessState.END;
                throw ex;
            }
        }
        return state != ProcessState.END;
    }

    private void processCardPlayed( int card)  {
        try {
            if (state == ProcessState.WAITING_ON_CARD_FROM_PLAYER) {
                networkClient.sendMessage(String.format("%s %d", Commands.PLAY_CARD, card));
                state = ProcessState.WAITING_ON_CARD_RESPONSE;
            } else {
                logError("Unexpected card played callback");
            }
        } catch( IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void processBidMade( int bid) {
        try {
            if (state == ProcessState.WAITING_ON_BID_FROM_PLAYER) {
                networkClient.sendMessage(String.format("%s %d", Commands.BID, bid));
                state = ProcessState.WAITING_ON_BID_RESPONSE;
            } else {
                logError("Unexpected bid made callback");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void processMsg(String msg) {
        String [] tokens = msg.split(" ");
        logDebug("Processing tokens: " + Arrays.toString(tokens));
        String cmd = tokens[0];
        switch (state) {
            case START:
                break;
            case WAIT_FOR_LOGIN:
                if (cmd.equals(Commands.OK.toString())) {
                    logDebug("Logged in");
                    state = ProcessState.LOGGED_IN;
                    client.registered(-1);
                } else {
                    logError("Login error: " + msg);
                    state = ProcessState.LOGIN_ERROR;
                }
                break;
            case WAITING_ON_BID_RESPONSE:
                if (cmd.equals(Commands.OK.toString())) {
                    client.bidValidity(true, 0);
                    state = ProcessState.LOGGED_IN;
                } else if (cmd.equals(Commands.ERROR.toString())) {
                    client.bidValidity(false, 0);
                    state = ProcessState.LOGGED_IN;
                } else if (cmd.equals(Commands.BADBID.toString())) {
                    client.bidValidity(false, Integer.parseInt(tokens[1]));
                    state = ProcessState.LOGGED_IN;
                } else {
                    logError("Unexpected message: " + msg);
                }
                break;
            case WAITING_ON_CARD_RESPONSE:
                if (cmd.equals(Commands.OK.toString())) {
                    client.validation(null);
                } else {
                    client.validation(msg.substring(Commands.ERROR.toString().length() + 1));
                }
                state = ProcessState.LOGGED_IN;
                break;
            case LOGGED_IN:
                switch (Commands.valueOf(cmd)) {
                    case NEW_PLAYER:
                        client.playerRegistered(-1, tokens[1]);
                        logInfo(String.format("Player '%s' joined", tokens[1]));
                        break;
                    case START_GAME: {
                        numPlayers = Integer.parseInt(tokens[1]);
                        int playerId = -1;
                        String playerName = client.getName();
                        for (int i = 0; i < numPlayers; i++) {
                            String name = tokens[2 * i + 2];
                            names.add(name);
                            scores.add(Integer.parseInt(tokens[2 * i + 3]));
                            if (name.equals(playerName)) {
                                playerId = i;
                            }
                            tricksMade.add(0);
                        }
                        if (playerId < 0) {
                            logError("Unable to find player name: " + playerName);
                        } else {
                            String logMsg = " with players: " + Arrays.toString(names.toArray());
                            if (scores.stream().allMatch(x -> x == 0)) {
                                logInfo("Game started" + logMsg);
                                client.gameStarted(playerId, names);
                            } else {
                                logInfo("Game restarted" + logMsg);
                                client.gameRestarted(playerId, names, scores);
                            }
                        }
                        break;
                    }
                    case NEW_HAND:
                        handCards.clear();
                        Collections.fill(tricksMade, 0);
                        dealer = Integer.parseInt(tokens[2]);
                        break;
                    case DRAW:
                        handCards.add(Integer.parseInt(tokens[1]));
                        break;
                    case DEAL_OVER:
                    {
                        int trump = Integer.parseInt(tokens[1]);
                        trickNum = 0;
                        numCards = handCards.size();
                        logInfo(String.format("Hand started with %d cards trump %s dealer %s",
                                numCards,
                                (trump >= 0) ? Deck.cardToString(trump) : "None",
                                names.get(dealer)));
                        client.handStarted(handCards, dealer, trump);
                        client.trickStarted(0);
                    }
                    break;
                    case BID_ANNOUNCE: {
                        int bid = Integer.parseInt(tokens[2]);
                        int playerId = Integer.parseInt(tokens[1]);
                        logInfo(String.format("%s bid %d", names.get(playerId), bid));
                        client.bidMade(playerId, bid);
                        }
                        break;
                    case BID:
                        state = ProcessState.WAITING_ON_BID_FROM_PLAYER;
                        client.getBid(this::processBidMade);
                        break;
                    case GET_CARD:
                        state = ProcessState.WAITING_ON_CARD_FROM_PLAYER;
                        client.getCard(this::processCardPlayed);
                        break;
                    case CARD_PLAYED:
                        client.cardPlayed(Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]));
                        break;
                    case TRICK_WINNER:
                    {
                        int winner = Integer.parseInt(tokens[1]);
                        tricksMade.set(winner, tricksMade.get(winner) + 1);
                        client.trickEnded(trickNum, winner);
                        trickNum++;
                        if (trickNum < numCards) {
                            client.trickStarted(trickNum);
                        }
                        break;
                    }
                    case END_HAND:
                    {
                        List<Integer> deltas = new ArrayList<>();
                        for(int i=1; i < tokens.length; i++) {
                            deltas.add(Integer.parseInt(tokens[i]));
                        }
                        for(int i=0; i < scores.size(); i++) {
                            scores.set(i, scores.get(i) + deltas.get(i));
                        }
                        for(int i=1; i < deltas.size(); i++) {
                            String name = names.get(i);
                            if (deltas.get(i) > 0) {
                                logInfo(String.format("%s made %d points", name, deltas.get(i)));
                            } else if (deltas.get(i) == 0) {
                                logInfo(String.format("%s went over", name));
                            } else {
                                logInfo(String.format("%s went down %d", name, -deltas.get(i)));
                            }
                        }
                        client.handEnded(tricksMade, deltas);
                        break;
                    }
                    case GAME_OVER:
                    {
                        List<Integer> winners = new ArrayList<>();
                        for(int i=1; i < tokens.length; i++) {
                            winners.add(Integer.parseInt(tokens[i]));
                        }
                        // TODO: This is already logged to the log window by GameModel
                        List<String> winnerNames = winners.stream().map(names::get).toList();
                        logInfo(String.format("Game over, winner(s) %s", Arrays.toString(winnerNames.toArray())));
                        client.gameEnded(scores, winners);
                        break;
                    }
                }
                break;
                default:
                    logError(String.format("Unexpected message: %s", msg));
                    break;
            }
    }
}
