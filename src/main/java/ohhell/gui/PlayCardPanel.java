package ohhell.gui;

import ohhell.game.Deck;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.function.Consumer;

//TODO: Remove this as it's not used

public class PlayCardPanel extends JPanel {

    private static class CardToPlayPanel extends JPanel {
        private int cardToPlay = -1;

        public CardToPlayPanel() {
            super();
        }

        public void setCardToPlay(int card) {
            cardToPlay = card;
        }

        public int getCardToPlay() {
            return cardToPlay;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (cardToPlay > 0) {
                g.drawImage(Deck.getCardImage(cardToPlay), 10, OtherPlayerPanel.baseCardTop, null);
            }

        }
    }

    final private CardToPlayPanel cardToPlayPanel = new CardToPlayPanel();

    final private JButton playButton = new JButton("Play");

    final private Consumer<Integer> playCallback;

    public PlayCardPanel(Consumer<Integer> playCallback) {
        super(new BorderLayout());

        this.playCallback = playCallback;
        var buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(playButton);
        add(buttonPanel, BorderLayout.NORTH);
        add(cardToPlayPanel, BorderLayout.CENTER);
        playButton.setEnabled(false);
        playButton.addActionListener(e -> { this.playCallback.accept(cardToPlayPanel.getCardToPlay()); });
    }

    public void setCardToPlay(int card) {
        playButton.setEnabled(card >= 0);
        cardToPlayPanel.setCardToPlay(card);
    }

    public static void main(String [] args) throws IOException {
        Deck.loadCardImages();

        SwingUtilities.invokeLater( () -> {
            JFrame frame = new JFrame("Demo");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            var panel = new PlayCardPanel( card -> {System.out.println("Played: " + Deck.cardToString(card)); });
            panel.setCardToPlay(11);
            frame.setContentPane(panel);
            frame.setSize(100, 250);
            frame.setVisible(true);

        });

    }

}
