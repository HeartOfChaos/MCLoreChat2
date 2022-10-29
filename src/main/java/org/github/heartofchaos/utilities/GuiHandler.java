package org.github.heartofchaos.utilities;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.github.heartofchaos.Main;
import org.javacord.api.DiscordApi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GuiHandler implements Listener {

    /*This needs to have a method that when passed the player, opens a gui for them. It needs methods which
    load the main lore screen, which will have a queue. A lore pane on the queue which is clicked is "Selected"
    A spot to place an item where "/lore item" lore will automatically be placed on it, or lore in the queue that is selected.
    It will have a button where they can send lore from an item in that slot to a config-set Discord channel or DMs.
    A button that brings up a chest with all their lore history in it, with buttons to browse left and right
    A button to save the lore on the item to lore history*/

    Main main;
    DiscordApi api;

    public static NamespacedKey blocker;

    public GuiHandler(Main main) {
        this.api = main.getApi();
        this.main = main;

        this.blocker = new NamespacedKey(main, "blocker");

    }

    //We will apply the blocker namespacedkey to any of the glass pane items used in the gui.

    /*
    A button will allow you to click it, which will then highlight it as the currently selected option displayed in the
    center. Different buttons selected will cause different actions to be performed on clicking.

    Buttons for the gui:
    Add to queue: Allows you to click an item and that adds its lore to your queue.
    Erase: Allows you to click a lore pane and remove its lore from your queue.
    Send Lore: Allows you to send lore to either a designated channel or your dms.
     */

    public void updateGui(Player player) {
        UsersAndAccounts dataHandler = new UsersAndAccounts(main);
        player.sendMessage(ChatColor.RED + "Placeholder for actually updating the thingy");
        YamlConfiguration playerConfig = dataHandler.getPlayerConfig(player.getUniqueId());
        //Makes sure it's linked, returning if the player is not linked.
        if (!dataHandler.isLinked(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You aren't linked to a discord account! Use /dlore link to generate a code for linking your account.");
            return;
        }
        YamlConfiguration accountConfig = dataHandler.getAccountConfig(Long.valueOf((int) playerConfig.get("LinkedAccount")));
        ArrayList<ArrayList> playerLoreArray = (ArrayList<ArrayList>) accountConfig.get("LoreQueue");
        Inventory guiWindow = Bukkit.createInventory(null, 54, "Gui");

        for (int i = 0; i < playerLoreArray.size(); i++) {
            ItemStack newItem = new ItemStack(Material.BLAZE_ROD);
            ItemMeta newMeta = newItem.getItemMeta();
            newMeta.setLore(playerLoreArray.get(i));
            newItem.setItemMeta(newMeta);
            guiWindow.addItem(newItem);
        }
        player.openInventory(guiWindow);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

    }

}
