package ohhell.game;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.logging.Logger;

/**
 * Validator of user card plays
 */
public final class PlayerValidator {
    /** Logger. */
    private final static Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass().getPackage().getName());

    /** Cards in user's hand */
    private final CardList cards = new CardList();

    /**
     * Set list of users cards
     * @param cards Cards dealt to player
     */
    public void setCards(List<Integer> cards) {
        this.cards.clear();
        this.cards.addCards(cards);
    }

    /**
     * Validate the play of card by user
     * @param card Card to validate
     * @param leadCard First card played for trick (-1 if user is in lead)
     * @return true if card is valid
     */
    public boolean validateCard(int card, int leadCard) {
        // First check that player has card
        if (!cards.hasCard(card)) {
            logger.warning(String.format("Card: %s not in hand", Deck.cardToString(card)));
            return false;
        }

        // If user isn't leading, check that card is legal play
        if (leadCard >= 0) {
            int leadSuit = Deck.cardSuit(leadCard);
            if (cards.hasSuit(leadSuit) && Deck.cardSuit(card) != leadSuit) {
                logger.warning(String.format("Card: %s, not valid because of lead card: %s",
                                             Deck.cardToString(card), Deck.cardToString(leadCard)));
                return false;
            }
        }

        // Remove card from user's hand
        cards.removeCard(card);

        return true;
    }
}
