package ohhell.game;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;

public class Settings {
    private final static Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass().getPackage().getName());

    final private String propertiesFile;

    public record SettingsRec( String name,
                               String host,
                               int port,
                               Integer audibleReminderFreq ) {}
    private record SettingsJson(SettingsRec settings) {}

    private SettingsJson settings = new SettingsJson(new SettingsRec("", "127.0.0.1", 7000, 10));
    private final SettingsListener listener;

    @FunctionalInterface
    public interface SettingsListener {
        void settingsChanged(SettingsRec settings);
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

    public Integer getAudibleReminderFreq() {
        return settings.settings.audibleReminderFreq;
    }

    private void updateListener() {
        listener.settingsChanged(settings.settings);

    }

    public void setName(String name) {
        settings = new SettingsJson(new SettingsRec(name, settings.settings.host, settings.settings.port, settings.settings.audibleReminderFreq));
        updateListener();
    }


    public void setValues(SettingsRec settings) {
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
