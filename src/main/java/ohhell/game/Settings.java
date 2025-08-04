package ohhell.game;

import java.io.*;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;

public class Settings {
    private final static Logger logger = Logger.getLogger("ohhell.game.Settings");

    final private String propertiesFile;

    private record SettingsRec( String name, String host, int port) {}
    private record SettingsJson(SettingsRec settings) {}

    private SettingsJson settings = new SettingsJson(new SettingsRec("", "127.0.0.1", 7000));
    private final SettingsListener listener;

    public interface SettingsListener {
        void settingsChanged(String name, String host, int port);
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

    public boolean readSettings() throws IOException {
        Gson gson = new Gson();
        try(BufferedReader rdr = new BufferedReader(new FileReader(propertiesFile))) {
            settings = gson.fromJson(rdr, SettingsJson.class);
            return true;
        } catch( FileNotFoundException e) {
            writeSettings();
        }
        return false;
    }

    public void writeSettings() throws IOException {
        Gson gson = new Gson();
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(propertiesFile));
            JsonWriter jsonWriter = gson.newJsonWriter(writer)) {
            gson.toJson(settings, SettingsJson.class, jsonWriter);
            logger.info("Wrote settings");
        }
    }

    public String getName() {
        return settings.settings.name;
    }

    public String getHost() {
        return settings.settings.host;
    }

    public int getPort() {
        return settings.settings.port;
    }

    private void updateListener() {
        listener.settingsChanged(settings.settings.name, settings.settings.host, settings.settings.port);

    }

    public void setName(String name) {
        settings = new SettingsJson(new SettingsRec(name, settings.settings.host, settings.settings.port));
        updateListener();
    }

    public void setHost(String host) {
        settings = new SettingsJson(new SettingsRec(settings.settings.name, host, settings.settings.port));
        updateListener();
    }

    public void setPort(int port) {
        settings = new SettingsJson(new SettingsRec(settings.settings.name, settings.settings.host, port));
        updateListener();
    }

    public void setValues(String name, String host, int port) {
        settings = new SettingsJson(new SettingsRec(name, host, port));
        updateListener();
    }

    public static void main( String [] args) throws IOException {
        var settings = new Settings("/home/pcarter/settings.json", (n, h, p) -> {});
        settings.setName("Paul");
        settings.writeSettings();

        var otherSettings = new Settings("/home/pcarter/settings.json", (n, h, p) -> {});
        otherSettings.readSettings();
        System.out.println("Name: " + otherSettings.getName());
    }
}
