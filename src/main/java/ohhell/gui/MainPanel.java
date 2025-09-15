package ohhell.gui;

import ohhell.game.ClientProtocol;
import ohhell.game.Deck;
import ohhell.game.Settings;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.List;

/**
 * Main panel for the game
 */
public class MainPanel extends JPanel implements GameModel.Listener {

    /** Text font to use */
    private final static Font textFont = new Font(Font.SERIF, Font.BOLD, 16);
    /** Card text size */
    private final static int cardTextSize = 20;
    /** Color to blink when user input is required */
    private final static Color notifyColor = Color.green;

    /** Game model */
    private final GameModel model;
    /** Panel to display other player's hands */
    private final JPanel otherPlayersPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
    /** Panel with log messages */
    private final LogPanel logPanel = new LogPanel(20, 30);
    /** Play card button */
    private final JButton playButton = new JButton("Play");
    /** Card to play label */
    private final JLabel playLabel = new JLabel(getCardText(null));
    /** Bid input field */
    private final IntTextField bidField =  new IntTextField(0, 2);
    /** Panel with play button */
    private final JPanel playButtonPanel;
    /** Panel with bid input field */
    private final JPanel bidPanel;
    /** Counter for beep reminder to give input */
    private int beepCounter = 0;
    /** Callback for playing a card */
    private ClientProtocol.InputCallback cardCallback;
    /** Callback for making a bid */
    private ClientProtocol.InputCallback bidCallback;

    /**
     * Get card text for the play card panel.
     * This method pads the text with spaces to keep the panel the same size no matter what
     * card is selected (or none selected)
     * @param card card value as Integer (null if not card selected)
     * @return String with card value padded to cardTextSize characters or all spaces if card is null
     */
    private static String getCardText( Integer card) {
        if (card == null) {
            return " ".repeat(cardTextSize);
        }
        var cardText = Deck.cardToString(card);
        return cardText + " ".repeat(cardTextSize - cardText.length());
    }

    /**
     * Create a main panel
     * @param frame JFrame for game
     * @param model Game model
     */
    public MainPanel(JFrame frame, GameModel model) {
        super(new BorderLayout());

        this.model = model;
        model.setGameLogger(this::log);
        var trumpPanel = new TrumpPanel(model);
        var handPanel = new HandPanel(model);
        var gameStatusPanel = new GameStatusPanel(model, textFont);
        var centerPanel = new JPanel(new BorderLayout());
        add(centerPanel, BorderLayout.CENTER);
        var topPanel = new JPanel(new BorderLayout());
        topPanel.add(trumpPanel, BorderLayout.WEST);
        topPanel.add(otherPlayersPanel, BorderLayout.CENTER);
        centerPanel.add(topPanel, BorderLayout.NORTH);

        var rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(new EmptyBorder(3,3,3,3));
        rightPanel.add(gameStatusPanel, BorderLayout.NORTH);
        rightPanel.add(logPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
        centerPanel.add(handPanel, BorderLayout.CENTER);
        handPanel.setMinimumSize(new Dimension(800, 700));
        JPanel buttonPanel = new JPanel(new BorderLayout());
        playButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        playButtonPanel.add(playButton);
        playButtonPanel.add(playLabel);
        playLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));
        buttonPanel.add(playButtonPanel, BorderLayout.WEST);
        bidPanel = new JPanel(new FlowLayout((FlowLayout.TRAILING)));
        buttonPanel.add(bidPanel, BorderLayout.EAST);
        JLabel bidLabel = new JLabel("Bid: ");
        bidPanel.add(bidLabel);
        bidPanel.add(bidField);
        centerPanel.add(buttonPanel, BorderLayout.SOUTH);
        playButton.setEnabled(false);
        playButton.addActionListener(this::cardPlayed);
        bidField.addActionListener(this::bidMade);
        // Enter acts as click to Play button
        frame.getRootPane().setDefaultButton(playButton);
    }

    /**
     * Blink the background of a component
     * @param comp Component to blink
     * @param normalColor Normal background color
     * @param otherColor Other background color to blink to
     */
    private static void blink(JComponent comp, Color normalColor, Color otherColor) {
        comp.setBackground((comp.getBackground() == normalColor) ? otherColor : normalColor);
    }

    /**
     * Hint to the user that an action is needed by beeping and blinking
     */
    public void processUserHints() {
        if (model.userActionRequired()) {
            var reminderFreq = model.getSettings().getAudibleReminderFreq();
            if ( reminderFreq != null ) {
                beepCounter++;
                if (beepCounter >= reminderFreq) {
                    SoundUtils.beep();
                    beepCounter = 0;
                }
            }

            var normalBackground = playButtonPanel.getParent().getBackground();
            if (model.bidRequired()) {
                blink(bidPanel, normalBackground, notifyColor);
                bidField.requestFocusInWindow();
            }
            if (model.cardRequired()) {
                blink(playButtonPanel, normalBackground, notifyColor);
                playButton.requestFocusInWindow();
            }
        }

    }

    /**
     * Add a message to the log panel
     * @param msg Message
     */
    public void log(String msg) {
        logPanel.log(msg);
    }

    /**
     * Process new player
     * @param id ID of player
     * @param name Name of player
     */
    @Override
    public void newPlayer(int id, String name) {
        var playerPanel = new OtherPlayerPanel(model, id, textFont);
        otherPlayersPanel.add(playerPanel);
        repaint();
    }

    /**
     * Handle settings change
     * @param rec New settings
     */
    @Override
    public void settingsChanged(Settings.SettingsValues rec) {
        // Reset beepCounter
        beepCounter = 0;
    }

    /**
     * Handle a game state change
     */
    @Override
    public void gameStateChanged() {
        switch(model.getState()) {
            case WAITING_FOR_CARD_RESPONSE:
                playButtonPanel.setBackground(notifyColor);
                break;
            case WAITING_FOR_BID_RESPONSE:
                bidPanel.setBackground(notifyColor);
                bidField.requestFocusInWindow();
                break;
            default:
                playButtonPanel.setBackground(playButtonPanel.getParent().getBackground());
                bidPanel.setBackground(playButtonPanel.getParent().getBackground());
                break;
        }

        var playCard = model.getSelectedCard() >= 0
                       && model.getState() == GameModel.State.WAITING_FOR_CARD_RESPONSE;
        playButton.setEnabled(playCard);
        if (playCard) {
            playLabel.setText(getCardText(model.getSelectedCard()));
        } else {
            playLabel.setText(getCardText(null));
        }
        bidField.setEnabled(model.getState() == GameModel.State.WAITING_FOR_BID_RESPONSE);
        repaint();
    }

    /**
     * Return the card played by user
     * @param e Unused
     */
    private void cardPlayed(ActionEvent e) {
        playButton.setEnabled(false);
        model.playCard(model.getSelectedCard());
    }

    /**
     * Return the bid made by user
     * @param e Unused
     */
    private void bidMade(ActionEvent e) {
        bidField.setEnabled(false);
        model.bid(bidField.getValue());
        bidField.clear();
    }

    /**
     * Test program
     * @param args Unused
     * @throws IOException On I/O error
     */
    public static void main(String [] args) throws IOException {
        Deck.loadCardImages();

        SwingUtilities.invokeLater( () -> {
            JFrame frame = new JFrame("Oh Hell");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            final var cards = List.of(6, 10, 21, 33, 34, 40);
            final var model = new GameModel();
            final var panel = new MainPanel(frame, model);
            model.addListener( panel);
            model.gameStarted(0, List.of("User", "P1", "P2"));
            panel.log("Log msg");
            model.handStarted(cards, 0, 25);
            model.bidMade(0, 2);
            model.bidMade(1, 1);
            model.bidMade(2, 2);
            model.cardPlayed(1, 5);
            model.cardPlayed(2, 9);
            model.getCard( (card) -> {System.out.println("Played: " + Deck.cardToString(card)); });
            panel.setOpaque(true);
            frame.setContentPane(panel);
            frame.setSize(1000, 800);
            frame.setVisible(true);

        });

    }

}
