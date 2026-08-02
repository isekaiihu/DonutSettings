package com.donutsmp.settings.listeners;

import com.donutsmp.settings.DonutSettings;
import com.donutsmp.settings.managers.SettingsManager.PlayerSettings;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class TPAListener implements Listener {

    private final DonutSettings plugin;

    public TPAListener(DonutSettings plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTPA(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage().toLowerCase();

        // Check if it's a TPA command
        if (!message.startsWith("/tpa ") && !message.startsWith("/tpahere ")) {
            return;
        }

        Player sender = event.getPlayer();
        PlayerSettings senderSettings = plugin.getSettingsManager().getPlayerSettings(sender);

        // If TPA is disabled for the sender
        if (!senderSettings.isTpaEnabled()) {
            event.setCancelled(true);
            sender.sendMessage("§cYou have TPA disabled in your settings!");
            return;
        }

        // Extract target player name
        String[] parts = message.split(" ");
        if (parts.length < 2) {
            return;
        }

        String targetName = parts[1];
        Player target = plugin.getServer().getPlayer(targetName);

        if (target != null) {
            PlayerSettings targetSettings = plugin.getSettingsManager().getPlayerSettings(target);

            // If target has TPA disabled
            if (!targetSettings.isTpaEnabled()) {
                event.setCancelled(true);
                sender.sendMessage("§cThat player has TPA requests disabled!");
                return;
            }

            // If target has TPAuto enabled
            if (targetSettings.isTpautoEnabled()) {
                // Integration with TPA plugin would go here
            }
        }
    }
}
