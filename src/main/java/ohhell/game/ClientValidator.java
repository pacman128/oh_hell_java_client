package ohhell.game;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.logging.Logger;

/**
 * Decorator class to validate client operations
 */
public class ClientValidator implements ClientProtocol{
    /** Logger */
    private final static Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass().getPackage().getName());

    /** Cards of client */
    private final CardList cardList = new CardList();

    /**  Card validator for client */
    private final PlayerValidator cardValidator = new PlayerValidator();

    /** Bid validator for client */
    private BidValidator bidValidator;

    /** Decorated client */
    private final ClientProtocol client;

    /** Leading card (-1) if not set */
    private int leadCard = -1;

    /**
     * Create a validator
     * @param client Decorated client
     */
    public ClientValidator( ClientProtocol client) {
        this.client = client;
    }

    /**
     * Validate a card to play
     * @param card Card to play
     * @param callback Callback for card value
     */
    private void validateCardPlayed( int card, InputCallback callback)
    {
        if (cardValidator.validateCard(card, leadCard)) {
            // If card is valid, use callback to return it
            logger.finer(String.format("Card %s validated", Deck.cardToString(card)));
            callback.returnValue(card);
        } else {
            // If card is invalid, tell client of error and restart get card process
            client.error("Invalid card: " + Deck.cardToString(card));
            getCard(callback);
        }
    }

    /**
     * Validate bid to make
     * @param bid Bid to make
     * @param callback Callback for bid value
     */
    private void validateBidMade( int bid, InputCallback callback)
    {
        if (bidValidator.validateBid(bid)) {
            logger.finer(String.format("Bid %d validated", bid));
            // If bid is valid, use callback to return bid
            callback.returnValue(bid);
        } else {
            // If bid is invalid, tell client and restart get bid process
            client.error("Invalid bid: " + bid);
            getBid(callback);
        }
    }

    @Override
    public String getName() {
        return client.getName();
    }

    @Override
    public void getCard( InputCallback callback) {
        // Inject local check of card to play
        client.getCard( card -> { validateCardPlayed(card, callback);});
    }

    @Override
    public void getBid( InputCallback callback) {
        // Inject local check of bid made
        client.getBid( bid -> { validateBidMade(bid, callback);});
    }

    @Override
    public void validation(String errorMsg) {
        if (errorMsg != null) {
            // This should never happen since the client validates play
            logger.severe("Server rejected card play!");
        }
        client.validation(errorMsg);
    }

    @Override
    public void registered(int id) {
        client.registered(id);
    }

    @Override
    public void playerRegistered(int id, String name) {
        client.playerRegistered(id, name);
    }

    @Override
    public void gameStarted(int playerId, List<String> playerNames) {
        // Create bid validator now that number of players known
        bidValidator = new BidValidator(playerNames.size());
        client.gameStarted(playerId, playerNames);
    }

    @Override
    public void gameRestarted(int playerId, List<String> playerNames, List<Integer> scores) {
        // Create bid validator now that number of players known
        bidValidator = new BidValidator(playerNames.size());
        client.gameRestarted(playerId, playerNames, scores);
    }

    @Override
    public void handStarted(List<Integer> cards, int dealer, int trump) {
        // Save cards dealt to client
        cardList.clear();
        cardList.addCards(cards);
        // Give cards to card validator
        cardValidator.setCards(cardList);
        // Tell bid validator about new hand
        bidValidator.newHand(cards.size());
        client.handStarted(cards, dealer, trump);
    }

    @Override
    public void bidMade(int playerId, int bid) {
        // Tell bid validator about bid
        bidValidator.addBid(bid);
        client.bidMade(playerId, bid);
    }

    @Override
    public void bidValidity(boolean valid, int bid) {
        if (! valid) {
            // This should never happen since bid is validated by client
            logger.severe(String.format("Bid %d rejected by server!", bid));
        }
        client.bidValidity(valid, bid);
    }

    @Override
    public void biddingDone(List<Integer> bids) {
        client.biddingDone(bids);
    }

    @Override
    public void trickStarted(int trickNum) {
        client.trickStarted(trickNum);
        // Reset leadCard value
        leadCard = -1;
    }

    @Override
    public void cardPlayed(int playerId, int card) {
        // Set the leadCard value if this is first card
        if (leadCard < 0) {
            leadCard = card;
        }
        client.cardPlayed(playerId, card);
    }

    @Override
    public void trickEnded(int trickNum, int winningPlayer) {
        client.trickEnded(trickNum, winningPlayer);
    }

    @Override
    public void handEnded(List<Integer> tricksMade, List<Integer> scoreDeltas) {
        client.handEnded(tricksMade, scoreDeltas);
    }

    @Override
    public void gameEnded(List<Integer> scores, List<Integer> winningPlayers) {
        client.gameEnded(scores, winningPlayers);
    }

    @Override
    public void error(String msg) {
        client.error(msg);
    }
}
