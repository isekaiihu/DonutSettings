package com.donutsmp.settings.listeners;

import com.donutsmp.settings.DonutSettings;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerDataListener implements Listener {

    private final DonutSettings plugin;

    public PlayerDataListener(DonutSettings plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Load player settings on join (creates with defaults if new player)
        plugin.getSettingsManager().getPlayerSettings(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Save and unload player settings on quit
        plugin.getSettingsManager().unloadPlayerSettings(event.getPlayer().getUniqueId());
    }
}
