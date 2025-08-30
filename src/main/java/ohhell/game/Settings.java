package ohhell.game;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;

/**
 * Game settings
 */
public class Settings {
    /** Logger */
    private final static Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass().getPackage().getName());

    /** Path to properties file */
    final private String propertiesFile;

    /** Record for server connection attributes */
    public record Server(String host, int port) {}

    /**
     * Values of game settings
     */
    public static class SettingsValues {
        public SettingsValues() {
            name = "";
            server = "local";
            servers.put("local", new Server("127.0.0.1", 7000));
            servers.put("remote", new Server("ohhell.com", 7000));
            audibleReminderFreq = 10;
        }

        /** Player name */
        public String name;

        /** Server to connect to */
        public String server;

        /** Known servers */
        public Map<String, Server> servers = new HashMap<>();

        /**
         * How often to remind player to play with beep?
         * In ms or null to disable
         */
        public Integer audibleReminderFreq;
    }

    /** Record of settings for Gson library to use */
    private record SettingsJson(SettingsValues settings) {}

    /** Setting values */
    private SettingsJson settings = new SettingsJson(new SettingsValues());

    /** Listener for settings changes */
    private final SettingsListener listener;

    /**
     * Interface for settings change listeners
     */
    @FunctionalInterface
    public interface SettingsListener {
        /**
         * Settings changed
         * @param settings New values
         */
        void settingsChanged(SettingsValues settings);
    }

    /**
     * Create a new Settings object
     * @param propertiesFile Path to settings file
     * @param listener Listener for changes
     */
    public Settings(String propertiesFile, SettingsListener listener) {
        this.propertiesFile = propertiesFile;
        this.listener = listener;
        try {
            readSettings();
        } catch( IOException ex) {
            // Ignore and use defaults on I/O error
        }
    }

    /**
     * Read settings from settings file
     * @throws IOException On I/O error
     */
    public void readSettings() throws IOException {
        Gson gson = new Gson();
        try(BufferedReader rdr = new BufferedReader(new FileReader(propertiesFile))) {
            settings = gson.fromJson(rdr, SettingsJson.class);
        } catch( FileNotFoundException e) {
            // If unable to read file, write out current settings to create file
            writeSettings();
        }
    }

    /**
     * Write settings to file
     * @throws IOException On I/O error
     */
    public void writeSettings() throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(propertiesFile));
            JsonWriter jsonWriter = gson.newJsonWriter(writer)) {
            gson.toJson(settings, SettingsJson.class, jsonWriter);
            logger.info("Wrote settings");
        }
    }

    /**
     * Get player name
     * @return Name of player to send to server
     */
    public String getName() {
        return settings.settings.name;
    }

    /**
     * Get known servers
     * @return Set of known servers
     */
    public Set<String> getServerNames() {
        return settings.settings.servers.keySet();
    }

    /**
     * Get settings for given server
     * @param name Label for server
     * @return Server connection parameters
     */
    public Server getServer(String name) {
        return settings.settings.servers.get(name);
    }

    /**
     * Get selected server label
     * @return Selected server label
     */
    public String getSelectedServer() {
        return settings.settings.server;
    }

    /**
     * Get audible reminder frequency
     * @return Reminder frequency in ms (null if disabled)
     */
    public Integer getAudibleReminderFreq() {
        return settings.settings.audibleReminderFreq;
    }

    /**
     * Get all settings
     * @return Settings
     */
    public SettingsValues getValues() {
        return settings.settings;
    }

    /**
     * Notify listener that settings changed
     */
    private void updateListener() {
        listener.settingsChanged(settings.settings);
    }

    /**
     * Set the player name
     * @param name Player name
     */
    public void setName(String name) {
        var values = settings.settings;
        values.name = name;
        settings = new SettingsJson(values);
        updateListener();
    }

    /**
     * Set the selected server
     * @param server Selected server
     */
    public void setServer(String server) {
        var values = settings.settings;
        values.server = server;
        settings = new SettingsJson(values);
        updateListener();
    }

    /**
     * Set all the settings
     * @param settings New settings
     */
    public void setValues(SettingsValues settings) {
        this.settings = new SettingsJson(settings);
        updateListener();
    }

    public static void main( String [] args) throws IOException {
        var settings = new Settings("/home/pcarter/settings.json", (rec) -> {});
        settings.setName("Paul");
        settings.writeSettings();

        var otherSettings = new Settings("/home/pcarter/settings.json", (rec) -> {});
        otherSettings.readSettings();
        System.out.println("Name: " + otherSettings.getName());
    }
}
