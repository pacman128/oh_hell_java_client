package ohhell.gui;

import ohhell.game.Deck;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.List;

public class OtherPlayerPanel extends JPanel {

    final static int baseCardTop = 50;
    private final static int cardSpacing = 10;
    private final static int cardOffset = 10;

    private final int playerId;
    private final Font nameFont;
    private final GameModel model;

    public OtherPlayerPanel(GameModel model, int playerId, Font font) {
        super();
        this.model = model;
        this.playerId = playerId;
        nameFont = font;
        setPreferredSize(new Dimension(250, 200));
        setBorder(BorderFactory.createLineBorder(Color.black));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        var cardImage = Deck.getCardBackImage();
        int panelWidth = getWidth();

        var name = model.getPlayerName(playerId);
        if (name != null) {
            g.setFont(nameFont);
            var metrics  = g.getFontMetrics();
            var textWidth = metrics.stringWidth(name);
            var textHeight = metrics.getHeight();
            g.drawString(name, (panelWidth - textWidth)/2, textHeight + 5);
        }

        for(int i=0; i < model.getNumCards(playerId); i++) {
            g.drawImage(cardImage, cardOffset + i*cardSpacing, baseCardTop, null);
        }

        if (model.getPlayedCard(playerId) != null) {
            g.drawImage(Deck.getCardImage(model.getPlayedCard(playerId)),
                    panelWidth - cardOffset - cardImage.getWidth(),
                    baseCardTop,
                    null);
        }
    }

    public static void main(String [] args) throws IOException {
        Deck.loadCardImages();

        SwingUtilities.invokeLater( () -> {
            JFrame frame = new JFrame("Demo");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            var model = new GameModel();
            final var playerPanel = new OtherPlayerPanel(model, 1, new Font(Font.SERIF, Font.BOLD, 16));
            model.addListener(new GameModel.ListenerAdapter() {
                @Override
                public void gameStateChanged() {
                    playerPanel.repaint();
                }
            });
            model.gameStarted(0, List.of("P1", "P2", "P3"));
            model.handStarted(List.of(0,1,2), 0, 5);
            model.cardPlayed(1, 5);
            frame.setContentPane(playerPanel);
            frame.setSize(500, 300);
            frame.setVisible(true);

        });

    }

}
