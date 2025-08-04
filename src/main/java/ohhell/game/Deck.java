package ohhell.game;

import ohhell.Util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public final class Deck {

    private final ArrayList<Integer> cards;
    private int position = 0;

    static private final String[] suit = {"Clubs", "Diamonds", "Hearts", "Spades"};

    static private final String [] value = {
            "Two",
            "Three",
            "Four",
            "Five",
            "Six",
            "Seven",
            "Eight",
            "Nine",
            "Ten",
            "Jack",
            "Queen",
            "King",
            "Ace"
    };

    static private final ArrayList<BufferedImage> cardImages = new ArrayList<>();
    static private BufferedImage cardBackImage;

    public Deck(AbstractList<Integer> deck) {
        cards = new ArrayList<>(deck);
    }

    public Deck() {
        cards = new ArrayList<>();
        for( int i=0; i < 52; i++) {
            cards.add(i);
        }
        Collections.shuffle(cards);
    }

    public int draw() {
        return cards.get(position++);
    }

    public static int cardValue(int card) {
        return card % 13;
    }

    public static int cardSuit(int card) {
        return card/13;
    }

    public static String cardToString(int card) {
        return value[cardValue(card)] + " of " + suit[cardSuit(card)];
    }

    public static synchronized void loadCardImages() throws IOException {
        if (cardImages.isEmpty()) {
            Arrays.asList("c", "d", "h", "s").forEach( suit -> {
                Arrays.asList("2", "3", "4", "5", "6", "7", "8", "9", "t", "j", "q", "k", "a").forEach( value -> {
                   InputStream strm = Util.getFileFromResourceAsStream(Util.class,"images/" + suit + value + ".png");
                   try {
                       cardImages.add(ImageIO.read(strm));
                   } catch( IOException e) {
                       throw new RuntimeException((e));
                   }
                });
            });

            InputStream strm = Util.getFileFromResourceAsStream(Util.class, "images/back.png");
            cardBackImage = ImageIO.read(strm);
        }
    }

    public static synchronized BufferedImage getCardImage(int card) {
        return cardImages.get(card);
    }

    public static synchronized BufferedImage getCardBackImage() {
        return cardBackImage;
    }
}
