package com.donutsmp.settings.listeners;

import com.donutsmp.settings.DonutSettings;
import com.donutsmp.settings.managers.SettingsManager.PlayerSettings;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;

public class AntiPickupListener implements Listener {

    private final DonutSettings plugin;

    public AntiPickupListener(DonutSettings plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemPickup(EntityPickupItemEvent event) {
        // Only handle player pickups
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        PlayerSettings settings = plugin.getSettingsManager().getPlayerSettings(player);

        // If anti-pickup is enabled, cancel the pickup
        if (settings.isAntipickupEnabled()) {
            event.setCancelled(true);
        }
    }
}
