package ohhell.gui;

import ohhell.Util;
import ohhell.game.*;
import ohhell.network.AsyncMessageClient;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GuiClient extends JFrame {
    private final static Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass().getPackage().getName());

    private final static String VERSION = "1.0";

    private final GameModel model = new GameModel();

    private final MainPanel mainPanel = new MainPanel(model);

    private AsyncMessageClient messageClient;

    private ClientAdapter adapter;

    private final JMenuItem connectItem = new JMenuItem("Connect", KeyEvent.VK_C);

    private final JMenuItem disconnectItem = new JMenuItem("Disconnect", KeyEvent.VK_D);

    private final SettingsDialog settingsDialog;

    private final int timeout = 1000;

    private final Timer networkTask = new Timer(timeout, this::networkProc);

    public GuiClient() {
        super("Oh Hell");
        model.addListener(mainPanel);
        model.setUserInput(mainPanel);
        model.addListener(new GameModel.ListenerAdapter() {
            @Override
            public void settingsChanged(Settings.SettingsRec settings) {
                handleSettingsChange(settings);
            }
        });
        networkTask.setCoalesce(true);
        settingsDialog = new SettingsDialog(this, model.getSettings());
        makeMenus();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainPanel.setOpaque(true);
        setContentPane(mainPanel);
    }

    private void makeMenus() {
        var menuBar = new JMenuBar();
        var game = new JMenu("Game");
        connectItem.addActionListener(this::handleConnect);
        disconnectItem.addActionListener( (e) -> { handleDisconnect(); });
        game.add(connectItem);
        game.add(disconnectItem);
        disconnectItem.setEnabled(false);
        game.addSeparator();
        var settings = new JMenuItem("Settings", KeyEvent.VK_S);
        settings.addActionListener(this::handleSettings);
        game.add(settings);
        menuBar.add(game);
        var help = new JMenu("Help");
        var about = new JMenuItem("About", KeyEvent.VK_A);
        about.addActionListener(this::handleAbout);
        help.add(about);
        menuBar.add(help);
        setJMenuBar(menuBar);
    }

    private void handleConnect(ActionEvent e) {
        try {
            var settings = model.getSettings();
            connect(settings.getHost(), settings.getPort());
            connectItem.setEnabled(false);
            disconnectItem.setEnabled(true);
        } catch (Exception ex) {
            logger.severe("Connection error: " + ex.getLocalizedMessage());
        }
    }

    private void handleDisconnect() {
        try {
            messageClient.close();
            disconnectItem.setEnabled(false);
            connectItem.setEnabled(true);
            model.setState(GameModel.State.DISCONNECTED);
            networkTask.stop();
        } catch (IOException ex) {
            logger.severe("Disconnect error: " + ex.getLocalizedMessage());
        }
    }

    private void handleSettings(ActionEvent e) {
        settingsDialog.setVisible(true);
    }

    private void handleAbout(ActionEvent e) {
        JOptionPane.showMessageDialog(this,
                "Oh Hell Client version " + VERSION,
                "About",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleSettingsChange( final Settings.SettingsRec settings) {
        SwingUtilities.invokeLater(() -> {
        connectItem.setEnabled( !(settings.name().isBlank()
                                  || settings.host().isBlank()
                                  || model.getState() != GameModel.State.DISCONNECTED)); } );
    }

    private void networkProc(ActionEvent e) {
        try {
            adapter.process(100);
            mainPanel.processUserHints();
        } catch (IOException ex) {
            mainPanel.log("Error: " + ex.getLocalizedMessage());
            logger.severe(ex.getLocalizedMessage());
            handleDisconnect();
        }
    }

    public void connect(String host, int port) throws IOException {
        messageClient = new AsyncMessageClient(host, port, msg -> {});
        messageClient.connect(1000);
        adapter = new ClientAdapter(messageClient, model, mainPanel::log);
        networkTask.start();
    }

    public static void main( String [] args) throws IOException {
        if (args.length > 0) {
            switch(args[0]) {
                case "quiet":
                    Util.setLevel(Logger.getLogger("ohhell"), Level.WARNING);
                    break;
                case "debug":
                    Util.setLevel(Logger.getLogger("ohhell"), Level.FINER);
                    break;
                case "detailed":
                    Util.setLevel(Logger.getLogger("ohhell"), Level.FINEST);
                    break;
                default:
                    System.err.println("Unknown option: " + args[0]);
                    break;
            }
        }

        Deck.loadCardImages();

        SwingUtilities.invokeLater( () -> {
            var frame = new GuiClient();
            frame.setSize(1000, 800);
            frame.setVisible(true);
        });

    }
}
