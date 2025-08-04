package ohhell.gui;

import javax.swing.*;
import java.awt.*;

public class LogPanel extends JPanel {
    private final JTextArea textArea;

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

    public void log(String text) {
        textArea.append(text + "\n");
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }

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
