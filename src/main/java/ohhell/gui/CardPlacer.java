package ohhell.gui;

import ohhell.game.Deck;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class to compute where cards of players hand should be placed
 */
public final class CardPlacer {

    /** Size of a card */
    private static final Rectangle cardSize;
    /** Horizontal spacing between cards in pixels */
    private final static int cardSpacing = 10;
    /** Y offset for selected card */
    private final static int selectedCardOffset = 15;
    /** Height offset of card */
    private final static int heightOffset;
    /** Max number of cards in a row */
    private final static int maxCardsPerRow = 8;

    static {
        var cardImage = Deck.getCardBackImage();
        cardSize = new Rectangle(cardImage.getWidth(), cardImage.getHeight());
        heightOffset = Deck.getCardBackImage().getHeight() + 30;
    }

    /**
     * CardPosition record
     * @param card Value of card
     * @param position Position of card
     * @param selected Is card selected?
     */
    public record CardPosition( int card, Rectangle position, boolean selected) { }

    /**
     * Place cards in position
     * @param cards Player cards (sorted)
     * @param selectedCard Selected card (-1 for none)
     * @return List of card positions
     */
    public static List<CardPosition> placeCards( List<Integer> cards,
                                                 int selectedCard) {
        var cardPositions = new ArrayList<CardPosition>();

        int rowTopY = 40 + cardSize.height;
        for( int suit=0; suit<4; suit++) {
            var rowCards = getCardsInSuit(cards, suit);
            if (!rowCards.isEmpty()) {
                if (rowCards.size() <= maxCardsPerRow) {
                    cardPositions.addAll(placeCardsHorizontally(rowCards, selectedCard, rowTopY, 0));
                } else {
                    cardPositions.addAll(placeCardsHorizontally(rowCards.subList(0, maxCardsPerRow - 1),
                                                                selectedCard,
                                                                rowTopY,
                                                          0));
                    rowTopY += heightOffset;
                    cardPositions.addAll(placeCardsHorizontally(rowCards.subList(maxCardsPerRow, rowCards.size()),
                                                                selectedCard,
                                                                rowTopY,
                                                         1));
                }
                rowTopY += heightOffset;
            }
        }
        return cardPositions;
    }

    /**
     * Compute the places for a horizontal line of cards
     * @param cards Cards to display
     * @param selectedCard Selected card (-1 for none)
     * @param verticalPos Vertical coordinate of line
     * @param startX Starting X position of first card
     * @return List of CardPositions
     */
    private static List<CardPosition> placeCardsHorizontally(List<Integer> cards,
                                                             int selectedCard,
                                                             int verticalPos,
                                                             int startX) {
        var cardPositions = new ArrayList<CardPosition>();
        int i = startX;
        for( var card: cards) {
            int cardVerticalPos = (card == selectedCard) ? verticalPos - selectedCardOffset : verticalPos;
            cardPositions.add(new CardPosition(card,
                                               new Rectangle(cardSpacing + i*(cardSize.width + cardSpacing),
                                                              cardVerticalPos,
                                                              cardSize.width,
                                                              cardSize.height),
                                               card == selectedCard));
            i++;
        }
        return cardPositions;

    }

    /**
     * Get list of cards in the specified suit
     * @param cards Player cards (sorted)
     * @param suit Suit of desired cards
     * @return List of cards in specified suit
     */
    private static List<Integer> getCardsInSuit( List<Integer> cards, int suit) {
        var cardsInSuit = new ArrayList<Integer>();
        var suitStart = 13*suit;
        var suitEnd = suitStart + 13;
        for(var card: cards) {
            if (card >= suitStart && card < suitEnd) {
                cardsInSuit.add(card);
            }
        }
        return cardsInSuit;
    }
}
