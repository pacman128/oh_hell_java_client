package ohhell.gui;

import ohhell.game.Settings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.logging.Logger;

/**
 * Dialog to change settings
 */
public class SettingsDialog extends JDialog {
    private final static Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass().getPackage().getName());

    private final JTextField host = new JTextField(20);

    private final IntTextField port = new IntTextField(7000, 5);

    private final JTextField name = new NameTextField( "", 15);

    private final static String [] freqs = { "None", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
    private final JComboBox<String> reminderFreq = new JComboBox<>(freqs);

    private final Settings settings;

    private void addRow(JPanel mainPanel, String label, JComponent comp) {
        var l = new JLabel(label);
        mainPanel.add(l);
        l.setLabelFor(comp);
        mainPanel.add(comp);
    }

    public SettingsDialog(Frame aFrame, Settings settings) {
        super(aFrame, true);
        this.settings = settings;
        setTitle("Settings");

        var contentPane = new JPanel(new BorderLayout());
        var mainLayout = new SpringLayout();
        var mainPanel = new JPanel(mainLayout);
        var buttonPanel = new JPanel(new FlowLayout( FlowLayout.TRAILING));
        var okBtn = new JButton("OK");
        var cancelBtn = new JButton("Cancel");

        okBtn.addActionListener(this::processOk);
        cancelBtn.addActionListener(this::processCancel);

        buttonPanel.add(okBtn);
        buttonPanel.add(cancelBtn);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);
        contentPane.add(mainPanel, BorderLayout.CENTER);

        addRow(mainPanel, "Name", name);
        addRow(mainPanel, "Host", host);
        addRow(mainPanel, "Port", port);
        addRow(mainPanel, "Audible Reminder Freq (sec)", reminderFreq);

        SpringUtilities.makeCompactGrid(mainPanel, 4, 2, 6, 6, 6, 6);
        setContentPane(contentPane);
        pack();

        var compListener = new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                super.componentShown(e);
                try {
                    settings.readSettings();
                    updateFields();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        };
        addComponentListener(compListener);
    }

    private void updateFields() {
        name.setText(settings.getName());
        host.setText(settings.getHost());
        port.setText("" + settings.getPort());
        if (settings.getAudibleReminderFreq() == null) {
            reminderFreq.setSelectedIndex(0);
        } else {
            reminderFreq.setSelectedIndex( settings.getAudibleReminderFreq() - 1);
        }
    }

    private void updateSettings() {
        Integer reminderValue = (reminderFreq.getSelectedIndex() == 0 ? null : Integer.parseInt((String) reminderFreq.getSelectedItem()));
        settings.setValues(new Settings.SettingsRec(name.getText(), host.getText(), port.getValue(), reminderValue));
    }

    private void processOk(ActionEvent e) {
        setVisible(false);
        try {
            updateSettings();
            settings.writeSettings();
        } catch (IOException ex) {
            logger.severe("Error writing settings: " + ex.getLocalizedMessage());
        }
    }

    private void processCancel(ActionEvent e) {
        setVisible(false);
    }

    public static void createAndShowGUI() {
        var frame = new JFrame("SettingsDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        var settings = new Settings("/home/pcarter/settings.json", (rec) -> {});
        var dialog = new SettingsDialog(frame, settings);
        var contentPanel = new JPanel();
        var showSettings = new JButton("Settings");
        contentPanel.add(showSettings);
        contentPanel.setOpaque(true);
        showSettings.addActionListener( e -> { dialog.setLocationRelativeTo(frame); dialog.setVisible(true);});
        frame.setContentPane(contentPanel);
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String [] args) {
        SwingUtilities.invokeLater(SettingsDialog::createAndShowGUI);
    }
}
