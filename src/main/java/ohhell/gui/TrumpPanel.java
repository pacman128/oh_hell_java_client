package ohhell.gui;

import ohhell.game.Deck;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.List;

public class TrumpPanel extends JPanel {

    private final GameModel model;

    public TrumpPanel( GameModel model) {
        super();
        this.model = model;
        setBorder(BorderFactory.createLineBorder(Color.black));
        var image = Deck.getCardBackImage();
        Dimension size = new Dimension(20 + image.getWidth(), 50 + image.getHeight());
        setPreferredSize(size);
        setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Trump"));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        var trumpCard = model.getTrump();
        var cardImage = (trumpCard > 0) ? Deck.getCardImage(trumpCard) : Deck.getCardBackImage();

        g.drawImage(cardImage, 10, OtherPlayerPanel.baseCardTop, null);

    }

    public static void main(String [] args) throws IOException {
        Deck.loadCardImages();

        SwingUtilities.invokeLater( () -> {
            JFrame frame = new JFrame("Demo");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            var model = new GameModel();
            var trumpPanel = new TrumpPanel(model);
            model.handStarted(List.of(0, 1, 2), 0, 11);
            frame.setContentPane(trumpPanel);
            frame.setSize(100, 200);
            frame.setVisible(true);

        });

    }

}
