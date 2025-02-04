package com.example.foodordersystem;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {
    private static final String CONFIG_FILE = "config.properties";

    public static Properties loadConfig() {
        Properties properties = new Properties();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try (InputStream input = classLoader.getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                throw new RuntimeException("Unable to find config.properties file.");
            }
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception according to your application's error handling mechanism
        }
        return properties;
    }
}
