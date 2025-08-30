package ohhell.gui;

import ohhell.game.ClientProtocol;
import ohhell.game.NetworkTask;

import javax.swing.*;
import java.util.List;

/**
 * Decorator class that modifies a ClientProtocol to delay the
 * trickEnd and trickStart method calls to give user a chance to see
 * all the cards of a trick before they are removed for the start of
 * the next trick.
 *
 * All the decorated ClientProtocol calls are made in the Swing event thread.
 * All the incoming ClientProtocol calls are assumed to be made from the NetworkTask
 * thread.
 */
public class GuiDecorator implements ClientProtocol {

    /**  Client to decorate */
    private final ClientProtocol client;

    /** Delay between tricks in ms */
    private final long trickDelay;

    /** Original callback to call in the network task's thread */
    private InputCallback bidCallback;

    /** Original callback to call in the network task's thread */
    private InputCallback cardCallback;

    /** Network task */
    private final NetworkTask networkTask;

    /**
     * Create a new decorator
     * @param client Client to decorator
     * @param networkTask NetworkTask to run callbacks in
     * @param trickDelay Delay between tricks in ms
     */
    public GuiDecorator(ClientProtocol client, NetworkTask networkTask, long trickDelay) {
        this.client = client;
        this.trickDelay = trickDelay;
        this.networkTask = networkTask;
    }

    /**
     * Process card callback in NetworkTask thread
     * @param card Card played
     */
    private void processCardCallback( int card) {
        networkTask.submit( () -> {
            cardCallback.returnValue(card);
            cardCallback = null;
            return null;
        });
    }

    /**
     * Process bid callback in NetworkTask thread
     * @param bid Bid made
     */
    private void processBidCallback( int bid) {
        networkTask.submit( () -> {
            bidCallback.returnValue(bid);
            bidCallback = null;
            return null;
        });
    }

    @Override
    public String getName() {
        return client.getName();
    }

    @Override
    public void getCard(InputCallback callback) {
        // Save callback to call in NetworkTask thread
        cardCallback = callback;
        // Redirect Swing code to call local callback
        SwingUtilities.invokeLater( () -> { client.getCard(this::processCardCallback); });
    }

    @Override
    public void getBid(InputCallback callback) {
        // Save callback to call in Network thread
        bidCallback = callback;
        // Redirect Swing code to call locall callback
        SwingUtilities.invokeLater( () -> { client.getBid(this::processBidCallback); });
    }

    @Override
    public void validation(String errorMsg) {
        SwingUtilities.invokeLater( () -> { client.validation(errorMsg); });
    }

    @Override
    public void registered(int id) {
        SwingUtilities.invokeLater( () -> { client.registered(id); });
    }

    @Override
    public void playerRegistered(int id, String name) {
        SwingUtilities.invokeLater(() -> { client.playerRegistered(id, name); });
    }

    @Override
    public void gameStarted(int playerId, List<String> playerNames) {
        SwingUtilities.invokeLater( () -> { client.gameStarted(playerId, playerNames); });
    }

    @Override
    public void gameRestarted(int playerId, List<String> playerNames, List<Integer> scores) {
        SwingUtilities.invokeLater( () -> { client.gameRestarted(playerId, playerNames, scores); });
    }

    @Override
    public void handStarted(List<Integer> cards, int dealer, int trump) {
        SwingUtilities.invokeLater( () -> { client.handStarted(cards, dealer, trump); });
    }

    @Override
    public void bidMade(int playerId, int bid) {
        SwingUtilities.invokeLater( () -> { client.bidMade(playerId, bid); });
    }

    @Override
    public void bidValidity(boolean valid, int bid) {
        SwingUtilities.invokeLater( () -> { client.bidValidity(valid, bid); });
    }

    @Override
    public void biddingDone(List<Integer> bids) {
        SwingUtilities.invokeLater( () -> { client.biddingDone(bids); });
    }

    @Override
    public void trickStarted(int trickNum) {
        SwingUtilities.invokeLater( () -> { client.trickStarted( trickNum ); });
    }

    @Override
    public void cardPlayed(int playerId, int card) {
        SwingUtilities.invokeLater( () -> { client.cardPlayed(playerId, card); });
    }

    @Override
    public void trickEnded(int trickNum, int winningPlayer) {
        SwingUtilities.invokeLater( () -> { client.trickEnded( trickNum, winningPlayer); });

        // Wait for delay for user to see end of trick before next trick starts
        try {
            Thread.sleep(trickDelay);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void handEnded(List<Integer> tricksMade, List<Integer> scoreDeltas) {
        SwingUtilities.invokeLater( () -> { client.handEnded( tricksMade, scoreDeltas); });
    }

    @Override
    public void gameEnded(List<Integer> scores, List<Integer> winningPlayers) {
        SwingUtilities.invokeLater( () -> { client.gameEnded(scores, winningPlayers); });
    }

    @Override
    public void error(String msg) {
        SwingUtilities.invokeLater( () -> { client.error(msg); });
    }
}
