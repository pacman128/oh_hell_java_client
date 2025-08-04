package ohhell.game;

import java.util.AbstractList;

import static org.junit.jupiter.api.Assertions.*;

class CardListTest {
    private CardList cardList;

    @org.junit.jupiter.api.BeforeEach
    void init() {
        cardList = new CardList();
    }

    @org.junit.jupiter.api.Test
    void clear() {
        assertEquals(0, cardList.size());
        cardList.addCard(0);
        assertEquals(1, cardList.size());
        cardList.clear();
        assertEquals(0, cardList.size());
    }

    @org.junit.jupiter.api.Test
    void hasCard() {
        assertFalse(cardList.hasCard(0));
        assertFalse(cardList.hasCard(51));
        cardList.addCard(0);
        assertTrue(cardList.hasCard(0));
        assertFalse(cardList.hasCard(51));
        cardList.addCard(51);
        assertTrue(cardList.hasCard(0));
        assertTrue(cardList.hasCard(51));
    }

    @org.junit.jupiter.api.Test
    void addCard() {
        assertEquals(0, cardList.size());
        assertFalse(cardList.hasCard(0));
        assertFalse(cardList.hasCard(51));
        cardList.addCard(0);
        assertEquals(1, cardList.size());
        assertTrue(cardList.hasCard(0));
        assertFalse(cardList.hasCard(51));
        cardList.addCard(51);
        assertEquals(2, cardList.size());
        assertTrue(cardList.hasCard(0));
        assertTrue(cardList.hasCard(51));
    }

    @org.junit.jupiter.api.Test
    void removeCard() {
        assertEquals(0, cardList.size());
        assertFalse(cardList.hasCard(0));
        assertFalse(cardList.hasCard(51));
        cardList.addCard(0);
        assertEquals(1, cardList.size());
        assertTrue(cardList.hasCard(0));
        assertFalse(cardList.hasCard(51));
        cardList.addCard(51);
        assertEquals(2, cardList.size());
        assertTrue(cardList.hasCard(0));
        assertTrue(cardList.hasCard(51));
        cardList.removeCard(0);
        assertEquals(1, cardList.size());
        assertFalse(cardList.hasCard(0));
        assertTrue(cardList.hasCard(51));
        cardList.removeCard(10);
    }

    @org.junit.jupiter.api.Test
    void hasSuit() {
        assertEquals(0, cardList.size());
        for(int i=0; i < 3; i++) {
            assertFalse(cardList.hasSuit(i), "Suit: " + i);
        }
        cardList.addCard(0);
        assertTrue(cardList.hasSuit(0));
        for(int i=1; i < 3; i++) {
            assertFalse(cardList.hasSuit(i), "Suit: " + i);
        }

    }

    @org.junit.jupiter.api.Test
    void getFirstCard() {
        assertEquals(0, cardList.size());
        assertFalse(cardList.hasCard(10));
        assertFalse(cardList.hasCard(51));
        cardList.addCard(10);
        assertEquals(1, cardList.size());
        assertTrue(cardList.hasCard(10));
        assertFalse(cardList.hasCard(51));
        cardList.addCard(51);
        assertEquals(2, cardList.size());
        assertTrue(cardList.hasCard(10));
        assertTrue(cardList.hasCard(51));
        assertEquals(10, cardList.getFirstCard());
    }

    @org.junit.jupiter.api.Test
    void getList() {
        assertEquals(0, cardList.size());
        assertFalse(cardList.hasCard(10));
        assertFalse(cardList.hasCard(51));
        cardList.addCard(10);
        assertEquals(1, cardList.size());
        assertTrue(cardList.hasCard(10));
        assertFalse(cardList.hasCard(51));
        cardList.addCard(51);
        assertEquals(2, cardList.size());
        assertTrue(cardList.hasCard(10));
        assertTrue(cardList.hasCard(51));
        AbstractList<Integer> cards = cardList.getList();
        assertEquals(2, cards.size());
        assertEquals(10, cards.get(0));
        assertEquals(51, cards.get(1));
    }

    @org.junit.jupiter.api.Test
    void testToString() {
        assertEquals(0, cardList.size());
        assertFalse(cardList.hasCard(10));
        assertFalse(cardList.hasCard(51));
        cardList.addCard(10);
        assertEquals(1, cardList.size());
        assertTrue(cardList.hasCard(10));
        assertFalse(cardList.hasCard(51));
        cardList.addCard(51);
        assertEquals(2, cardList.size());
        assertTrue(cardList.hasCard(10));
        assertTrue(cardList.hasCard(51));
        assertEquals("[Queen of Clubs, Ace of Spades]", cardList.toString());
    }
}