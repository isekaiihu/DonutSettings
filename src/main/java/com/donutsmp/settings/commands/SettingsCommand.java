package com.donutsmp.settings.commands;

import com.donutsmp.settings.DonutSettings;
import com.donutsmp.settings.gui.SettingsGUI;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SettingsCommand implements CommandExecutor {

    private final DonutSettings plugin;

    public SettingsCommand(DonutSettings plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("donutsettings.use")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.no-permission", "&cYou don't have permission to use this!")));
            return true;
        }

        SettingsGUI.openGUI(player, plugin);
        return true;
    }
}
