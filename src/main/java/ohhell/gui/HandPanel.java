package ohhell.gui;

import ohhell.game.Deck;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Panel to display and select users cards
 */
public class HandPanel extends JPanel {

    private final static Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass().getPackage().getName());
    private final static int baseCardTop = 350;
    private final static int cardSpacing = 10;
    private final static int selectedCardOffset = 15;

    private final Rectangle cardSize;

    private final GameModel model;

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


    public void selectCard( int card) {
        model.setSelectedCard(card);
    }

    private List<Rectangle> getCardPositions() {
        final List<Rectangle> positions = new ArrayList<>();
        final int panelWidth = getWidth();
        final var cards = model.getCards();
        final int startingPos = (panelWidth - cards.size()*cardSize.width - (cards.size() -1)*cardSpacing)/2;
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

    private void handleMouseClick(MouseEvent e) {
        if (model.getPlayedCard() < 0) {
            var positions = getCardPositions();
            var cards = model.getCards();
            for (int i = 0; i < positions.size(); i++) {
                if (positions.get(i).contains(e.getPoint())) {
                    var card = cards.get(i);
                    if (card == model.getSelectedCard()) {
                        selectCard(-1);
                    } else {
                        selectCard(card);
                    }
                    break;
                }
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        final int playedCard = model.getPlayedCard();
        if (playedCard >= 0) {
            final int panelWidth = getWidth();
            final int pos = (panelWidth - cardSize.width)/2;

            g.drawImage(Deck.getCardImage(playedCard), pos, 20, null);
        }

        var positions = getCardPositions();
        final var cards = model.getCards();
        for(int i=0; i < cards.size(); i++) {
            BufferedImage image = Deck.getCardImage(cards.get(i));
            var pos = positions.get(i);
            g.drawImage(image, pos.x, pos.y, null);
        }
    }

    public static void main(String [] args) throws IOException {
        Deck.loadCardImages();

        SwingUtilities.invokeLater( () -> {
            JFrame frame = new JFrame("Demo");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            var model = new GameModel();
            HandPanel handPanel = new HandPanel(model);
            model.handStarted(List.of(0, 14, 45), 0, 1);
            model.setSelectedCard(14);
            handPanel.setOpaque(true);
            frame.setContentPane(handPanel);
            frame.setSize(500, 300);
            frame.setVisible(true);

        });

    }
}
