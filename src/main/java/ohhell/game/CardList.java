package ohhell.game;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public final class CardList {
    private final TreeSet<Integer> cards;

    public CardList() {
        cards = new TreeSet<>();
    }

    public CardList(CardList orig) {
        cards = new TreeSet<>(orig.cards);
    }

    public void clear() {
        cards.clear();
    }

    public boolean hasCard(int card) {
        return cards.contains(card);
    }

    public void addCard(int card) {
        cards.add(card);
    }

    public void addCards(List<Integer> cards) {
        this.cards.addAll(cards);
    }

    public void removeCard(int card) {
        cards.remove(card);
    }

    public boolean hasSuit(int suit) {
        for( int i=13*suit; i < 13*suit + 13; i++) {
            if (cards.contains((i))) {
                return true;
            }
        }
        return false;
    }

    public int size() {
        return cards.size();
    }

    public int getFirstCard() {
        return cards.iterator().next();
    }

    public AbstractList<Integer> getList() {
        return new ArrayList<>(cards);
    }

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
