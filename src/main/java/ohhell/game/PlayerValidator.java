package ohhell.game;

public final class PlayerValidator {

    private CardList cards;

    public void setCards(CardList cardList) {
        cards = new CardList(cardList);
    }

    public boolean validateCard(int card, int leadCard) {
        if (!cards.hasCard(card)) {
            return false;
        }
        if (leadCard >= 0) {
            int leadSuit = Deck.cardSuit(leadCard);
            if (cards.hasSuit(leadSuit) && Deck.cardSuit(card) != leadSuit) {
                return false;
            }
        }
        cards.removeCard(card);
        return true;
    }
}
