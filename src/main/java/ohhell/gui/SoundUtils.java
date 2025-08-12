package ohhell.gui;

import java.awt.Toolkit;

/**
 * Utilities for game sounds
 */
public class SoundUtils {

    /**
     * Make a beep sound
     */
    static void beep() {
        Toolkit.getDefaultToolkit().beep();
    }

    /**
     * Make a sound to remind user to play a card
     */
    static void playCard() {
        beep();
    }

    /**
     * Make a sound to remind user to bid
     */
    static void makeBid() {
        beep();
    }
}
