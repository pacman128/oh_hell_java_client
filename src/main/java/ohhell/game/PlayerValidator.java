package ohhell.game;

/**
 * Validator of user card plays
 */
public final class PlayerValidator {

    /** Cards in user's hand */
    private CardList cards;

    /**
     * Set list of users cards
     * @param cardList Cards dealt to player
     */
    public void setCards(CardList cardList) {
        cards = new CardList(cardList);
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
            return false;
        }

        // If user isn't leading, check that card is legal play
        if (leadCard >= 0) {
            int leadSuit = Deck.cardSuit(leadCard);
            if (cards.hasSuit(leadSuit) && Deck.cardSuit(card) != leadSuit) {
                return false;
            }
        }

        // Remove card from user's hand
        cards.removeCard(card);

        return true;
    }
}
