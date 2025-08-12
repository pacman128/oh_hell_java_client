package ohhell.game;

/**
 * Local validator for user bids
 */
public final class BidValidator {

    /** Number of players in game */
    private final int numPlayers;

    /** Total bid in hand so far */
    private int bidTotal;

    /** Number of bids made in hand so far */
    private int numBids;

    /** Number of cards in hand */
    private int numCards;

    /**
     * Create new validator
     * @param numPlayers Number of players in game
     */
    public BidValidator(int numPlayers) {
        this.numPlayers = numPlayers;
    }

    /**
     * Reset validator for new hand
     * @param numCards Number of cards in hand
     */
    public void newHand(int numCards) {
        bidTotal = 0;
        numBids = 0;
        this.numCards = numCards;
    }

    /**
     * Add bid made by any player
     * @param bid Bid value
     */
    public void addBid(int bid) {
        bidTotal += bid;
        numBids++;
    }

    /**
     * Validate the bid of the user
     * @param bid Bid of user
     * @return true if valid, else false
     */
    public boolean validateBid(int bid) {
        // Is bid even in the correct range?
        if (bid < 0 || bid > numCards) {
            return false;
        }
        // If last bid, is bid allowed?
        if (numBids == numPlayers - 1) {
            return bidTotal + bid != numCards;
        }
        return true;
    }

}
