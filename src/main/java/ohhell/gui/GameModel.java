package ohhell.gui;

import ohhell.game.*;

import javax.swing.table.DefaultTableModel;
import java.lang.invoke.MethodHandles;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Model for game.
 * This class maintains the state of the game UI.
 */
public class GameModel implements ClientProtocol  {
    /** logger */
    private final static Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass().getPackage().getName());

    private final static int TRICKS_COLUMN = 1;
    private final static int BID_COLUMN = 2;
    private final static int SCORE_COLUMN = 3;

    /** Game state */
    public enum State {
        DISCONNECTED,
        WAITING_FOR_START,
        WAITING_FOR_CARD_RESPONSE,
        WAITING_FOR_BID_RESPONSE,
        PLAYING,
        GAME_OVER
    };

    /** Table model for status panel */
    private final DefaultTableModel statusModel;

    /** Game settings */
    private final Settings settings;

    /** Game logger */
    private GameLogger gameLogger = new GameLogger() {
        @Override
        public void log(String msg) {
            System.out.println(msg);
        }
    };

    /** Model state */
    private State state = State.DISCONNECTED;

    /** Player ID */
    private int playerId = -1;

    /** Number of players */
    private int numPlayers = -1;

    /** Current trump card */
    private int trumpCard = -1;

    /** Current number of cards in hand */
    private int numCardsInHand = -1;

    /** ID of current dealer */
    private int dealer = -1;

    /** Value of current card being validated */
    private int cardBeingValidated = -1;

    /** Bid validator */
    private BidValidator bidValidator;

    /** Callback for returning user bid */
    private InputCallback bidCallback;

    /** Validator for user card selections */
    private final PlayerValidator cardValidator = new PlayerValidator();

    /** Callback for returning user card play */
    private InputCallback playCallback;

    /** Lead card for trick */
    private int leadCard = -1;

    /** Names of players */
    private final List<String> names = new ArrayList<>();

    /** Scores of players */
    private final List<Integer> scores = new ArrayList<>();

    /** Number of tricks taken by each player */
    private final List<Integer> tricks = new ArrayList<>();

    /** Number of cards each player currently has */
    private final List<Integer> numCardsForPlayer = new ArrayList<>();

    /** Played card for each player for current trick (-1 for none) */
    private final List<Integer> playedCardForPlayer = new ArrayList<>();

    /** Cards held by user */
    private final List<Integer> cards = new ArrayList<>();

    /** Card selected to play by user */
    private int selectedCard = -1;

    /** Card played by user for current trick */
    private int playedCard = -1;

    /** Model listeners */
    private final List<Listener> listeners = new ArrayList<>();

    /**
     * Interface for model change listeners
     */
    public interface Listener {
        /**
         * Notify listeners of game settings change
         * @param rec New settings
         */
        void settingsChanged(Settings.SettingsValues rec);

        /**
         * Notify listeners of game state change
         */
        void gameStateChanged();

        /**
         * Notify listeners of new player
         * @param id ID of player
         * @param name Name of player
         */
        void newPlayer(int id, String name);
    }

    /**
     * Interface for logging messages
     */
    @FunctionalInterface
    public interface GameLogger {
        /**
         * Log message
         * @param msg Message to log
         */
        void log(String msg);
    }

    /**
     * Adapter for Listeners
     */
    public static class ListenerAdapter implements Listener {

        @Override
        public void settingsChanged(Settings.SettingsValues rec) { }

        @Override
        public void gameStateChanged() { }

        @Override
        public void newPlayer(int id, String name) { }
    }

    /**
     * Create a new model
     */
    public GameModel() {
        final String settingsFile = Paths.get(System.getProperty("user.home"), "oh_hell.settings").toString();
        logger.info(String.format("Settings file: %s", settingsFile));
        settings = new Settings(settingsFile, this::settingsChanged);
        String[] columnNames = {"Name", "Tricks", "Bid", "Score"};
        statusModel = new DefaultTableModel(columnNames, 0);
    }

    /**
     * Set the logger to use
     * @param gameLogger Logger to use
     */
    public void setGameLogger( GameLogger gameLogger) {
        this.gameLogger = gameLogger;
    }

    /**
     * Get the table model for status panel
     * @return TableModel for status panel
     */
    public DefaultTableModel getStatusTableModel() {
        return statusModel;
    }

    /**
     * Add a listener for model events
     * @param listener Listener to add
     */
    public void addListener( Listener listener) {
        listeners.add(listener);
    }

    /**
     * Get model state
     * @return State of model
     */
    public State getState() {
        return state;
    }

    /**
     * Set the state of model
     * @param state State of model
     */
    public void setState( State state) {
        this.state = state;
    }

    /**
     * Get trump card for hand
     * @return Trump card (-1 for no trump)
     */
    public int getTrump() {
        return trumpCard;
    }

    /**
     * Get dealer for hand
     * @return ID of dealer for hand
     */
    public int getDealer() {
        return dealer;
    }

    /**
     * Get number of cards in hand
     * @return Number of cards in hand
     */
    public int getNumCardsInHand() {
        return numCardsInHand;
    }

    /**
     * Get selected card
     * @return Selected card (-1 for none)
     */
    public int getSelectedCard() {
        return selectedCard;
    }

    /**
     * Set selected card
     * @param card Selected card
     */
    public void setSelectedCard( int card ) {
        selectedCard = card;
        notifyListeners();
    }

    /**
     * Get played card
     * @return Played card
     */
    public int getPlayedCard() {
        return playedCard;
    }

    /**
     * Get cards for current hand
     * @return Cards for hand
     */
    public List<Integer> getCards() {
        return cards;
    }

    /**
     * Get game settings
     * @return Game settings
     */
    public Settings getSettings() {
        return settings;
    }

    /**
     * Get player ID
     * @return player ID
     */
    public int getPlayerId() {
        return playerId;
    }

    /**
     * Get Number of players
     * @return Number of players
     */
    public int getNumPlayers() {
        return numPlayers;
    }

    /**
     * Get name of player
     * @param id ID of player
     * @return Name of player
     */
    public String getPlayerName(int id) {
        return names.get(id);
    }

    /**
     * Get number of cards for player
     * @param playerId ID of player
     * @return Number of cards for player
     */
    public int getNumCards(int playerId) {
        return numCardsForPlayer.get(playerId);
    }

    /**
     * Get played card for player
     * @param playerId ID of player
     * @return Played card for player (-1 if none)
     */
    public Integer getPlayedCard(int playerId) {
        return playedCardForPlayer.get(playerId);
    }

    /**
     * Play selected card.
     * Play the card selected by user.
     */
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

    /**
     * Is bid required from user?
     * @return true if bid needed, else false
     */
    public boolean bidRequired() {
        return state == GameModel.State.WAITING_FOR_BID_RESPONSE;
    }

    /**
     * Is card required from user?
     * @return true if card needed, else false
     */
    public boolean cardRequired() {
        return state == GameModel.State.WAITING_FOR_CARD_RESPONSE;
    }

    /**
     * Is user action required?
     * @return true if user action needed, else false
     */
    public boolean userActionRequired() {
        return cardRequired() || bidRequired();
    }

    /**
     * Notify listeners that model changed
     */
    private void notifyListeners() {
        for( var listener: listeners) {
            listener.gameStateChanged();
        }
    }

    /**
     * Validate and then send bid to server
     * @param bid User bid
     */
    public void bid(int bid) {
        if (bidValidator.validateBid(bid)) {
            state = State.PLAYING;
            bidCallback.returnValue(bid);
        } else {
            gameLogger.log("Invalid bid: " + bid);
        }
        notifyListeners();
    }

    /**
     * Validate and then send user card play to server
     * @param card User card
     */
    public void playCard(int card) {
        if (cardValidator.validateCard(card, leadCard)) {
            state = State.PLAYING;
            cardBeingValidated = card;
            playCallback.returnValue(card);
        } else {
            gameLogger.log("Invalid card: " + Deck.cardToString(card));
        }
        notifyListeners();
    }

    /**
     * Get player name to use
     * @return Player name
     */
    @Override
    public String getName() {
        return settings.getName();
    }

    /**
     * Get card from user
     * @param callback Callback to use to return back card value at later time
     */
    @Override
    public void getCard(final InputCallback callback) {
        state = State.WAITING_FOR_CARD_RESPONSE;
        playCallback = callback;
        if (cards.size() == 1) {
            selectedCard = cards.get(0);
        }
        notifyListeners();
    }

    /**
     * Get bid from user
     * @param callback Callback to use to return back bid value at later time
     */
    @Override
    public void getBid(final InputCallback callback) {
        state = State.WAITING_FOR_BID_RESPONSE;
        bidCallback = callback;
        notifyListeners();
    }

    /**
     * Process result of played card validation
     * @param errorMsg null if card is valid, else message describing error
     */
    @Override
    public void validation(String errorMsg) {
        if (errorMsg != null) {
            gameLogger.log(String.format("%s is not valid: %s", Deck.cardToString(cardBeingValidated), errorMsg));
        } else {
            state = State.PLAYING;
            playCard();
        }
        notifyListeners();
    }

    /**
     * Player registered with ID
     * @param id ID assigned to client
     */
    @Override
    public void registered(int id) {
        state = State.WAITING_FOR_START;
        playerId = id;
        gameLogger.log("Connected to server");
    }

    /**
     * Player registered
     * @param id ID of opponent
     * @param name Name of opponent
     */
    @Override
    public void playerRegistered(int id, String name) {
        // Nothing to do
    }

    /**
     * Game started
     * @param playerId ID assigned to client
     * @param playerNames Names of players in game (in ID order)
     */
    @Override
    public void gameStarted(int playerId, List<String> playerNames) {
        this.playerId = playerId;
        names.addAll(playerNames);
        numPlayers = playerNames.size();
        bidValidator = new BidValidator(numPlayers);
        for( int i=0; i < numPlayers; i++) {
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

    /**
     * Game restarted
     * @param playerId ID assigned to client
     * @param playerNames Names of players in game (in ID order)
     * @param scores Scores of players (in ID order)
     */
    @Override
    public void gameRestarted(int playerId, List<String> playerNames, List<Integer> scores) {
        this.playerId = playerId;
        names.addAll(playerNames);
        this.scores.addAll(scores);
        numPlayers = playerNames.size();
        bidValidator = new BidValidator(numPlayers);
        for( int i=0; i < numPlayers; i++) {
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

    /**
     * Hand started
     * @param cards Cards dealt to client
     * @param dealer ID of dealer
     * @param trump Trump card
     */
    @Override
    public void handStarted(List<Integer> cards, int dealer, int trump) {
        trumpCard = trump;
        this.dealer = dealer;
        this.numCardsInHand = cards.size();
        this.cards.clear();
        this.cards.addAll(cards);
        bidValidator.newHand(cards.size());
        cardValidator.setCards(cards);
        for(int i=0; i < numPlayers; i++) {
            tricks.set(i, 0);
            statusModel.setValueAt(null, i, BID_COLUMN);
            statusModel.setValueAt(0, i, TRICKS_COLUMN);
            numCardsForPlayer.set(i, cards.size());
        }
        notifyListeners();
    }

    /**
     * Bid made by player
     * @param playerId ID of player
     * @param bid Bid value
     */
    @Override
    public void bidMade(int playerId, int bid) {
        statusModel.setValueAt(bid, playerId, BID_COLUMN);
        bidValidator.addBid(bid);
        notifyListeners();
    }

    /**
     * Result of bid validation
     * @param valid Was bid valid?
     * @param bid Bid made by client
     */
    @Override
    public void bidValidity(boolean valid, int bid) {
        if (valid) {
            state = State.PLAYING;
        } else {
            gameLogger.log(String.format("Invalid bid: %d", bid));
        }
        notifyListeners();
    }

    /**
     * Notification that biding is over
     * @param bids Bids of the players (in ID order)
     */
    @Override
    public void biddingDone(List<Integer> bids) {

    }

    /**
     * Notification that trick started
     * @param trickNum Number of trick (0=first)
     */
    @Override
    public void trickStarted(int trickNum) {
        resetTrick();
    }

    /**
     * Notification that card was played
     * @param playerId ID of player
     * @param card Card played
     */
    @Override
    public void cardPlayed(int playerId, int card) {
        // Set the leadCard value if this is first card
        if (leadCard < 0) {
            leadCard = card;
        }
        numCardsForPlayer.set(playerId, numCardsForPlayer.get(playerId) - 1);
        playedCardForPlayer.set(playerId, card);
        gameLogger.log(String.format("\"%s\" played %s", names.get(playerId), Deck.cardToString(card)));
        notifyListeners();
    }

    /**
     * Notification that trick ended
     * @param trickNum Number of trick
     * @param winningPlayer ID of winning player
     */
    @Override
    public void trickEnded(int trickNum, int winningPlayer) {
        tricks.set(winningPlayer, tricks.get(winningPlayer) + 1);
        statusModel.setValueAt(tricks.get(winningPlayer), winningPlayer, TRICKS_COLUMN);
        notifyListeners();
    }

    /**
     * Notification that hand ended
     * @param tricksMade List of tricks made by each player (in ID order)
     * @param scoreDeltas List of score deltas for each player (in ID order)
     */
    @Override
    public void handEnded(List<Integer> tricksMade, List<Integer> scoreDeltas) {
        for(int i = 0; i < numPlayers; i++) {
            scores.set(i, scores.get(i) + scoreDeltas.get(i));
            statusModel.setValueAt(null, i, BID_COLUMN);
            statusModel.setValueAt(null, i, TRICKS_COLUMN);
            statusModel.setValueAt(scores.get(i), i, SCORE_COLUMN);
        }
        notifyListeners();
    }

    /**
     * Notification that game ended
     * @param scores List of player scores (in ID order)
     * @param winningPlayers List of winning player ids
     */
    @Override
    public void gameEnded(List<Integer> scores, List<Integer> winningPlayers) {
        state = State.GAME_OVER;
        playedCard = -1;
        for(int i=0; i < numPlayers; i++) {
            playedCardForPlayer.set(i, null);
        }
        notifyListeners();
    }

    /**
     * Notification of error
     * @param msg Error message
     */
    @Override
    public void error(String msg) {
        gameLogger.log(String.format("Error: %s", msg));
    }

    /**
     * Notify listeners that settings changed
     * @param settings New settings
     */
    private void settingsChanged(Settings.SettingsValues settings) {
        for( var listener: listeners) {
            listener.settingsChanged(settings);
        }
    }

    /**
     * Reset trick state
     */
    private void resetTrick() {
        playedCard = -1;
        for(int i=0; i < numPlayers; i++) {
            playedCardForPlayer.set(i, null);
        }
        // Reset leadCard value
        leadCard = -1;
        notifyListeners();
    }
}
