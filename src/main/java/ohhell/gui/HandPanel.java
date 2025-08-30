package ohhell.gui;

import ohhell.Util;
import ohhell.game.Deck;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Panel to display and select users cards.
 * Displays player's cards and allows card to be selected
 * by clicking on it. Selected card is raised and outlined.
 */
public class HandPanel extends JPanel {

    /** Y coordinate of top of card */
    private final static int baseCardTop = 350;
    /** Horizontal spacing between cards in pixels */
    private final static int cardSpacing = 10;
    /** Y offset for selected card */
    private final static int selectedCardOffset = 15;

    /** Size of a card image */
    private final Rectangle cardSize;

    /** Game model */
    private final GameModel model;

    /**
     * Create a new panel
     * @param model Game model
     */
    public HandPanel( GameModel model) {
        super(new BorderLayout());
        this.model = model;
        var cardImage = Deck.getCardBackImage();
        cardSize = new Rectangle(cardImage.getWidth(), cardImage.getHeight());
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                handleMouseClick(e);
            }
        });
        setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Hand"));
    }

    /**
     * Select a card
     * @param card Value of card to select
     */
    public void selectCard( int card) {
        model.setSelectedCard(card);
    }

    /**
     * Compute the positions of the cards
     * @return List of rectangles of cards
     */
    private List<Rectangle> getCardPositions() {
        final List<Rectangle> positions = new ArrayList<>();
        final int panelWidth = getWidth();
        final var cards = model.getCards();
        final int startingPos = (panelWidth - cards.size()*cardSize.width - (cards.size() -1)*cardSpacing)/2;
        // If more than 5 cards, use two rows of cards
        final boolean twoRows = cards.size() > 5;
        final int selectedCard = model.getSelectedCard();

        if ( twoRows) {
            final int heightOffset = Deck.getCardBackImage().getHeight() + 40;

            for(int i = 0 ; i < cards.size() - 5; i++) {
                positions.add(new Rectangle(startingPos + i*(cardSize.width + cardSpacing),
                        ((cards.get(i) == selectedCard) ? baseCardTop - selectedCardOffset: baseCardTop) - heightOffset,
                        cardSize.width,
                        cardSize.height));
            }

        }

        final int startIndex = twoRows ? cards.size() - 5 : 0;
        for(int i = startIndex; i < cards.size(); i++) {
            positions.add(new Rectangle(startingPos + (i - startIndex)*(cardSize.width + cardSpacing),
                    (cards.get(i) == selectedCard) ? baseCardTop - selectedCardOffset : baseCardTop,
                    cardSize.width,
                    cardSize.height));
        }
        return positions;
    }

    /**
     * Handle a mouse click
     * @param e Mouse event
     */
    private void handleMouseClick(MouseEvent e) {
        // If game is waiting for a card to be selected
        if (model.getPlayedCard() < 0) {

            var positions = getCardPositions();
            var cards = model.getCards();
            // Look for a card with a rectangle that contains the mouse position
            for (int i = 0; i < positions.size(); i++) {
                if (positions.get(i).contains(e.getPoint())) {
                    var card = cards.get(i);

                    // If clicked on selected card
                    if (card == model.getSelectedCard()) {
                        // Unselect card
                        selectCard(-1);
                    } else {
                        // Select indicated card
                        selectCard(card);
                    }
                    repaint();
                    break;
                }
            }
        }
    }

    /**
     * Draw the panel
     * @param g Graphics context to use
     */
    @Override
    protected void paintComponent(Graphics g) {
        // Do normal paint
        super.paintComponent(g);

        // If a card has been played, display it at top of panel
        final int playedCard = model.getPlayedCard();
        if (playedCard >= 0) {
            final int panelWidth = getWidth();
            final int pos = (panelWidth - cardSize.width)/2;

            g.drawImage(Deck.getCardImage(playedCard), pos, 20, null);
        }

        // Display the unplayed cards
        var positions = getCardPositions();
        final var cards = model.getCards();
        final var selectedCard = model.getSelectedCard();
        for(int i=0; i < cards.size(); i++) {
            BufferedImage image = Deck.getCardImage(cards.get(i));
            var pos = positions.get(i);
            if (cards.get(i) == selectedCard) {
                g.setColor(Color.red);
                g.drawRect(pos.x -2, pos.y-2, image.getWidth() + 4, image.getHeight() + 4);
            }
            g.drawImage(image, pos.x, pos.y, null);
        }
    }

    /**
     * Test program
     * @param args Unused
     * @throws IOException On I/O error
     */
    public static void main(String [] args) throws IOException {
        Util.setLevel(Logger.getLogger("ohhell"), Level.FINER);
        Deck.loadCardImages();

        SwingUtilities.invokeLater( () -> {
            JFrame frame = new JFrame("Demo");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            var model = new GameModel();
            HandPanel handPanel = new HandPanel(model);
            model.handStarted(List.of(0, 3, 6, 9, 14, 20, 24, 30, 35, 45), 0, 1);
            model.setSelectedCard(14);
            handPanel.setOpaque(true);
            frame.setContentPane(handPanel);
            frame.setSize(900, 500);
            frame.repaint();
            frame.setVisible(true);

        });

    }
}
