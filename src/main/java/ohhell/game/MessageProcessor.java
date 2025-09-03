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

/**
 * Adapter class that processes server messages into
 * events for the ClientProtocol interface.
 */
public class MessageProcessor {
    /** I/O Message client*/
    private final AsyncMessageClient networkClient;

    /** ClientProtocol implementation to send events to*/
    private final ClientProtocol client;

    /** Game logger to log events to */
    private final GameLogger gameLogger;

    /** Process state values */
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

    /** Current state */
    private ProcessState state;

    /** Dealer for current hand */
    private int dealer;

    /** Number of current trick */
    private int trickNum;

    /** Number of cards in current hand */
    private int numCards;

    /** Tricks made for each player in current hand */
    private final List<Integer> tricksMade = new ArrayList<>();

    /** Current scores for each player */
    private final List<Integer> scores = new ArrayList<>();

    /** Names of the players */
    private final List<String> names = new ArrayList<>();

    /** Cards for user in current hand */
    private final List<Integer> handCards = new ArrayList<>();

    /** Logger. */
    private final static Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass().getPackage().getName());

    /**
     * Create new processor
     * @param networkClient Network client for I/O
     * @param client ClientProtocol to send events to
     * @param gameLogger Logger to use to log messages
     */
    public MessageProcessor(AsyncMessageClient networkClient,
                            ClientProtocol client,
                            GameLogger gameLogger) {
        this.networkClient = networkClient;
        this.gameLogger = gameLogger;
        state = ProcessState.START;
        this.client = client;
        this.networkClient.setMsgProcessor(this::processMsg);
    }

    /**
     * Log an error
     * @param msg Error message
     */
    private void logError(String msg) {
        logger.severe(msg);
        gameLogger.log(msg);
    }

    /**
     * Log informative message
     * @param msg Message
     */
    private void logInfo(String msg) {
        logger.fine(msg);
        gameLogger.log(msg);
    }

    /**
     * Log debug message
     * @param msg Message
     */
    private void logDebug(String msg) {
        logger.finer(msg);
    }

    /**
     * Process network input.
     * This method must be call periodically to process client messages. It will
     * automatically log in the server at start up.
     *
     * @param timeout Time to spend waiting for input in ms
     * @return is game still active
     * @throws IOException On I/O error
     */
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

    /**
     * Process card played from user.
     * This method must be called in the network thread.
     * @param card Card played
     */
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

    /**
     * Process bid made by user
     * @param bid Bid made
     */
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

    /**
     * Process a message from the client
     * @param msg Message received
     */
    private void processMsg(String msg) {
        String [] tokens = msg.split(" ");
        logDebug("Processing tokens: " + Arrays.toString(tokens));
        String cmd = tokens[0];
        switch (state) {
            case START:
                break;
                
            case WAIT_FOR_LOGIN:
                // Waiting for response from server for login
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
                // Waiting for a response from server to BID message
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
                // Waiting for response from server to playing of card
                if (cmd.equals(Commands.OK.toString())) {
                    client.validation(null);
                } else {
                    client.validation(msg.substring(Commands.ERROR.toString().length() + 1));
                }
                state = ProcessState.LOGGED_IN;
                break;
                
            case LOGGED_IN:
                // Not waiting for any specific server message
                processCmd(cmd, tokens);
                break;
            default:
                logError(String.format("Unexpected message: %s", msg));
                break;
            }
    }

    /**
     * Process message from server
     * @param cmd Server command
     * @param tokens Array of tokens of message
     */
    private void processCmd(String cmd, String[] tokens) {
        switch (Commands.valueOf(cmd)) {
            case NEW_PLAYER:
                client.playerRegistered(-1, tokens[1]);
                logInfo(String.format("Player '%s' joined", tokens[1]));
                break;

            case START_GAME: {
                int numPlayers = Integer.parseInt(tokens[1]);
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
                Collections.sort(handCards);
                logInfo(String.format("New hand with %d cards trump: %s dealer: \"%s\"",
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
                logInfo(String.format("\"%s\" bid %d", names.get(playerId), bid));
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
                logInfo(String.format("\"%s\" won trick", names.get(winner)));
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
                for(int i = 1; i < tokens.length; i++) {
                    deltas.add(Integer.parseInt(tokens[i]));
                }
                for(int i=0; i < scores.size(); i++) {
                    scores.set(i, scores.get(i) + deltas.get(i));
                }
                for(int i=0; i < deltas.size(); i++) {
                    String name = names.get(i);
                    if (deltas.get(i) > 0) {
                        logInfo(String.format("\"%s\" made %d points", name, deltas.get(i)));
                    } else if (deltas.get(i) == 0) {
                        logInfo(String.format("\"%s\" went over", name));
                    } else {
                        logInfo(String.format("\"%s\" went down %d", name, -deltas.get(i)));
                    }
                }
                client.handEnded(tricksMade, deltas);
                break;
            }

            case GAME_OVER:
            {
                List<Integer> winners = new ArrayList<>();
                for(int i = 1; i < tokens.length; i++) {
                    winners.add(Integer.parseInt(tokens[i]));
                }
                List<String> winnerNames = winners.stream().map(names::get).toList();
                logInfo(String.format("Game over, winner(s): %s", Arrays.toString(winnerNames.toArray())));
                client.gameEnded(scores, winners);
                break;
            }
        }
    }
}
