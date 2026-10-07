package io.felixtech.teltonika.tachosync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public final class Settings {
    private static final Logger logger = LoggerFactory.getLogger(Settings.class);
    private static Settings INSTANCE = null;

    public static Settings getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Settings();
        }

        return INSTANCE;
    }

    private static final String PROPERTIES_FILE = "settings.properties";
    private final Properties properties = new Properties();

    private Settings() {
        try (InputStream input = Files.newInputStream(Paths.get(PROPERTIES_FILE))) {
            properties.load(input);
        } catch (IOException ex) {
            logger.error("Unable to load configuration file", ex);
        }
    }

    public String get(String key) {
        return properties.getProperty(key, "");
    }

    public String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}
