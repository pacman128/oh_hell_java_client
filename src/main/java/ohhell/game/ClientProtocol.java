package ohhell.game;

import java.util.List;

/**
 * Interface for client operations
 */
public interface ClientProtocol {

    /**
     * Callback for get bid and card operations
     */
    @FunctionalInterface
    interface InputCallback {
        /**
         * Return back the input value
         * @param value Value to return
         */
        void returnValue(int value);
    }

    /**
     * Get name of client
     * @return Name of client
     */
    String getName();

    /**
     * Get card from client
     *
     * @param callback Callback to use to return back card value at later time
     */
    void getCard( InputCallback callback);

    /**
     * Get bid from client
     *
     * @param callback Callback to use to return back bid value at later time
     */
    void getBid( InputCallback callback);

    /**
     * Validation of client card play
     *
     * @param errorMsg null if card is valid, else message describing error
     */
    void validation( String errorMsg);

    /**
     * Client registration notification
     * @param id ID assigned to client
     */
    void registered(int id);

    /**
     * Opponent player registration notification
     * @param id ID of opponent
     * @param name Name of opponent
     */
    void playerRegistered(int id, String name);

    /**
     * Game start notification
     * @param playerId ID assigned to client
     * @param playerNames Names of players in game (in ID order)
     */
    void gameStarted(int playerId, List<String> playerNames);

    /**
     * Game restart notification
     * @param playerId ID assigned to client
     * @param playerNames Names of players in game (in ID order)
     * @param scores Scores of players (in ID order)
     */
    void gameRestarted(int playerId, List<String> playerNames, List<Integer> scores);

    /**
     * Notification of hand started
     * @param cards Cards dealt to client
     * @param dealer ID of dealer
     * @param trump Trump card
     */
    void handStarted(List<Integer> cards, int dealer, int trump);

    /**
     * Notification of bid made
     * @param playerId ID of player
     * @param bid Bid value
     */
    void bidMade(int playerId, int bid);

    /**
     * Notification of validity of bid made by client
     * @param valid Was bid valid?
     * @param bid Bid made by client
     */
    void bidValidity(boolean valid, int bid);

    /**
     * Notification of bidding complete for hand
     * @param bids Bids of the players (in ID order)
     */
    void biddingDone(List<Integer> bids);

    /**
     * Notification of trick started
     * @param trickNum Number of trick (0=first)
     */
    void trickStarted(int trickNum);

    /**
     * Notification of card played
     * @param playerId ID of player
     * @param card Card played
     */
    void cardPlayed(int playerId, int card);

    /**
     * Notification of trick finished
     * @param trickNum Number of trick
     * @param winningPlayer ID of winning player
     */
    void trickEnded(int trickNum, int winningPlayer);

    /**
     * Notification of hand ended
     * @param tricksMade List of tricks made by each player (in ID order)
     * @param scoreDeltas List of score deltas for each player (in ID order)
     */
    void handEnded(List<Integer> tricksMade, List<Integer> scoreDeltas);

    /**
     * Notification of game end
     * @param scores List of player scores (in ID order)
     * @param winningPlayers List of winning player ids
     */
    void gameEnded(List<Integer> scores, List<Integer> winningPlayers);

    /**
     * Notification of error
     * @param msg Error message
     */
    void error(String msg);
}
