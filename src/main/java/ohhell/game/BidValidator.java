package ohhell.game;

import java.util.ArrayList;

public final class BidValidator {
    private final int numPlayers;

    private int bidTotal;

    private int numBids;

    private int numCards;

    public BidValidator(int numPlayers) {
        this.numPlayers = numPlayers;
    }

    public void newHand(int numCards) {
        bidTotal = 0;
        numBids = 0;
        this.numCards = numCards;
    }

    public void addBid(int bid) {
        bidTotal += bid;
        numBids++;
    }

    public boolean validateBid(int bid) {
        if (bid < 0 || bid > numCards) {
            return false;
        }
        if (numBids == numPlayers - 1) {
            return bidTotal + bid != numCards;
        }
        return true;
    }

}
