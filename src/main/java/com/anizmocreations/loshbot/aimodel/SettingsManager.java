package com.anizmocreations.loshbot.aimodel;

import com.anizmocreations.loshbot.entity.SystemSettings;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class SettingsManager {
    private static final String SETTINGS_FILE = "system_settings.json";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private SystemSettings settings;

    public SettingsManager() {
        load();
    }

    public SystemSettings getSettings() {
        return settings;
    }

    public void update(SystemSettings newSettings) {
        this.settings = newSettings;
        applySettings();
        save();
    }

    private void applySettings() {
        if (settings.getCacheProvider() != null) {
            System.setProperty("loshbot.cache-provider", settings.getCacheProvider());
        }
    }

    private void load() {
        File file = new File(SETTINGS_FILE);
        if (file.exists()) {
            try {
                settings = objectMapper.readValue(file, SystemSettings.class);
            } catch (IOException e) {
                settings = new SystemSettings();
            }
        } else {
            settings = new SystemSettings();
        }
        applySettings();
    }

    private void save() {
        try {
            objectMapper.writeValue(new File(SETTINGS_FILE), settings);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
