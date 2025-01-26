package com.besome.sketch.editor.manage.library.material3;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.CompoundButton;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import mod.hey.studios.util.Helper;
import pro.sketchware.databinding.ManageLibraryMaterial3Binding;
import pro.sketchware.utility.FileUtil;

public class Material3LibraryActivity extends AppCompatActivity {

    private ManageLibraryMaterial3Binding binding;
    private String sc_id;

    private record ConfigData(boolean isMaterial3Enabled, boolean isDynamicColorsEnabled) {
    }

    private ConfigData configData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ManageLibraryMaterial3Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        initialize();
    }

    private void initialize() {
        binding.toolbar.setNavigationOnClickListener(Helper.getBackPressedClickListener(this));

        sc_id = getIntent().getStringExtra("sc_id");
        File configFile = getConfigPath(sc_id);

        if (configFile.exists()) {
            configData = loadConfigData(configFile);
        } else {
            configData = new ConfigData(false, false);
            saveConfigData(configFile, configData);
        }

        binding.libSwitch.setChecked(configData.isMaterial3Enabled());
        binding.dynamicColorsSwitch.setChecked(configData.isDynamicColorsEnabled());

        if (!configData.isMaterial3Enabled) {
            binding.dynamicColorsSwitch.setEnabled(false);
        }

        binding.libSwitch.setOnCheckedChangeListener(getOnCheckedChangeListener());
        binding.dynamicColorsSwitch.setOnCheckedChangeListener(getOnCheckedChangeListener());

        binding.layoutSwitchLib.setOnClickListener(view -> binding.libSwitch.setChecked(!binding.libSwitch.isChecked()));
        binding.layoutSwitchDynamicColors.setOnClickListener(view -> {
            if (configData.isMaterial3Enabled())
                binding.dynamicColorsSwitch.setChecked(!binding.dynamicColorsSwitch.isChecked());
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    private CompoundButton.OnCheckedChangeListener getOnCheckedChangeListener() {
        return (buttonView, isChecked) -> {
            boolean isLibEnabled = binding.libSwitch.isChecked();
            boolean isDynamicColorsEnabled = binding.dynamicColorsSwitch.isChecked();
            binding.dynamicColorsSwitch.setEnabled(isLibEnabled);

            configData = new ConfigData(isLibEnabled, isDynamicColorsEnabled);
            saveConfigData(getConfigPath(sc_id), configData);
        };
    }

    private static File getConfigPath(String sc_id) {
        return new File(Environment.getExternalStorageDirectory(),
                ".sketchware" + File.separator + "data" + File.separator + sc_id + File.separator + "material_3.json");
    }

    private static ConfigData loadConfigData(File file) {
        String jsonData = FileUtil.readFile(file.getAbsolutePath());
        if (!jsonData.isEmpty()) {
            try {
                return new Gson().fromJson(jsonData, ConfigData.class);
            } catch (Exception ignored) {
            }
        }
        return new ConfigData(false, false); // Return default config if loading fails
    }

    private static void saveConfigData(File file, ConfigData configData) {
        try (FileWriter writer = new FileWriter(file)) {
            new Gson().toJson(configData, writer);
        } catch (IOException e) {
            Log.e("Material3LibraryActivity", "Error saving config data", e);
        }
    }

    public static boolean isMaterial3Enabled(String sc_id) {
        File configFile = getConfigPath(sc_id);
        if (configFile.exists()) {
            ConfigData configData = loadConfigData(configFile);
            return configData.isMaterial3Enabled();
        }
        return false;
    }

    public static boolean isDynamicColorsEnabled(String sc_id) {
        File configFile = getConfigPath(sc_id);
        if (configFile.exists()) {
            ConfigData configData = loadConfigData(configFile);
            return configData.isMaterial3Enabled() && configData.isDynamicColorsEnabled();
        }
        return false;
    }
}
