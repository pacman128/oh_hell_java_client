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

/**
 * Main Oh Hell graphical client
 */
public class GuiClient extends JFrame {
    /** logger */
    private final static Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass().getPackage().getName());

    /** Version string */
    private final static String VERSION = "1.0";

    /** Game model */
    private final GameModel model = new GameModel();

    /** Main panel for UI */
    private final MainPanel mainPanel = new MainPanel(model);

    /** Message client to communicate with server */
    private AsyncMessageClient messageClient;

    /** Message processor that translates server messages to ClientProtocol */
    private MessageProcessor messageProcessor;

    /** Connect menu item */
    private final JMenuItem connectItem = new JMenuItem("Connect", KeyEvent.VK_C);

    /** Disconnect menu item */
    private final JMenuItem disconnectItem = new JMenuItem("Disconnect", KeyEvent.VK_D);

    /** Settings dialog window */
    private final SettingsDialog settingsDialog;

    /** Time to wait for events (ms) */
    private final int timeout = 1000;

    /** GUI task for events like blinking */
    private final javax.swing.Timer guiTask = new javax.swing.Timer(timeout, (e) -> {mainPanel.processUserHints(); });

    /** Network I/O task */
    private final NetworkTask networkTask;

    /**
     * Create a new GUI client window
     */
    public GuiClient() {
        super("Oh Hell");
        model.addListener(mainPanel);
        model.setUserInput(mainPanel);
        model.addListener(new GameModel.ListenerAdapter() {
            @Override
            public void settingsChanged(Settings.SettingsValues settings) {
                handleSettingsChange(settings);
            }
        });

        networkTask = new NetworkTask(this::handleDisconnect, 100);
        guiTask.setCoalesce(true);
        guiTask.start();
        settingsDialog = new SettingsDialog(this, model.getSettings());
        makeMenus();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainPanel.setOpaque(true);
        setContentPane(mainPanel);
    }

    /**
     * Create the menu items
     */
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

    /**
     * Handle a connect menu request
     * @param e Unused event
     */
    private void handleConnect(ActionEvent e) {
        try {
            var settings = model.getSettings();
            var server = settings.getServer(settings.getSelectedServer());

            // Connect to the server
            connect(server.host(), server.port());

            // Update menu
            connectItem.setEnabled(false);
            disconnectItem.setEnabled(true);
        } catch (Exception ex) {
            logger.severe("Connection error: " + ex.getLocalizedMessage());
        }
    }

    /**
     * Handle server disconnect
     */
    private void handleDisconnect() {
        try {
            messageClient.close();

            // Update menu
            disconnectItem.setEnabled(false);
            connectItem.setEnabled(true);
            model.setState(GameModel.State.DISCONNECTED);
       } catch (IOException ex) {
            logger.severe("Disconnect error: " + ex.getLocalizedMessage());
        }
    }

    /**
     * Handle menu request for changing settings
     * @param e Unused event
     */
    private void handleSettings(ActionEvent e) {
        // Make settings dialog visible
        settingsDialog.setVisible(true);
    }

    /**
     * Handle About menu request
     * @param e Unused
     */
    private void handleAbout(ActionEvent e) {
        JOptionPane.showMessageDialog(this,
                "Oh Hell Client version " + VERSION,
                "About",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Handle change to settings
     * @param settings New settings values
     */
    private void handleSettingsChange( final Settings.SettingsValues settings) {
        // TODO: Isn't this already in the UI thread?
        SwingUtilities.invokeLater(() -> {
        connectItem.setEnabled( !(settings.name.isBlank()
                                  || settings.server.isBlank()
                                  || model.getState() != GameModel.State.DISCONNECTED)); } );
    }

    /**
     * Log a message to log window
     * @param msg Message to log
     */
    private void log(String msg) {
        SwingUtilities.invokeLater( () -> { mainPanel.log(msg); } );
    }

    /**
     * Connect to the server
     * @param host Server host address
     * @param port Server port
     * @throws IOException On I/O error
     */
    public void connect(String host, int port) throws IOException {
        // Create and connect message client
        messageClient = new AsyncMessageClient(host, port, msg -> {});
        messageClient.connect(1000);

        // Create message processor
        messageProcessor = new MessageProcessor(messageClient, new GuiDecorator(model, networkTask,3000), this::log);

        // Start network task
        networkTask.start(messageProcessor);
    }

    /**
     * Main program
     * @param args Command line arguments
     * @throws IOException On I/O error
     */
    public static void main( String [] args) throws IOException {
        // Look for log level command line argument
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

        // Preload the card images
        Deck.loadCardImages();

        // Start up the UI
        SwingUtilities.invokeLater( () -> {
            var frame = new GuiClient();
            frame.setSize(1200, 900);
            frame.setVisible(true);
        });

    }
}
