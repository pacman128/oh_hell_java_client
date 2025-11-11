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
     * Remind user to play a card
     */
    static void playCard() {
        beep();
    }

    /**
     * Remind user to bid
     */
    static void makeBid() {
        beep();
    }
}
