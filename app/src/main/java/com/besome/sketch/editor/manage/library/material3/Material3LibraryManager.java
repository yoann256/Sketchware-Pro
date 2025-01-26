package com.besome.sketch.editor.manage.library.material3;

import android.os.Environment;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileWriter;

import pro.sketchware.utility.FileUtil;

public class Material3LibraryManager {

    private final String sc_id;
    private ConfigData configData;

    public Material3LibraryManager(String sc_id) {
        this.sc_id = sc_id;
        File configFile = getConfigPath();

        if (configFile.exists()) {
            configData = loadConfigData(configFile);
        } else {
            configData = new ConfigData(false, false);
            saveConfigData(configFile, configData);
        }
    }

    public ConfigData getConfigData() {
        return configData;
    }

    public void setConfigData(ConfigData configData) {
        this.configData = configData;
        saveConfigData(getConfigPath(), configData);
    }

    public File getConfigPath() {
        return new File(Environment.getExternalStorageDirectory(),
                ".sketchware" + File.separator + "data" + File.separator + sc_id + File.separator + "material_3.json");
    }

    public ConfigData loadConfigData(File file) {
        String jsonData = FileUtil.readFileIfExist(file.getAbsolutePath());
        if (!jsonData.isEmpty()) {
            try {
                return new Gson().fromJson(jsonData, ConfigData.class);
            } catch (Exception ignored) {
            }
        }
        return new ConfigData(false, false); // Return default config if loading fails
    }

    public void saveConfigData(File file, ConfigData configData) {
        try (FileWriter writer = new FileWriter(file)) {
            new Gson().toJson(configData, writer);
        } catch (Exception ignored) {
        }
    }

    public boolean isMaterial3Enabled() {
        File configFile = getConfigPath();
        if (configFile.exists()) {
            ConfigData configData = loadConfigData(configFile);
            return configData.isMaterial3Enabled();
        }
        return false;
    }

    public boolean isDynamicColorsEnabled() {
        File configFile = getConfigPath();
        if (configFile.exists()) {
            ConfigData configData = loadConfigData(configFile);
            return configData.isMaterial3Enabled() && configData.isDynamicColorsEnabled();
        }
        return false;
    }

    public record ConfigData(boolean isMaterial3Enabled, boolean isDynamicColorsEnabled) {
    }
}