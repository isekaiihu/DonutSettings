package com.donutsmp.settings.managers;

import com.donutsmp.settings.DonutSettings;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SettingsManager {

    private final DonutSettings plugin;
    private final Map<UUID, PlayerSettings> playerSettings;
    private final File playerDataFolder;

    public SettingsManager(DonutSettings plugin) {
        this.plugin = plugin;
        this.playerSettings = new HashMap<>();
        this.playerDataFolder = new File(plugin.getDataFolder(), "playerdata");
        
        // Create playerdata folder if it doesn't exist
        if (!playerDataFolder.exists()) {
            playerDataFolder.mkdirs();
        }
    }

    public PlayerSettings getPlayerSettings(Player player) {
        return playerSettings.computeIfAbsent(player.getUniqueId(), k -> loadPlayerSettings(player.getUniqueId()));
    }

    public PlayerSettings getPlayerSettings(UUID uuid) {
        return playerSettings.computeIfAbsent(uuid, this::loadPlayerSettings);
    }

    /**
     * Load player settings from file, or create new with defaults if file doesn't exist
     */
    private PlayerSettings loadPlayerSettings(UUID uuid) {
        File playerFile = getPlayerFile(uuid);
        
        if (playerFile.exists()) {
            // Returning player - load their saved settings
            FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
            return new PlayerSettings(plugin, config, false);
        } else {
            // New player - use defaults from config
            return new PlayerSettings(plugin, null, true);
        }
    }

    /**
     * Save player settings to file
     */
    public void savePlayerSettings(UUID uuid) {
        PlayerSettings settings = playerSettings.get(uuid);
        if (settings == null) return;
        
        File playerFile = getPlayerFile(uuid);
        FileConfiguration config = new YamlConfiguration();
        
        // Save all settings to config
        config.set("chat", settings.isChatEnabled());
        config.set("tpa", settings.isTpaEnabled());
        config.set("tpauto", settings.isTpautoEnabled());
        config.set("antipickup", settings.isAntipickupEnabled());
        config.set("hide", settings.isHideEnabled());
        config.set("glow", settings.isGlowEnabled());
        config.set("fast-crystals", settings.isFastCrystalsEnabled());
        config.set("do-not-disturb", settings.isDndEnabled());
        
        // Save custom settings
        for (Map.Entry<String, Boolean> entry : settings.getCustomSettings().entrySet()) {
            config.set("custom." + entry.getKey(), entry.getValue());
        }
        
        try {
            config.save(playerFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save settings for player " + uuid + ": " + e.getMessage());
        }
    }

    /**
     * Save all player settings (called on plugin disable)
     */
    public void saveAllPlayerSettings() {
        for (UUID uuid : playerSettings.keySet()) {
            savePlayerSettings(uuid);
        }
    }

    /**
     * Remove player settings from memory (but keep on disk)
     */
    public void unloadPlayerSettings(UUID uuid) {
        savePlayerSettings(uuid);
        playerSettings.remove(uuid);
    }

    private File getPlayerFile(UUID uuid) {
        return new File(playerDataFolder, uuid.toString() + ".yml");
    }

    public static class PlayerSettings {
        private final DonutSettings plugin;
        private final Map<String, Boolean> customSettings;

        // Built-in settings
        private boolean chatEnabled;
        private boolean tpaEnabled;
        private boolean tpautoEnabled;
        private boolean antipickupEnabled;
        private boolean hideEnabled;
        private boolean glowEnabled;
        private boolean fastCrystalsEnabled;
        private boolean dndEnabled;

        /**
         * Create player settings
         * @param plugin The plugin instance
         * @param savedConfig The saved config (null for new players)
         * @param isNewPlayer Whether this is a new player (use defaults)
         */
        public PlayerSettings(DonutSettings plugin, FileConfiguration savedConfig, boolean isNewPlayer) {
            this.plugin = plugin;
            this.customSettings = new HashMap<>();

            if (isNewPlayer || savedConfig == null) {
                // New player - load default states from main config
                this.chatEnabled = parseDefault("chat", true);
                this.tpaEnabled = parseDefault("tpa", true);
                this.tpautoEnabled = parseDefault("tpauto", false);
                this.antipickupEnabled = parseDefault("antipickup", false);
                this.hideEnabled = parseDefault("hide", false);
                this.glowEnabled = parseDefault("glow", false);
                this.fastCrystalsEnabled = parseDefault("fast-crystals", false);
                this.dndEnabled = parseDefault("do-not-disturb", false);

                // Load custom settings defaults from config
                if (plugin.getConfig().contains("defaults")) {
                    for (String key : plugin.getConfig().getConfigurationSection("defaults").getKeys(false)) {
                        if (!isBuiltInSetting(key)) {
                            customSettings.put(key, parseDefault(key, false));
                        }
                    }
                }
            } else {
                // Returning player - load from saved file
                this.chatEnabled = savedConfig.getBoolean("chat", parseDefault("chat", true));
                this.tpaEnabled = savedConfig.getBoolean("tpa", parseDefault("tpa", true));
                this.tpautoEnabled = savedConfig.getBoolean("tpauto", parseDefault("tpauto", false));
                this.antipickupEnabled = savedConfig.getBoolean("antipickup", parseDefault("antipickup", false));
                this.hideEnabled = savedConfig.getBoolean("hide", parseDefault("hide", false));
                this.glowEnabled = savedConfig.getBoolean("glow", parseDefault("glow", false));
                this.fastCrystalsEnabled = savedConfig.getBoolean("fast-crystals", parseDefault("fast-crystals", false));
                this.dndEnabled = savedConfig.getBoolean("do-not-disturb", parseDefault("do-not-disturb", false));

                // Load custom settings from saved file
                if (savedConfig.contains("custom")) {
                    for (String key : savedConfig.getConfigurationSection("custom").getKeys(false)) {
                        customSettings.put(key, savedConfig.getBoolean("custom." + key, false));
                    }
                }
            }
        }

        /**
         * Parse default value from config - handles both ON/OFF strings and boolean values
         */
        private boolean parseDefault(String key, boolean fallback) {
            String path = "defaults." + key;
            if (!plugin.getConfig().contains(path)) {
                return fallback;
            }
            
            // Try to get as string first (ON/OFF format)
            Object value = plugin.getConfig().get(path);
            if (value instanceof String) {
                String strValue = ((String) value).trim().toUpperCase();
                return strValue.equals("ON") || strValue.equals("TRUE") || strValue.equals("YES") || strValue.equals("1");
            } else if (value instanceof Boolean) {
                return (Boolean) value;
            }
            
            return fallback;
        }

        private boolean isBuiltInSetting(String key) {
            return key.equalsIgnoreCase("chat") ||
                    key.equalsIgnoreCase("tpa") ||
                    key.equalsIgnoreCase("tpauto") ||
                    key.equalsIgnoreCase("antipickup") ||
                    key.equalsIgnoreCase("hide") ||
                    key.equalsIgnoreCase("glow") ||
                    key.equalsIgnoreCase("fast-crystals") ||
                    key.equalsIgnoreCase("do-not-disturb");
        }

        // Custom settings support
        public Map<String, Boolean> getCustomSettings() {
            return customSettings;
        }

        public boolean getCustomSetting(String key) {
            return customSettings.getOrDefault(key, false);
        }

        public void setCustomSetting(String key, boolean value) {
            customSettings.put(key, value);
        }

        public void toggleCustomSetting(String key) {
            customSettings.put(key, !customSettings.getOrDefault(key, false));
        }

        // Chat
        public boolean isChatEnabled() { return chatEnabled; }
        public void setChatEnabled(boolean enabled) { this.chatEnabled = enabled; }
        public void toggleChat() { this.chatEnabled = !this.chatEnabled; }

        // TPA
        public boolean isTpaEnabled() { return tpaEnabled; }
        public void setTpaEnabled(boolean enabled) { this.tpaEnabled = enabled; }
        public void toggleTpa() { this.tpaEnabled = !this.tpaEnabled; }

        // TPAuto
        public boolean isTpautoEnabled() { return tpautoEnabled; }
        public void setTpautoEnabled(boolean enabled) { this.tpautoEnabled = enabled; }
        public void toggleTpauto() { this.tpautoEnabled = !this.tpautoEnabled; }

        // Anti Pickup
        public boolean isAntipickupEnabled() { return antipickupEnabled; }
        public void setAntipickupEnabled(boolean enabled) { this.antipickupEnabled = enabled; }
        public void toggleAntipickup() { this.antipickupEnabled = !this.antipickupEnabled; }

        // Hide Players
        public boolean isHideEnabled() { return hideEnabled; }
        public void setHideEnabled(boolean enabled) { this.hideEnabled = enabled; }
        public void toggleHide() { this.hideEnabled = !this.hideEnabled; }

        // Glow
        public boolean isGlowEnabled() { return glowEnabled; }
        public void setGlowEnabled(boolean enabled) { this.glowEnabled = enabled; }
        public void toggleGlow() { this.glowEnabled = !this.glowEnabled; }

        // Fast Crystals
        public boolean isFastCrystalsEnabled() { return fastCrystalsEnabled; }
        public void setFastCrystalsEnabled(boolean enabled) { this.fastCrystalsEnabled = enabled; }
        public void toggleFastCrystals() { this.fastCrystalsEnabled = !this.fastCrystalsEnabled; }

        // Do Not Disturb
        public boolean isDndEnabled() { return dndEnabled; }
        public void setDndEnabled(boolean enabled) { this.dndEnabled = enabled; }
        public void toggleDnd() { this.dndEnabled = !this.dndEnabled; }
    }
}
