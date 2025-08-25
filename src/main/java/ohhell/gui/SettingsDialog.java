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

    private final DefaultComboBoxModel<String> serverModel = new DefaultComboBoxModel<>();
    private final JComboBox<String> server = new JComboBox<>(serverModel);
    private final JTextField name = new NameTextField( "", 15);

    private final static String [] freqs = { "None", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
    private final JComboBox<String> reminderFreq = new JComboBox<>(freqs);
    private final JLabel serverLabel = new JLabel();

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
        serverModel.addAll(settings.getServerNames());
        buttonPanel.add(okBtn);
        buttonPanel.add(cancelBtn);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);
        contentPane.add(mainPanel, BorderLayout.CENTER);
        serverLabel.setMinimumSize(new Dimension(300, 0));

        addRow(mainPanel, "Name", name);
        addRow(mainPanel, "Audible Reminder Freq (sec)", reminderFreq);
        addRow(mainPanel, "Server", server);
        addRow(mainPanel, "Server details", serverLabel);

        SpringUtilities.makeCompactGrid(mainPanel, 4, 2, 6, 6, 6, 6);
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

    private void setServerLabel(Settings.Server serverValue) {
        if (serverValue != null) {
            serverLabel.setText(String.format("host: %s port: %d", serverValue.host(), serverValue.port()));
        } else {
            serverLabel.setText("                   ");
        }

    }

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

    private void updateServers() {
        serverModel.removeAllElements();
        serverModel.addAll(settings.getServerNames());
   }

    private void updateSettings() {
        var values = settings.getValues();
        values.audibleReminderFreq = reminderFreq.getSelectedIndex() == 0 ? null : Integer.parseInt((String) reminderFreq.getSelectedItem());
        values.name = name.getText();
        values.server = (String) server.getSelectedItem();
        settings.setValues(values);
        System.out.println(String.format("min size: %s size: %s", serverLabel.getMinimumSize(), serverLabel.getSize()));
        System.out.println(String.format("dailog min: %s size %s", getMinimumSize(), getSize()));
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
