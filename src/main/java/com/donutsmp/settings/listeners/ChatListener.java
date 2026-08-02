package com.donutsmp.settings.listeners;

import com.donutsmp.settings.DonutSettings;
import com.donutsmp.settings.managers.SettingsManager.PlayerSettings;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Set;

public class ChatListener implements Listener {

    private final DonutSettings plugin;

    public ChatListener(DonutSettings plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        PlayerSettings settings = plugin.getSettingsManager().getPlayerSettings(player);

        // If chat is disabled, remove the player from viewers
        if (!settings.isChatEnabled()) {
            Set<Audience> viewers = event.viewers();
            viewers.remove(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        // Placeholder for command output filtering
    }
}
