package ohhell.gui;

import ohhell.game.ClientProtocol;
import ohhell.game.Deck;
import ohhell.game.Settings;

import javax.swing.table.DefaultTableModel;
import java.lang.invoke.MethodHandles;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class GameModel implements ClientProtocol  {
    private final static Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass().getPackage().getName());

    private final static int TRICKS_COLUMN = 1;
    private final static int BID_COLUMN = 2;
    private final static int SCORE_COLUMN = 3;

    public enum State {
        DISCONNECTED,
        WAITING_FOR_START,
        WAITING_FOR_CARD_RESPONSE,
        WAITING_FOR_BID_RESPONSE,
        PLAYING,
        GAME_OVER
    };

    private final DefaultTableModel statusModel;

    private final Settings settings;

    private UserInput userInput;

    private GameLogger gameLogger = new GameLogger() {
        @Override
        public void log(String msg) {
            System.out.println(msg);
        }
    };

    private State state = State.DISCONNECTED;

    private int playerId = -1;

    private int numPlayers = -1;

    private int trumpCard = -1;

    private int dealer = -1;

    private int cardBeingValidiated = -1;

    private final List<String> names = new ArrayList<>();

    private final List<Integer> scores = new ArrayList<>();

    private final List<Integer> bids = new ArrayList<>();

    private final List<Integer> tricks = new ArrayList<>();

    private final List<Integer> numCardsForPlayer = new ArrayList<>();

    private final List<Integer> playedCardForPlayer = new ArrayList<>();

    private final List<Integer> cards = new ArrayList<>();

    private int selectedCard = -1;
    private int playedCard = -1;

    private final List<Listener> listeners = new ArrayList<>();

    public interface Listener {
        void settingsChanged(Settings.SettingsRec rec);

        void gameStateChanged();

        void newPlayer(int id, String name);
    }

    @FunctionalInterface
    public interface GameLogger {
        void log(String msg);
    }

    public static class ListenerAdapter implements Listener {

        @Override
        public void settingsChanged(Settings.SettingsRec rec) { }

        @Override
        public void gameStateChanged() { }

        @Override
        public void newPlayer(int id, String name) { }
    }

    public interface UserInput {
        void getCard(InputCallback callback);

        void getBid(InputCallback callback);
    }

    public GameModel() {
        final String settingsFile = Paths.get(System.getProperty("user.home"), "oh_hell.settings").toString();
        logger.info(String.format("Settings file: %s", settingsFile));
        settings = new Settings( settingsFile, this::settingsChanged);
        String [] columnNames = { "Name", "Tricks", "Bid", "Score"};
        statusModel = new DefaultTableModel(columnNames, 0);
    }

    public void setUserInput( UserInput userInput) {
        this.userInput = userInput;
    }

    public void setGameLogger( GameLogger gameLogger) {
        this.gameLogger = gameLogger;
    }

    public DefaultTableModel getStatusTableModel() {
        return statusModel;
    }

    public void addListener( Listener listener) {
        listeners.add(listener);
    }

    public State getState() {
        return state;
    }

    public void setState( State state) {
        this.state = state;
    }

    public int getTrump() {
        return trumpCard;
    }

    public int getDealer() {
        return dealer;
    }

    public int getSelectedCard() {
        return selectedCard;
    }

    public void setSelectedCard( int card ) {
        selectedCard = card;
        notifyListeners();
    }

    public int getPlayedCard() {
        return playedCard;
    }

    public List<Integer> getCards() {
        return cards;
    }

    public Settings getSettings() {
        return settings;
    }

    public int getPlayerId() {
        return playerId;
    }

    public int getNumPlayers() {
        return numPlayers;
    }

    public String getPlayerName(int id) {
        return names.get(id);
    }

    public int getNumCards(int playerId) {
        return numCardsForPlayer.get(playerId);
    }

    public Integer getPlayedCard(int playerId) {
        return playedCardForPlayer.get(playerId);
    }

    public void playCard() {
        playedCard = selectedCard;
        selectedCard = -1;
        int index = cards.indexOf(playedCard);
        if (index >= 0) {
            cards.remove(index);
        } else {
            logger.severe("Card not found in list: " + Deck.cardToString(playedCard));
        }
        notifyListeners();
    }

    public boolean bidRequired() {
        return state == GameModel.State.WAITING_FOR_BID_RESPONSE;
    }

    public boolean cardRequired() {
        return state == GameModel.State.WAITING_FOR_CARD_RESPONSE;
    }

    public boolean userActionRequired() {
        return cardRequired() || bidRequired();
    }


    private void notifyListeners() {
        for( var listener: listeners) {
            listener.gameStateChanged();
        }
    }

    @Override
    public String getName() {
        return settings.getName();
    }

    @Override
    public void getCard(final InputCallback callback) {
        state = State.WAITING_FOR_CARD_RESPONSE;
        userInput.getCard( (card) -> {cardBeingValidiated = card; callback.returnValue(card); });
        if (cards.size() == 1) {
            selectedCard = cards.get(0);
        }
        notifyListeners();
    }

    @Override
    public void getBid(final InputCallback callback) {
        state = State.WAITING_FOR_BID_RESPONSE;
        userInput.getBid( callback );
        notifyListeners();
    }

    @Override
    public void validation(String errorMsg) {
        if (errorMsg != null) {
            gameLogger.log(String.format("%s is not valid: %s", Deck.cardToString(cardBeingValidiated), errorMsg));
        } else {
            state = State.PLAYING;
            playCard();
        }
        notifyListeners();
    }

    @Override
    public void registered(int id) {
        state = State.WAITING_FOR_START;
        playerId = id;
        gameLogger.log("Connected to server");
    }

    @Override
    public void playerRegistered(int id, String name) {
        // Nothing to do
    }

    @Override
    public void gameStarted(int playerId, List<String> playerNames) {
        this.playerId = playerId;
        names.addAll(playerNames);
        numPlayers = playerNames.size();
        for( int i=0; i < numPlayers; i++) {
            bids.add(-1);
            scores.add(0);
            tricks.add(0);
            playedCardForPlayer.add(null);
            numCardsForPlayer.add(0);
        }
        for (int i=0; i < numPlayers; i++) {
            var name = playerNames.get(i);
            statusModel.addRow(new Object[]{name, null, null, 0});
            if (i != playerId) {
                for (var listener : listeners) {
                    listener.newPlayer(i, name);
                }
            }
        }
        state = State.PLAYING;
        notifyListeners();
    }

    @Override
    public void gameRestarted(int playerId, List<String> playerNames, List<Integer> scores) {
        this.playerId = playerId;
        names.addAll(playerNames);
        this.scores.addAll(scores);
        numPlayers = playerNames.size();
        for( int i=0; i < numPlayers; i++) {
            bids.add(-1);
            tricks.add(0);
            playedCardForPlayer.add(null);
            numCardsForPlayer.add(0);
        }
        for(int i=0; i < numPlayers; i++) {
            var name = playerNames.get(i);
            statusModel.addRow(new Object[]{name, null, null, scores.get(i)});
            if (i != playerId) {
                for (var listener : listeners) {
                    listener.newPlayer(i, name);
                }
            }
        }
        state = State.PLAYING;
        notifyListeners();
    }

    @Override
    public void handStarted(List<Integer> cards, int dealer, int trump) {
        trumpCard = trump;
        this.dealer = dealer;
        this.cards.clear();
        this.cards.addAll(cards);
        for(int i=0; i < numPlayers; i++) {
            bids.set(i, -1);
            tricks.set(i, 0);
            statusModel.setValueAt(null, i, BID_COLUMN);
            statusModel.setValueAt(0, i, TRICKS_COLUMN);
            numCardsForPlayer.set(i, cards.size());
        }
        notifyListeners();
    }

    @Override
    public void bidMade(int playerId, int bid) {
        bids.set(playerId, bid);
        statusModel.setValueAt(bid, playerId, BID_COLUMN);
        notifyListeners();
    }

    @Override
    public void bidValidity(boolean valid, int bid) {
        state = State.PLAYING;
        if (! valid) {
            gameLogger.log(String.format("Invalid bid: %d", bid));
        }
    }

    @Override
    public void biddingDone(List<Integer> bids) {

    }

    @Override
    public void trickStarted(int trickNum) {
        playedCard = -1;
        for(int i=0; i < numPlayers; i++) {
            playedCardForPlayer.set(i, null);
        }
        notifyListeners();
    }

    @Override
    public void cardPlayed(int playerId, int card) {
        numCardsForPlayer.set(playerId, numCardsForPlayer.get(playerId) - 1);
        playedCardForPlayer.set(playerId, card);
        gameLogger.log(String.format("\"%s\" played %s", names.get(playerId), Deck.cardToString(card)));
        notifyListeners();
    }

    @Override
    public void trickEnded(int trickNum, int winningPlayer) {
        tricks.set(winningPlayer, tricks.get(winningPlayer) + 1);
        statusModel.setValueAt(tricks.get(winningPlayer), winningPlayer, TRICKS_COLUMN);
        notifyListeners();
    }

    @Override
    public void handEnded(List<Integer> tricksMade, List<Integer> scoreDeltas) {
        for(int i = 0; i < numPlayers; i++) {
            var line = new StringBuilder(String.format("\"%s\" ", names.get(i)));
            var scoreDelta = scoreDeltas.get(i);
            if (scoreDelta > 0) {
                line.append(String.format("made %d points", scoreDelta));
            } else if (scoreDelta < 0) {
                line.append(String.format("when down %d points", -scoreDelta));
            } else {
                line.append("went over");
            }
            gameLogger.log(line.toString());
            scores.set(i, scores.get(i) + scoreDeltas.get(i));
            statusModel.setValueAt(null, i, BID_COLUMN);
            statusModel.setValueAt(null, i, TRICKS_COLUMN);
            statusModel.setValueAt(scores.get(i), i, SCORE_COLUMN);
        }
        notifyListeners();
    }

    @Override
    public void gameEnded(List<Integer> scores, List<Integer> winningPlayers) {
        state = State.GAME_OVER;
        playedCard = -1;
        for(int i=0; i < numPlayers; i++) {
            playedCardForPlayer.set(i, null);
        }
        var line = new StringBuilder("Game over, winner");
        if (winningPlayers.size() > 1) {
            line.append("s are: ");
        } else {
            line.append(" is: ");
        }
        for( var winnerId: winningPlayers) {
            line.append(names.get(winnerId));
            line.append(" ");
        }
        gameLogger.log(line.toString());
        notifyListeners();
    }

    @Override
    public void error(String msg) {
        gameLogger.log(String.format("Error: %s", msg));
    }

    private void settingsChanged(Settings.SettingsRec settings) {
        for( var listener: listeners) {
            listener.settingsChanged(settings);
        }
    }

}
