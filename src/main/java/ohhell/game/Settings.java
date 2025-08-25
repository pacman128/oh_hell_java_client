package ohhell.game;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;

public class Settings {
    private final static Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass().getPackage().getName());

    final private String propertiesFile;

    public record Server(String host, int port) {}

    public static class SettingsValues {
        public SettingsValues() {
            name = "";
            server = "local";
            servers.put("local", new Server("127.0.0.1", 7000));
            servers.put("remote", new Server("ohhell.com", 7000));
            audibleReminderFreq = 10;
        }
        public String name;
        public String server;
        public Map<String, Server> servers = new HashMap<>();
        public Integer audibleReminderFreq;
    }

    private record SettingsJson(SettingsValues settings) {}

    private SettingsJson settings = new SettingsJson(new SettingsValues());
    private final SettingsListener listener;

    @FunctionalInterface
    public interface SettingsListener {
        void settingsChanged(SettingsValues settings);
    }

    public Settings(String propertiesFile, SettingsListener listener) {
        this.propertiesFile = propertiesFile;
        this.listener = listener;
        try {
            readSettings();
        } catch( IOException ex) {
            // Ignore
        }
    }

    public void readSettings() throws IOException {
        Gson gson = new Gson();
        try(BufferedReader rdr = new BufferedReader(new FileReader(propertiesFile))) {
            settings = gson.fromJson(rdr, SettingsJson.class);
        } catch( FileNotFoundException e) {
            writeSettings();
        }
    }

    public void writeSettings() throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(propertiesFile));
            JsonWriter jsonWriter = gson.newJsonWriter(writer)) {
            gson.toJson(settings, SettingsJson.class, jsonWriter);
            logger.info("Wrote settings");
        }
    }

    public String getName() {
        return settings.settings.name;
    }

    public Set<String> getServerNames() {
        return settings.settings.servers.keySet();
    }

    public Server getServer(String name) {
        return settings.settings.servers.get(name);
    }

    public String getSelectedServer() {
        return settings.settings.server;
    }

    public Integer getAudibleReminderFreq() {
        return settings.settings.audibleReminderFreq;
    }

    public SettingsValues getValues() {
        return settings.settings;
    }

    private void updateListener() {
        listener.settingsChanged(settings.settings);

    }

    public void setName(String name) {
        var values = settings.settings;
        values.name = name;
        settings = new SettingsJson(values);
        updateListener();
    }

    public void setServer(String server) {
        var values = settings.settings;
        values.server = server;
        settings = new SettingsJson(values);
        updateListener();
    }

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
