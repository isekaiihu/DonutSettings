package com.donutsmp.settings;

import com.donutsmp.settings.commands.AntiPickupCommand;
import com.donutsmp.settings.commands.ReloadCommand;
import com.donutsmp.settings.commands.SettingsCommand;
import com.donutsmp.settings.listeners.AntiPickupListener;
import com.donutsmp.settings.listeners.ChatListener;
import com.donutsmp.settings.listeners.GUIListener;
import com.donutsmp.settings.listeners.PlayerDataListener;
import com.donutsmp.settings.listeners.TPAListener;
import com.donutsmp.settings.managers.SettingsManager;
import org.bukkit.plugin.java.JavaPlugin;

public class DonutSettings extends JavaPlugin {

    private SettingsManager settingsManager;

    @Override
    public void onEnable() {
        // Save default config
        saveDefaultConfig();

        // Initialize managers
        this.settingsManager = new SettingsManager(this);

        // Register commands
        getCommand("settings").setExecutor(new SettingsCommand(this));
        getCommand("settingsreload").setExecutor(new ReloadCommand(this));
        getCommand("antipickup").setExecutor(new AntiPickupCommand(this));

        // Register listeners
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new TPAListener(this), this);
        getServer().getPluginManager().registerEvents(new AntiPickupListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDataListener(this), this);

        getLogger().info("DonutSettings has been enabled!");
        getLogger().info("========================================");
        getLogger().info("         PLUGIN BY ISekai");
        getLogger().info("         Discord: akumasekai");
        getLogger().info("========================================");
    }

    @Override
    public void onDisable() {
        // Save all player settings before disabling
        if (settingsManager != null) {
            settingsManager.saveAllPlayerSettings();
        }
        getLogger().info("DonutSettings has been disabled!");
    }

    public SettingsManager getSettingsManager() {
        return settingsManager;
    }

    public void reloadPlugin() {
        reloadConfig();
        getLogger().info("Configuration reloaded!");
    }
}
