package com.donutsmp.settings.commands;

import com.donutsmp.settings.DonutSettings;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {

    private final DonutSettings plugin;

    public ReloadCommand(DonutSettings plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("donutsettings.admin")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.no-permission", "&cYou don't have permission to use this!")));
            return true;
        }

        try {
            plugin.reloadPlugin();
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.reload-success", "&aConfiguration reloaded successfully!")));
        } catch (Exception e) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.reload-failed", "&cFailed to reload configuration!")));
            plugin.getLogger().severe("Failed to reload configuration: " + e.getMessage());
        }

        return true;
    }
}
