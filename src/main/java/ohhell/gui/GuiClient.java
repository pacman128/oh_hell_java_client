package ohhell.gui;

import ohhell.game.*;
import ohhell.network.AsyncMessageClient;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GuiClient extends JFrame {
    private final static Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass().getPackage().getName());

    private final GameModel model = new GameModel();

    private final MainPanel mainPanel = new MainPanel(model);

    private AsyncMessageClient messageClient;

    private ClientAdapter adapter;

    private final JMenuItem connectItem = new JMenuItem("Connect", KeyEvent.VK_C);

    private final JMenuItem disconnectItem = new JMenuItem("Disconnect", KeyEvent.VK_D);

    private final SettingsDialog settingsDialog;

    private final int timeout = 1000;

    private final Timer networkTask = new Timer(timeout, this::networkProc);


    private static void setLevel(Logger pLogger, Level pLevel) {
        Handler[] handlers = pLogger.getHandlers();
        for (Handler h : handlers) {
            h.setLevel(pLevel);
        }
        pLogger.setLevel(pLevel);
    }

    private static void setLevel(Level pLevel) {
        setLevel(Logger.getLogger(""), pLevel);
    }

    public GuiClient() {
        super("Oh Hell");
        model.addListener(mainPanel);
        model.setUserInput(mainPanel);
        model.addListener(new GameModel.ListenerAdapter() {
            @Override
            public void settingsChanged(String name, String host, int port) {
                handleSettingsChange(name, host, port);
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

    private void handleSettingsChange( String name, String host, int port) {
        SwingUtilities.invokeLater(() -> {
        connectItem.setEnabled(!(name.isBlank() || host.isBlank() || model.getState() == GameModel.State.DISCONNECTED)); } );
    }

    //TODO: Is this needed?
    private void sendCommand(Commands cmd, String... args) throws IOException {
        StringBuilder msg = new StringBuilder( cmd.toString());
        for( String arg: args) {
            msg.append(" ");
            msg.append(arg);
        }
        messageClient.sendMessage(msg.toString(), 1000);
    }

    private void networkProc(ActionEvent e) {
        try {
            adapter.process(100);
            if (model.getState() == GameModel.State.WAITING_FOR_BID_RESPONSE
                || model.getState() == GameModel.State.WAITING_FOR_CARD_RESPONSE) {
                SoundUtils.beep();
            }
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
        setLevel(Logger.getLogger("ohhell"), Level.FINEST);
        logger.finer("FINER level msg");
        Deck.loadCardImages();

        SwingUtilities.invokeLater( () -> {
            var frame = new GuiClient();
            frame.setSize(1000, 800);
            frame.setVisible(true);
        });

    }
}
