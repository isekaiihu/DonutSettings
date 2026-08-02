package com.donutsmp.settings.commands;

import com.donutsmp.settings.DonutSettings;
import com.donutsmp.settings.managers.SettingsManager.PlayerSettings;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AntiPickupCommand implements CommandExecutor {

    private final DonutSettings plugin;

    public AntiPickupCommand(DonutSettings plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;
        PlayerSettings settings = plugin.getSettingsManager().getPlayerSettings(player);

        if (args.length == 0) {
            // Toggle mode
            settings.toggleAntipickup();
            plugin.getSettingsManager().savePlayerSettings(player.getUniqueId());
            if (settings.isAntipickupEnabled()) {
                player.sendMessage(ChatColor.GREEN + "Anti Pickup enabled! You will no longer pick up items.");
            } else {
                player.sendMessage(ChatColor.RED + "Anti Pickup disabled! You can now pick up items.");
            }
        } else if (args.length == 1) {
            String arg = args[0].toLowerCase();
            if (arg.equals("on") || arg.equals("enable") || arg.equals("true")) {
                settings.setAntipickupEnabled(true);
                plugin.getSettingsManager().savePlayerSettings(player.getUniqueId());
                player.sendMessage(ChatColor.GREEN + "Anti Pickup enabled! You will no longer pick up items.");
            } else if (arg.equals("off") || arg.equals("disable") || arg.equals("false")) {
                settings.setAntipickupEnabled(false);
                plugin.getSettingsManager().savePlayerSettings(player.getUniqueId());
                player.sendMessage(ChatColor.RED + "Anti Pickup disabled! You can now pick up items.");
            } else {
                player.sendMessage(ChatColor.YELLOW + "Usage: /antipickup [on|off]");
            }
        } else {
            player.sendMessage(ChatColor.YELLOW + "Usage: /antipickup [on|off]");
        }

        return true;
    }
}
