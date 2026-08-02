package com.donutsmp.settings.listeners;

import com.donutsmp.settings.DonutSettings;
import com.donutsmp.settings.gui.SettingsGUI;
import com.donutsmp.settings.managers.SettingsManager.PlayerSettings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GUIListener implements Listener {

    private final DonutSettings plugin;
    private final Map<UUID, Long> cooldowns;

    public GUIListener(DonutSettings plugin) {
        this.plugin = plugin;
        this.cooldowns = new HashMap<>();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        String guiTitle = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("gui.title", "&8Settings"));

        // Check if it's our GUI
        String viewTitle = ChatColor.stripColor(event.getView().getTitle());
        String expectedTitle = ChatColor.stripColor(guiTitle);

        if (!viewTitle.equals(expectedTitle)) {
            return;
        }

        event.setCancelled(true);

        int slot = event.getRawSlot();
        if (slot < 0) {
            return;
        }

        // Find which button was clicked
        ConfigurationSection buttonsSection = plugin.getConfig().getConfigurationSection("buttons");
        if (buttonsSection == null) {
            return;
        }

        for (String buttonKey : buttonsSection.getKeys(false)) {
            ConfigurationSection button = buttonsSection.getConfigurationSection(buttonKey);
            if (button == null) continue;

            if (!button.getBoolean("enabled", true)) continue;

            int buttonSlot = button.getInt("slot", -1);
            if (buttonSlot != slot) continue;

            // Check if button is clickable
            if (!button.getBoolean("clickable", false)) {
                return;
            }

            // Check permission
            String permission = button.getString("permission", "");
            if (!permission.isEmpty() && !player.hasPermission(permission)) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        plugin.getConfig().getString("messages.no-button-permission",
                                "&cYou don't have permission to toggle this setting!")));
                return;
            }

            // Check cooldown
            if (isOnCooldown(player)) {
                int remaining = getCooldownRemaining(player);
                String cooldownMsg = plugin.getConfig().getString("messages.cooldown-active",
                        "&cPlease wait before toggling again!");
                cooldownMsg = cooldownMsg.replace("{time}", String.valueOf(remaining));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', cooldownMsg));
                return;
            }

            // Handle the button click
            handleButtonClick(player, buttonKey, button);

            // Apply cooldown
            applyCooldown(player);

            // Refresh GUI with slight delay for responsiveness
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                SettingsGUI.openGUI(player, plugin);
                player.updateInventory();
            }, 1L);

            break;
        }
    }

    private void handleButtonClick(Player player, String buttonKey, ConfigurationSection button) {
        PlayerSettings settings = plugin.getSettingsManager().getPlayerSettings(player);

        // Toggle the setting
        boolean newState = toggleSetting(player, buttonKey, settings);

        // Save settings immediately after toggle
        plugin.getSettingsManager().savePlayerSettings(player.getUniqueId());

        // Execute command if specified
        String command = newState ? button.getString("command-on", "") : button.getString("command-off", "");
        if (command != null && !command.isEmpty()) {
            Bukkit.dispatchCommand(player, command);
        }

        // Play sound
        ConfigurationSection soundSection = newState ?
                button.getConfigurationSection("sound-on") :
                button.getConfigurationSection("sound-off");
        if (soundSection != null && soundSection.getBoolean("enabled", false)) {
            playSound(player, soundSection);
        } else {
            // Fallback to click sound
            ConfigurationSection clickSound = plugin.getConfig().getConfigurationSection("gui.click-sound");
            if (clickSound != null && clickSound.getBoolean("enabled", true)) {
                playSound(player, clickSound);
            }
        }

        // Play particles
        ConfigurationSection particleSection = newState ?
                button.getConfigurationSection("particle-on") :
                button.getConfigurationSection("particle-off");
        if (particleSection != null && particleSection.getBoolean("enabled", false)) {
            playParticles(player, particleSection);
        }

        // Send messages with customizable destinations
        sendMessages(player, button, newState);
    }

    /**
     * Send messages to customizable destinations (title, subtitle, actionbar, chat)
     * Use -1 to disable a specific destination
     */
    private void sendMessages(Player player, ConfigurationSection button, boolean newState) {
        // Get message config for this button
        String messageKey = newState ? "message-on" : "message-off";
        
        // Check for new format with destinations
        ConfigurationSection messageSection = button.getConfigurationSection(messageKey);
        
        if (messageSection != null) {
            // New format: message-on: { title: "...", subtitle: "...", actionbar: "...", chat: "..." }
            String titleMsg = messageSection.getString("title", "-1");
            String subtitleMsg = messageSection.getString("subtitle", "-1");
            String actionbarMsg = messageSection.getString("actionbar", "-1");
            String chatMsg = messageSection.getString("chat", "-1");
            
            // Handle title and subtitle together
            if (!titleMsg.equals("-1") || !subtitleMsg.equals("-1")) {
                String finalTitle = titleMsg.equals("-1") ? "" : ChatColor.translateAlternateColorCodes('&', titleMsg);
                String finalSubtitle = subtitleMsg.equals("-1") ? "" : ChatColor.translateAlternateColorCodes('&', subtitleMsg);
                sendTitle(player, finalTitle, finalSubtitle);
            }
            
            // Handle actionbar
            if (!actionbarMsg.equals("-1") && !actionbarMsg.isEmpty()) {
                player.sendActionBar(Component.text(ChatColor.translateAlternateColorCodes('&', actionbarMsg)));
            }
            
            // Handle chat
            if (!chatMsg.equals("-1") && !chatMsg.isEmpty()) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', chatMsg));
            }
        } else {
            // Old format: message-on: "text" (backwards compatible)
            String message = button.getString(messageKey, "");
            if (message == null || message.isEmpty() || message.equals("-1")) {
                return;
            }
            
            message = ChatColor.translateAlternateColorCodes('&', message);
            
            // Use global settings for where to send
            boolean useActionbar = plugin.getConfig().getBoolean("features.use-actionbar", false);
            boolean useTitles = plugin.getConfig().getBoolean("features.use-titles.enabled", true);
            
            if (useTitles) {
                sendTitle(player, message, "");
            } else if (useActionbar) {
                player.sendActionBar(Component.text(message));
            } else {
                player.sendMessage(message);
            }
        }
    }

    private void sendTitle(Player player, String title, String subtitle) {
        int fadeIn = plugin.getConfig().getInt("features.use-titles.fade-in", 10);
        int stay = plugin.getConfig().getInt("features.use-titles.stay", 40);
        int fadeOut = plugin.getConfig().getInt("features.use-titles.fade-out", 10);

        Title.Times times = Title.Times.times(
                Duration.ofMillis(fadeIn * 50L),
                Duration.ofMillis(stay * 50L),
                Duration.ofMillis(fadeOut * 50L)
        );

        Title titleObj = Title.title(
                Component.text(title),
                Component.text(subtitle),
                times
        );

        player.showTitle(titleObj);
    }

    private boolean toggleSetting(Player player, String buttonKey, PlayerSettings settings) {
        switch (buttonKey.toLowerCase()) {
            case "chat":
                settings.toggleChat();
                return settings.isChatEnabled();
            case "tpa":
                settings.toggleTpa();
                return settings.isTpaEnabled();
            case "tpauto":
                settings.toggleTpauto();
                return settings.isTpautoEnabled();
            case "antipickup":
                settings.toggleAntipickup();
                return settings.isAntipickupEnabled();
            case "hide":
                settings.toggleHide();
                return settings.isHideEnabled();
            case "glow":
                settings.toggleGlow();
                return settings.isGlowEnabled();
            case "fast-crystals":
                settings.toggleFastCrystals();
                return settings.isFastCrystalsEnabled();
            case "do-not-disturb":
                settings.toggleDnd();
                return settings.isDndEnabled();
            default:
                // Handle custom settings
                settings.toggleCustomSetting(buttonKey);
                return settings.getCustomSetting(buttonKey);
        }
    }

    private void playSound(Player player, ConfigurationSection soundSection) {
        try {
            String soundName = soundSection.getString("sound", "UI_BUTTON_CLICK");
            float volume = (float) soundSection.getDouble("volume", 1.0);
            float pitch = (float) soundSection.getDouble("pitch", 1.0);
            
            Sound sound = Sound.valueOf(soundName.toUpperCase());
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid sound: " + soundSection.getString("sound"));
        }
    }

    private void playParticles(Player player, ConfigurationSection particleSection) {
        try {
            String particleName = particleSection.getString("particle", "VILLAGER_HAPPY");
            int count = particleSection.getInt("count", 10);
            
            Particle particle = Particle.valueOf(particleName.toUpperCase());
            player.getWorld().spawnParticle(particle, player.getLocation().add(0, 1, 0), count, 0.5, 0.5, 0.5, 0);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid particle: " + particleSection.getString("particle"));
        }
    }

    private boolean isOnCooldown(Player player) {
        if (!plugin.getConfig().getBoolean("features.toggle-cooldown.enabled", true)) {
            return false;
        }
        
        Long lastUse = cooldowns.get(player.getUniqueId());
        if (lastUse == null) {
            return false;
        }
        
        int cooldownSeconds = plugin.getConfig().getInt("features.toggle-cooldown.duration", 2);
        return System.currentTimeMillis() - lastUse < cooldownSeconds * 1000L;
    }

    private int getCooldownRemaining(Player player) {
        Long lastUse = cooldowns.get(player.getUniqueId());
        if (lastUse == null) {
            return 0;
        }
        
        int cooldownSeconds = plugin.getConfig().getInt("features.toggle-cooldown.duration", 2);
        long remaining = (cooldownSeconds * 1000L) - (System.currentTimeMillis() - lastUse);
        return (int) Math.ceil(remaining / 1000.0);
    }

    private void applyCooldown(Player player) {
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }
}
