package com.banking.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyLoader {

    private static final String PROPERTIES_FILE = "application.properties";

    private static volatile PropertyLoader instance;
    private final Properties properties;


    private PropertyLoader() {
        properties = new Properties();
        loadProperties();
    }

    public static PropertyLoader getInstance() {
        if (instance == null) {
            synchronized (PropertyLoader.class) {
                if (instance == null) {
                    instance = new PropertyLoader();

                }
            }
        }
        return instance;

    }

    private void loadProperties() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            if (inputStream == null) {
                throw new RuntimeException("File not found: " + PROPERTIES_FILE);
            }
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read '"+ PROPERTIES_FILE + "': ",e);
        }
    }

    public String getProperty(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            throw new RuntimeException("Property '" + key + "' is not set");
        }
        return value.trim();
    }

    public String getProperty(String key, String defaultValue) {
        String value = properties.getProperty(key);
        return value != null ? value.trim() : defaultValue;
    }


}
