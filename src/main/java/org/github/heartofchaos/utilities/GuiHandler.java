package org.github.heartofchaos.utilities;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.github.heartofchaos.Main;
import org.javacord.api.entity.user.User;

import java.util.*;
import java.util.stream.Collectors;

import static java.awt.SystemColor.menu;

public class GuiHandler implements Listener {

    static Main main;


    public GuiHandler(Main main) {
        this.main = main;
    }

    UsersAndAccounts dataHandler = new UsersAndAccounts(main);


    public void updateGui(Player player, ArrayList<ArrayList<String>> loreLists, int page) {
        Inventory inventory = Bukkit.createInventory(player, 36, ChatColor.DARK_GREEN + "Lore Queue");

        // Fill inventory with lore lists
        int itemsPerPage = 27;
        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, loreLists.size());
        for (int i = startIndex; i < endIndex; i++) {
            ArrayList<String> loreList = loreLists.get(i);
            ItemStack item = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(loreList.get(0));
            loreList.remove(0);
            meta.setLore(loreList);
            item.setItemMeta(meta);
            inventory.addItem(item);
        }

        // Add page turning buttons
        ItemStack previousPageButton = new ItemStack(Material.ARROW);
        ItemMeta previousPageButtonMeta = previousPageButton.getItemMeta();
        previousPageButtonMeta.setDisplayName(ChatColor.BLUE + "Previous Page");
        previousPageButton.setItemMeta(previousPageButtonMeta);
        if (page > 1) {
            inventory.setItem(27, previousPageButton);
        }

        ItemStack nextPageButton = new ItemStack(Material.ARROW);
        ItemMeta nextPageButtonMeta = nextPageButton.getItemMeta();
        nextPageButtonMeta.setDisplayName(ChatColor.BLUE + "Next Page");
        nextPageButton.setItemMeta(nextPageButtonMeta);
        if (endIndex < loreLists.size()) {
            inventory.setItem(35, nextPageButton);
        }

        // Calculate number of pages
        int totalPages = (int) Math.ceil((double) loreLists.size() / itemsPerPage);

        // Add page information item
        ItemStack pageInformation = new ItemStack(Material.PAPER);
        ItemMeta pageInformationMeta = pageInformation.getItemMeta();
        pageInformationMeta.setDisplayName(ChatColor.GREEN + "Page " + page + " of " + totalPages);
        pageInformation.setItemMeta(pageInformationMeta);
        inventory.setItem(31, pageInformation);

        // Add refresh item
        ItemStack refreshButton = new ItemStack(Material.WHEAT_SEEDS);
        ItemMeta refreshButtonMeta = refreshButton.getItemMeta();
        refreshButtonMeta.setDisplayName(ChatColor.GOLD + "Refresh");
        refreshButton.setItemMeta(refreshButtonMeta);
        inventory.setItem(30,refreshButton);

        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        Inventory inventory = event.getClickedInventory();
        ArrayList<ArrayList<String>> loreLists = dataHandler.getPlayerLore(player.getUniqueId());


        // If the inventory isn't the lore queue panel, cancels event
        if (inventory == null || clickedItem == null || (!(event.getView().countSlots() == 36)) || (!event.getView().getTitle().equals(ChatColor.DARK_GREEN + "Lore Queue")
                && !event.getView().getTitle().equals(ChatColor.RED + "Set Held Item's Lore - Are you sure?"))) {
            return;
        }

        event.setCancelled(true);

        // Handle previous page button
        if (clickedItem.getType() == Material.ARROW && clickedItem.getItemMeta().getDisplayName().equals(ChatColor.BLUE + "Previous Page")) {
            int currentPage = Integer.parseInt(ChatColor.stripColor(inventory.getItem(31).getItemMeta().getDisplayName()).split(" ")[1]);
            int newPage = currentPage - 1;
            if (newPage < 1) {
                return;
            }
            updateGui(player, loreLists, 1);
            updateGui(player, loreLists, newPage);
        }

        // Handle next page button
        if (clickedItem.getType() == Material.ARROW && clickedItem.getItemMeta().getDisplayName().equals(ChatColor.BLUE + "Next Page")) {
            int currentPage = Integer.parseInt(ChatColor.stripColor(inventory.getItem(31).getItemMeta().getDisplayName()).split(" ")[1]);
            int newPage = currentPage + 1;
            int totalPages = (int) Math.ceil((double) loreLists.size() / 27);
            if (newPage > totalPages) {
                return;
            }
            updateGui(player, loreLists, newPage);
        }

        //Handle refresh button
        if (clickedItem.getType() == Material.WHEAT_SEEDS && clickedItem.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Refresh")) {
            int currentPage = Integer.parseInt(ChatColor.stripColor(inventory.getItem(31).getItemMeta().getDisplayName()).split(" ")[1]);
            updateGui(player, loreLists, currentPage);
        }


        // Handle clicking on a lore pane and copying the lore to the held item
        if (clickedItem.getType() == Material.GREEN_STAINED_GLASS_PANE) {
            ItemStack heldItem = player.getInventory().getItemInMainHand();
            if (heldItem != null && heldItem.getType() != Material.AIR) {
                // Display confirmation screen
                Inventory confirmInventory = Bukkit.createInventory(player, 9, ChatColor.RED + "Set Held Item's Lore - Are you sure?");

                ItemStack confirmButton = new ItemStack(Material.EMERALD);
                ItemMeta confirmButtonMeta = confirmButton.getItemMeta();
                confirmButtonMeta.setDisplayName(ChatColor.GREEN + "Confirm - Set Held Lore");
                confirmButton.setItemMeta(confirmButtonMeta);
                confirmInventory.setItem(3, confirmButton);

                ItemStack cancelButton = new ItemStack(Material.REDSTONE);
                ItemMeta cancelButtonMeta = cancelButton.getItemMeta();
                cancelButtonMeta.setDisplayName(ChatColor.RED + "Cancel - Return to Main Menu");
                cancelButton.setItemMeta(cancelButtonMeta);
                confirmInventory.setItem(5, cancelButton);

                // Store the clicked lore in the confirmation inventory as well
                ItemStack clickedItemCopy = clickedItem.clone();
                clickedItemCopy.setAmount(1);
                clickedItemCopy.setItemMeta(clickedItem.getItemMeta());
                confirmInventory.setItem(0, clickedItemCopy);

                // Store the held item as a separate itemstack for reference
                ItemStack heldItemCopy = heldItem.clone();
                heldItemCopy.setAmount(1);
                heldItemCopy.setItemMeta(heldItem.getItemMeta());
                confirmInventory.setItem(1, heldItemCopy);

                // Open the confirmation inventory
                player.openInventory(confirmInventory);
            }
        }

        // Handle confirmation of setting held item's lore
        if (event.getView().getTitle().equals(ChatColor.RED + "Set Held Item's Lore - Are you sure?")) {
            // Handle confirmation
            if (clickedItem.getType() == Material.EMERALD) {
                ItemStack heldItem = player.getInventory().getItemInMainHand();
                if (heldItem != null && heldItem.getType() != Material.AIR) {
                    // Get the clicked lore from the confirm inventory
                    ItemStack clickedLore = event.getClickedInventory().getItem(0);
                    ItemMeta clickedLoreMeta = clickedLore.getItemMeta();

                    // Set the held item's lore to the clicked lore
                    ItemMeta heldItemMeta = heldItem.getItemMeta();
                    heldItemMeta.setLore(clickedLoreMeta.getLore());
                    heldItemMeta.setDisplayName(clickedLoreMeta.getDisplayName());
                    heldItem.setItemMeta(heldItemMeta);

                    // Close the inventory
                    updateGui(player, loreLists, 1);

                    //Play the cancel sound
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }
            }

            // Handle cancellation
            if (clickedItem.getType() == Material.REDSTONE) {
                updateGui(player, loreLists, 1);
            }
        }
    }

    //I don't think I ever made this work. The initial idea was to add a book you could fill with lore in order to edit lore more easily in-game.
    public void openBookGui(Player player) {
        ItemStack heldItem = player.getInventory().getItemInMainHand();

        // Check if the held item is a writable book with lore
        if (heldItem.getType() == Material.WRITABLE_BOOK && heldItem.hasItemMeta() && heldItem.getItemMeta().hasLore()) {
            // Get the lore of the held item
            List<String> lore = heldItem.getItemMeta().getLore();

            // Build lore string for book and quill page
            StringBuilder loreText = new StringBuilder();
            for (String line : lore) {
                loreText.append(ChatColor.translateAlternateColorCodes('&', line)).append("\n");
            }

            // Create book and quill item
            ItemStack bookAndQuill = new ItemStack(Material.WRITABLE_BOOK);
            BookMeta bookMeta = (BookMeta) bookAndQuill.getItemMeta();
            bookMeta.setTitle("Lore Book");
            bookMeta.setAuthor(player.getName());
            bookMeta.setPages(loreText.toString().split("\n"));
            bookAndQuill.setItemMeta(bookMeta);

            // Register listener for book closing event
            Bukkit.getPluginManager().registerEvents(new Listener() {
                @EventHandler
                public void onPlayerQuit(InventoryCloseEvent event) {
                    Player player = (Player) event.getPlayer();
                    // Check if the player has the lore-editing book in their inventory
                    if (player.getInventory().contains(bookAndQuill)) {
                        // Get the item stack of the lore-editing book
                        ItemStack editingBook = null;
                        for (ItemStack item : player.getInventory().getContents()) {
                            if (item != null && item.isSimilar(bookAndQuill)) {
                                editingBook = item;
                                break;
                            }
                        }
                        // If the lore-editing book is found, remove it and update the lore of the held item
                        if (editingBook != null) {
                            player.getInventory().removeItem(editingBook);
                            ItemMeta itemMeta = heldItem.getItemMeta();
                            itemMeta.setLore(bookAndQuill.getItemMeta().getLore());
                            heldItem.setItemMeta(itemMeta);
                            player.sendMessage(ChatColor.GREEN + "Lore updated!");
                        }
                    }
                }
            }, main);

            // Give the player the lore-editing book
            player.getInventory().addItem(bookAndQuill);
            player.sendMessage(ChatColor.GREEN + "Please open the Lore Book to edit your item's lore.");
        } else {
            player.sendMessage(ChatColor.RED + "You must be holding a writable book with lore to use this command!");
        }
    }


    private List<String> splitLoreIntoPages(List<String> lore) {
        List<String> pages = new ArrayList<>();
        StringBuilder pageBuilder = new StringBuilder();
        int lineCount = 0;
        for (String line : lore) {
            if (lineCount + line.length() > 256) {
                pages.add(pageBuilder.toString());
                pageBuilder = new StringBuilder();
                lineCount = 0;
            }
            pageBuilder.append(line).append("\n");
            lineCount += line.length() + 1;
        }
        pages.add(pageBuilder.toString());
        return pages;
    }

}

