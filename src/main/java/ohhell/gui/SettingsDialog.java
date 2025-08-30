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
    /** Logger */
    private final static Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass().getPackage().getName());

    /** Combo box model to pick server */
    private final DefaultComboBoxModel<String> serverModel = new DefaultComboBoxModel<>();
    /** Combo box to pick server */
    private final JComboBox<String> server = new JComboBox<>(serverModel);
    /** Player name field */
    private final JTextField name = new NameTextField( "", 15);

    /** Values for reminder frequencies */
    private final static String [] freqs = { "None", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
    /** Combo box for reminder frequencies */
    private final JComboBox<String> reminderFreq = new JComboBox<>(freqs);
    /** Label to show server parameters */
    private final JLabel serverLabel = new JLabel();
    /** Game settings */
    private final Settings settings;

    /**
     * Add row consisting of a label and a component
     * @param mainDialogPanel Main panel of dialog
     * @param label Text for row label
     * @param comp Component for row
     */
    private void addRow(JPanel mainDialogPanel, String label, JComponent comp) {
        var l = new JLabel(label);
        mainDialogPanel.add(l);
        l.setLabelFor(comp);
        mainDialogPanel.add(comp);
    }

    /**
     * Create a new Settings dialog window
     * @param parent Parent frame
     * @param settings Settings object
     */
    public SettingsDialog(Frame parent, Settings settings) {
        super(parent, true);
        this.settings = settings;
        setTitle("Settings");

        var contentPane = new JPanel(new BorderLayout());
        var mainLayout = new SpringLayout();
        var mainDialogPanel = new JPanel(mainLayout);
        var buttonPanel = new JPanel(new FlowLayout( FlowLayout.TRAILING));
        var okBtn = new JButton("OK");
        var cancelBtn = new JButton("Cancel");

        okBtn.addActionListener(this::processOk);
        cancelBtn.addActionListener(this::processCancel);
        serverModel.addAll(settings.getServerNames());
        buttonPanel.add(okBtn);
        buttonPanel.add(cancelBtn);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);
        contentPane.add(mainDialogPanel, BorderLayout.CENTER);
        serverLabel.setMinimumSize(new Dimension(300, 0));

        addRow(mainDialogPanel, "Name", name);
        addRow(mainDialogPanel, "Audible Reminder Freq (sec)", reminderFreq);
        addRow(mainDialogPanel, "Server", server);
        addRow(mainDialogPanel, "Server details", serverLabel);

        SpringUtilities.makeCompactGrid(mainDialogPanel, 4, 2, 6, 6, 6, 6);
        setContentPane(contentPane);

        var compListener = new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                super.componentShown(e);
                try {
                    settings.readSettings();
                    updateServers();
                    updateFields();
                    pack();
                    repaint();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        };
        addComponentListener(compListener);

        server.addActionListener( (e) -> {
            setServerLabel(settings.getServer((String) serverModel.getSelectedItem()));
        });
    }

    /**
     * Set value of server label
     * @param serverValue Server value (null for none)
     */
    private void setServerLabel(Settings.Server serverValue) {
        if (serverValue != null) {
            serverLabel.setText(String.format("host: %s port: %d", serverValue.host(), serverValue.port()));
        } else {
            serverLabel.setText("                   ");
        }

    }

    /**
     * Update the fields from settings
     */
    private void updateFields() {
        name.setText(settings.getName());
        if (settings.getAudibleReminderFreq() == null) {
            reminderFreq.setSelectedIndex(0);
        } else {
            reminderFreq.setSelectedIndex( settings.getAudibleReminderFreq() - 1);
        }
        serverModel.setSelectedItem(settings.getSelectedServer());
        setServerLabel(settings.getServer((String) serverModel.getSelectedItem()));
    }

    /**
     * Update servers from settings
     */
    private void updateServers() {
        serverModel.removeAllElements();
        serverModel.addAll(settings.getServerNames());
   }

    /**
     * Update settings from dialog values
     */
    private void updateSettings() {
        var values = settings.getValues();
        values.audibleReminderFreq = reminderFreq.getSelectedIndex() == 0 ? null : Integer.parseInt((String) reminderFreq.getSelectedItem());
        values.name = name.getText();
        values.server = (String) server.getSelectedItem();
        settings.setValues(values);
    }

    /**
     * Process click on OK button
     * @param e Unused
     */
    private void processOk(ActionEvent e) {
        setVisible(false);
        try {
            updateSettings();
            settings.writeSettings();
        } catch (IOException ex) {
            logger.severe("Error writing settings: " + ex.getLocalizedMessage());
        }
    }

    /**
     * Process click on Cancel button
     * @param e Unused
     */
    private void processCancel(ActionEvent e) {
        setVisible(false);
    }

    /**
     * Create and show the dialog as a test
     */
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

    /**
     * Test program
     * @param args Unused
     */
    public static void main(String [] args) {
        SwingUtilities.invokeLater(SettingsDialog::createAndShowGUI);
    }
}
