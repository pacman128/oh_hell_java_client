package ohhell.game;

import java.util.List;

public interface ClientProtocol {

    @FunctionalInterface
    interface InputCallback {
        void returnValue(int value);
    }

    String getName();

    void getCard( InputCallback callback);

    void getBid( InputCallback callback);

    void validation( String errorMsg);

    void registered(int id);

    void playerRegistered(int id, String name);

    void gameStarted(int playerId, List<String> playerNames);

    void gameRestarted(int playerId, List<String> playerNames, List<Integer> scores);

    void handStarted(List<Integer> cards, int dealer, int trump);

    void bidMade(int playerId, int bid);

    void bidValidity(boolean valid, int bid);

    void biddingDone(List<Integer> bids);

    void trickStarted(int trickNum);

    void cardPlayed(int playerId, int card);

    void trickEnded(int trickNum, int winningPlayer);

    void handEnded(List<Integer> tricksMade, List<Integer> scoreDeltas);

    void gameEnded(List<Integer> scores, List<Integer> winningPlayers);

    void error(String msg);
}
