package ohhell.game;

import java.util.ArrayList;
import java.util.List;

public class ClientValidator implements ClientProtocol{

    private final CardList cardList = new CardList();

    private final List<String> playerNames = new ArrayList<>();

    private final PlayerValidator cardValidator = new PlayerValidator();

    private BidValidator bidValidator;

    private final ClientProtocol client;

    private int leadCard = -1;

    public ClientValidator( ClientProtocol client) {
        this.client = client;
    }

    private void validateCardPlayed( int card, InputCallback callback)
    {
        if (cardValidator.validateCard(card, leadCard)) {
            //TODO: Add logging
            callback.returnValue(card);
        } else {
            client.error("Invalid card: " + Deck.cardToString(card));
            getCard(callback);
        }
    }

    private void validateBidMade( int bid, InputCallback callback)
    {
        if (bidValidator.validateBid(bid)) {
            callback.returnValue(bid);
        } else {
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
        client.getCard( card -> { validateCardPlayed(card, callback);});
    }

    @Override
    public void getBid( InputCallback callback) {
        client.getBid( bid -> { validateBidMade(bid, callback);});
    }

    @Override
    public void validation(String errorMsg) {
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
        bidValidator = new BidValidator(playerNames.size());
        this.playerNames.addAll(playerNames);
        client.gameStarted(playerId, playerNames);
    }

    @Override
    public void gameRestarted(int playerId, List<String> playerNames, List<Integer> scores) {
        bidValidator = new BidValidator(playerNames.size());
        this.playerNames.addAll(playerNames);
        client.gameRestarted(playerId, playerNames, scores);
    }

    @Override
    public void handStarted(List<Integer> cards, int dealer, int trump) {
        cardList.clear();
        cardList.addCards(cards);
        cardValidator.setCards(cardList);
        bidValidator.newHand(cards.size());
        leadCard = -1;
        client.handStarted(cards, dealer, trump);
    }

    @Override
    public void bidMade(int playerId, int bid) {
        bidValidator.addBid(bid);
        client.bidMade(playerId, bid);
    }

    @Override
    public void bidValidity(boolean valid, int bid) {
        client.bidValidity(valid, bid);
    }

    @Override
    public void biddingDone(List<Integer> bids) {
        client.biddingDone(bids);
    }

    @Override
    public void trickStarted(int trickNum) {
        client.trickStarted(trickNum);
    }

    @Override
    public void cardPlayed(int playerId, int card) {
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
