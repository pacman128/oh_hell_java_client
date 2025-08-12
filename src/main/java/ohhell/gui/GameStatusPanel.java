package ohhell.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel to show the status of the game.
 *
 * For each user it shows the bid amount, current tricks taken and score.
 */
public class GameStatusPanel extends JPanel {

    public GameStatusPanel(GameModel gameModel, Font font) {
        super(new BorderLayout());
        DefaultTableModel model = gameModel.getStatusTableModel();
        JTable table = new JTable(model);
        table.setFont(font);
        // Needed for table header to be display. See https://stackoverflow.com/a/31137737/1366027
        add(table, BorderLayout.CENTER);
        add(table.getTableHeader(), BorderLayout.NORTH);
        setBorder( BorderFactory.createLineBorder(Color.black));
    }

    public static void main(String [] args) {
        SwingUtilities.invokeLater( () -> {
            JFrame frame = new JFrame("Demo");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            var model = new GameModel();
            var statusPanel = new GameStatusPanel(model, new Font(Font.SERIF, Font.BOLD, 16));
            model.addListener( new GameModel.ListenerAdapter() {
                @Override
                public void gameStateChanged() {
                    statusPanel.repaint();
                }
            });
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
