package ohhell.game;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * Collection of cards of a player
 */
public final class CardList {
    /** Sorted set of card values */
    private final TreeSet<Integer> cards;

    /**
     * Create a new card list
     */
    public CardList() {
        cards = new TreeSet<>();
    }

    /**
     * Create a copy of a CardList
     * @param orig Original list to copy
     */
    public CardList(CardList orig) {
        cards = new TreeSet<>(orig.cards);
    }

    /**
     * Empty the list
     */
    public void clear() {
        cards.clear();
    }

    /**
     * Does list contain a card?
     * @param card Card to look for
     * @return true if in list, else false
     */
    public boolean hasCard(int card) {
        return cards.contains(card);
    }

    /**
     * Add a card to the list
     * @param card Card to add
     */
    public void addCard(int card) {
        cards.add(card);
    }

    /**
     * Add a list of cards
     * @param cards List to add
     */
    public void addCards(List<Integer> cards) {
        this.cards.addAll(cards);
    }

    /**
     * Remove a card from the list
     * @param card Card to remove
     */
    public void removeCard(int card) {
        cards.remove(card);
    }

    /**
     * Does list contain a card with given suit
     * @param suit Suit (0->Clubs, 1->Diamonds, 2->Hearts, 3->Spades)
     * @return true if suit found, else false
     */
    public boolean hasSuit(int suit) {
        return !cardsInSuit(suit).isEmpty();
    }

    /**
     * List of the cards in suit that are in hand
     * @param suit Suit to look for
     * @return List of cards in suit are in hand
     */
    public List<Integer> cardsInSuit(int suit) {
        ArrayList<Integer> suitCards = new ArrayList<>();
        for( int i=13*suit; i < 13*suit + 13; i++) {
            if (cards.contains((i))) {
                suitCards.add(i);
            }
        }
        return suitCards;
    }
    /**
     * How many cards are in list
     * @return Number of cards in list
     */
    public int size() {
        return cards.size();
    }

    /**
     * Get lowest card in list.
     * This is most useful to get only card in a list of one card.
     * @return lowest card in list
     */
    public int getFirstCard() {
        return cards.iterator().next();
    }

    /**
     * Get cards as a list
     * @return Cards as a list
     */
    public AbstractList<Integer> getList() {
        return new ArrayList<>(cards);
    }

    /**
     * Convert list to a string
     * @return String rep of list
     */
    @Override
    public String toString() {
        StringBuilder list = new StringBuilder("[");
        if (! cards.isEmpty()) {
            for (int card : cards) {
                list.append(Deck.cardToString(card)).append(", ");
            }
            list.delete(list.length() - 2, list.length());
        }
        list.append("]");
        return list.toString();
    }

}
