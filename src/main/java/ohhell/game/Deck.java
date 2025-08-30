package ohhell.game;

import ohhell.Util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Representation of a deck of cards.
 * Card values are represented as integers in canonical suit order (Clubs, Diamonds, Hearts, Spades).
 * The rank of a card is represented by a number between 0 and 12 (0 -> Two, 1 -> Three, ... 12 -> Ace)
 * The suit of a card is represented by a number between 0 and 3 (0 -> Clubs, 1 -> Diamonds, ...)
 * So 0 -> Two of Clubs, 1 -> Three of Clubs, ... 12 -> Ace of Clubs,
 *   13 -> Two of Diamonds, ... 51 -> Ace of Spades
 */
public final class Deck {

    /** Suit names in canonical order */
    static private final String[] suit = {"Clubs", "Diamonds", "Hearts", "Spades"};

    /** Value names in order */
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

    /** List of card images */
    static private final ArrayList<BufferedImage> cardImages = new ArrayList<>();

    /** Image of back of card */
    static private BufferedImage cardBackImage;

    /**
     * Get rank of card.
     * For example, this returns 1 for 14 since 14 represents the Three of Diamonds.
     * @param card Card value
     * @return Card rank
     */
    public static int cardRank(int card) {
        return card % 13;
    }

    /**
     * Get suit of card
     * @param card Card value
     * @return Card suit (0 -> Clubs, 1 -> Diamonds, 2 -> Hearts, 3 -> Spades)
     */
    public static int cardSuit(int card) {
        return card/13;
    }

    /**
     * Get ASCII string representation of card
     * @param card Card value
     * @return String representation
     */
    public static String cardToString(int card) {
        return value[cardRank(card)] + " of " + suit[cardSuit(card)];
    }

    /**
     * Load card images.
     * This method must be called before any call to getCardImage or getCardBackImage.
     * @throws IOException On I/O error
     */
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

    /**
     * Get image of card
     * @param card Card value
     * @return Image of card
     */
    public static synchronized BufferedImage getCardImage(int card) {
        return cardImages.get(card);
    }

    /**
     * Get image of back of card
     * @return Image of back of card
     */
    public static synchronized BufferedImage getCardBackImage() {
        return cardBackImage;
    }
}
