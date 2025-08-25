package ohhell.gui;

import ohhell.game.ClientProtocol;
import ohhell.game.Deck;
import ohhell.game.Settings;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class MainPanel extends JPanel implements GameModel.Listener, GameModel.UserInput {

    private final static Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass().getPackage().getName());
    private final static Font textFont = new Font(Font.SERIF, Font.BOLD, 16);
    private final static int cardTextSize = 20;
    private final static Color notifyColor = Color.green;

    private final GameModel model;
    private final JPanel otherPlayersPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
    private final Map<Integer, OtherPlayerPanel> otherPlayers = new HashMap<>();
    private final LogPanel logPanel = new LogPanel(20, 30);
    private final JButton playButton = new JButton("Play");
    private final JLabel playLabel = new JLabel(getCardText(null));
    private final IntTextField bidField =  new IntTextField(0, 2);
    private final JPanel playButtonPanel;
    private final JPanel bidPanel;

    private int beepCounter = 0;
    private ClientProtocol.InputCallback cardCallback;
    private ClientProtocol.InputCallback bidCallback;

    /**
     * Get card text for the play card panel.
     *
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

    public MainPanel(GameModel model) {
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
    }

    private static void blink(JComponent comp, Color normalColor, Color otherColor) {
        comp.setBackground((comp.getBackground() == normalColor) ? otherColor : normalColor);
    }

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
            }
            if (model.cardRequired()) {
                blink(playButtonPanel, normalBackground, notifyColor);
            }
        }

    }

    public void log(String msg) {
        logPanel.log(msg);
    }

    @Override
    public void newPlayer(int id, String name) {
        var playerPanel = new OtherPlayerPanel(model, id, textFont);
        otherPlayersPanel.add(playerPanel);
        otherPlayers.put(id, playerPanel);
        repaint();
    }

    @Override
    public void settingsChanged(Settings.SettingsValues rec) {
        beepCounter = 0;
    }

    @Override
    public void getCard( ClientProtocol.InputCallback callback) {
        SoundUtils.playCard();
        cardCallback = callback;
        playButton.setEnabled(model.getSelectedCard() >= 0);
    }

    @Override
    public void getBid( ClientProtocol.InputCallback callback) {
        SoundUtils.makeBid();
        bidCallback = callback;
    }

    @Override
    public void gameStateChanged() {
        switch(model.getState()) {
            case WAITING_FOR_CARD_RESPONSE:
                playButtonPanel.setBackground(notifyColor);
                break;
            case WAITING_FOR_BID_RESPONSE:
                bidPanel.setBackground(notifyColor);
                break;
            default:
                playButtonPanel.setBackground(playButtonPanel.getParent().getBackground());
                bidPanel.setBackground(playButtonPanel.getParent().getBackground());
                break;
        }

        var playCard = cardCallback != null
                && model.getSelectedCard() >= 0
                && model.getState() == GameModel.State.WAITING_FOR_CARD_RESPONSE;
        playButton.setEnabled(playCard);
        if (playCard) {
            playLabel.setText(getCardText(model.getSelectedCard()));
        } else {
            playLabel.setText(getCardText(null));
        }
        bidField.setEnabled(bidCallback != null
                            && model.getState() == GameModel.State.WAITING_FOR_BID_RESPONSE);
        repaint();
    }

    private void cardPlayed(ActionEvent e) {
        playButton.setEnabled(false);
        cardCallback.returnValue(model.getSelectedCard());
    }

    private void bidMade(ActionEvent e) {
        bidField.setEnabled(false);
        bidCallback.returnValue(bidField.getValue());
        bidField.clear();
    }

    public static void main(String [] args) throws IOException {
        Deck.loadCardImages();

        SwingUtilities.invokeLater( () -> {
            JFrame frame = new JFrame("Oh Hell");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            final var cards = List.of(6, 10, 21, 33, 34, 40);
            final var model = new GameModel();
            final var panel = new MainPanel(model);
            model.addListener( panel);
            model.setUserInput( panel);
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
