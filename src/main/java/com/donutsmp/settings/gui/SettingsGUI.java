package com.donutsmp.settings.gui;

import com.donutsmp.settings.DonutSettings;
import com.donutsmp.settings.managers.SettingsManager.PlayerSettings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class SettingsGUI {

    public static void openGUI(Player player, DonutSettings plugin) {
        String title = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("gui.title", "&8Settings"));
        int size = plugin.getConfig().getInt("gui.size", 36);
        
        // Ensure size is a multiple of 9
        if (size % 9 != 0 || size < 9 || size > 54) {
            size = 36;
        }

        Inventory gui = Bukkit.createInventory(null, size, title);

        // Load buttons from config
        ConfigurationSection buttonsSection = plugin.getConfig().getConfigurationSection("buttons");
        if (buttonsSection != null) {
            PlayerSettings settings = plugin.getSettingsManager().getPlayerSettings(player);
            
            for (String buttonKey : buttonsSection.getKeys(false)) {
                ConfigurationSection button = buttonsSection.getConfigurationSection(buttonKey);
                if (button == null) continue;
                
                if (!button.getBoolean("enabled", true)) continue;
                
                int slot = button.getInt("slot", -1);
                if (slot < 0 || slot >= size) continue;
                
                ItemStack item = createButton(player, buttonKey, button, settings, plugin);
                if (item != null) {
                    gui.setItem(slot, item);
                }
            }
        }

        player.openInventory(gui);
        
        // Update inventory for responsiveness
        player.updateInventory();

        // Play open sound
        ConfigurationSection openSound = plugin.getConfig().getConfigurationSection("gui.open-sound");
        if (openSound != null && openSound.getBoolean("enabled", true)) {
            playSound(player, openSound, plugin);
        }
    }

    private static ItemStack createButton(Player player, String buttonKey, ConfigurationSection button, 
                                          PlayerSettings settings, DonutSettings plugin) {
        boolean isClickable = button.getBoolean("clickable", false);
        boolean isEnabled = isSettingEnabled(buttonKey, settings);

        // Get material
        String materialName;
        if (isClickable) {
            materialName = isEnabled ? 
                    button.getString("material-on", "STONE") : 
                    button.getString("material-off", "STONE");
        } else {
            materialName = button.getString("material", "STONE");
        }

        Material material;
        try {
            material = Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid material: " + materialName + " for button: " + buttonKey);
            material = Material.STONE;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        // Set name
        String name;
        if (isClickable) {
            name = isEnabled ? 
                    button.getString("name-on", buttonKey) : 
                    button.getString("name-off", buttonKey);
        } else {
            name = button.getString("name", buttonKey);
        }
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

        // Set lore
        List<String> loreList;
        if (isClickable) {
            loreList = isEnabled ? 
                    button.getStringList("lore-on") : 
                    button.getStringList("lore-off");
        } else {
            loreList = button.getStringList("lore");
        }

        List<String> coloredLore = new ArrayList<>();
        for (String line : loreList) {
            coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(coloredLore);

        // Add enchant glow if enabled
        boolean shouldGlow;
        if (isClickable) {
            shouldGlow = isEnabled ? 
                    button.getBoolean("enchanted-on", false) : 
                    button.getBoolean("enchanted-off", false);
        } else {
            shouldGlow = button.getBoolean("enchanted", false);
        }

        if (shouldGlow) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);

        return item;
    }

    private static boolean isSettingEnabled(String buttonKey, PlayerSettings settings) {
        switch (buttonKey.toLowerCase()) {
            case "chat":
                return settings.isChatEnabled();
            case "tpa":
                return settings.isTpaEnabled();
            case "tpauto":
                return settings.isTpautoEnabled();
            case "antipickup":
                return settings.isAntipickupEnabled();
            case "hide":
                return settings.isHideEnabled();
            case "glow":
                return settings.isGlowEnabled();
            case "fast-crystals":
                return settings.isFastCrystalsEnabled();
            case "do-not-disturb":
                return settings.isDndEnabled();
            default:
                return settings.getCustomSetting(buttonKey);
        }
    }

    private static void playSound(Player player, ConfigurationSection soundSection, DonutSettings plugin) {
        try {
            String soundName = soundSection.getString("sound", "BLOCK_CHEST_OPEN");
            float volume = (float) soundSection.getDouble("volume", 1.0);
            float pitch = (float) soundSection.getDouble("pitch", 1.0);
            
            Sound sound = Sound.valueOf(soundName.toUpperCase());
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid sound: " + soundSection.getString("sound"));
        }
    }
}
