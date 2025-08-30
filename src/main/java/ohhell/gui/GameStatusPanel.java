package ohhell.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel to show the status of the game.
 * For each user it shows the bid amount, current tricks taken and score.
 */
public class GameStatusPanel extends JPanel {

    /** Label to display dealer name */
    private final JLabel dealer = new JLabel();

    /** Label to show number of tricks in current hand */
    private final JLabel numTricks = new JLabel();

    /** Model for game */
    private final GameModel model;

    /**
     * Create a new status panel
     * @param gameModel Model for game
     * @param font Font for table
     */
    public GameStatusPanel(GameModel gameModel, Font font) {
        super(new BorderLayout());
        model = gameModel;
        var handStatusPanel = new JPanel(new FlowLayout());
        handStatusPanel.add(new JLabel("Num Tricks: "));
        handStatusPanel.add(numTricks);
        handStatusPanel.add(new JLabel("  Dealer: "));
        handStatusPanel.add(dealer);
        add(handStatusPanel, BorderLayout.NORTH);
        var tablePanel = new JPanel(new BorderLayout());
        add(tablePanel, BorderLayout.CENTER);
        DefaultTableModel tableModel = gameModel.getStatusTableModel();
        JTable table = new JTable(tableModel);
        table.setFont(font);
        // Needed for table header to be display. See https://stackoverflow.com/a/31137737/1366027
        tablePanel.add(table, BorderLayout.CENTER);
        tablePanel.add(table.getTableHeader(), BorderLayout.NORTH);
        setBorder( BorderFactory.createLineBorder(Color.black));
        this.model.addListener( new GameModel.ListenerAdapter() {
            @Override
            public void gameStateChanged() {
                modelUpdate();
            }
        });
    }

    /**
     * Update panel from model
     */
    private void modelUpdate() {
        String dealerName = "";
        if ( model.getDealer() >= 0) {
            dealerName = model.getPlayerName(model.getDealer());
        }
        dealer.setText(dealerName);
        String numTricksText = "";
        if (model.getNumCardsInHand() > 0) {
            numTricksText = String.format("%d", model.getNumCardsInHand());
        }
        numTricks.setText(numTricksText);
        repaint();
    }

    public static void main(String [] args) {
        SwingUtilities.invokeLater( () -> {
            JFrame frame = new JFrame("Demo");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            var model = new GameModel();
            var statusPanel = new GameStatusPanel(model, new Font(Font.SERIF, Font.BOLD, 16));
            model.playerRegistered(1, "Anne");
            model.gameStarted(0, List.of("Paul", "Anne"));
            model.handStarted(List.of(3, 10, 25), 0, 6);
            model.bidMade(0, 2);
            model.bidMade(1, 1);
            //statusPanel.setTricks(0, 1);
            statusPanel.setOpaque(true);
            frame.setContentPane(statusPanel);
            frame.setSize(500, 300);
            frame.setVisible(true);

        });
    }
}
