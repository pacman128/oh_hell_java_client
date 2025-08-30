package ohhell.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Panel to show log messages
 */
public class LogPanel extends JPanel {
    /** Text area for log messages */
    private final JTextArea textArea;

    /**
     * Create a new log panel
     * @param rows Number of rows
     * @param columns Num of columns
     */
    public LogPanel( int rows, int columns) {
        super(new BorderLayout());

        add(new JLabel("Game Log", SwingConstants.CENTER), BorderLayout.NORTH);
        textArea = new JTextArea(rows, columns);
        textArea.setEditable(false);
        textArea.setLineWrap(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Add a log message
     * @param text Message text
     */
    public void log(String text) {
        textArea.append(text + "\n");
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }

    /**
     * Test program
     * @param args Not used
     */
    public static void main(String [] args) {
        SwingUtilities.invokeLater( () -> {
            JFrame frame = new JFrame("Demo");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            LogPanel logPanel = new LogPanel(10, 100);
            frame.add(logPanel);
            frame.pack();
            frame.setVisible(true);

            for(int i=0; i< 15; i++) {
                logPanel.log("Log message");
            }
        });
    }
}
