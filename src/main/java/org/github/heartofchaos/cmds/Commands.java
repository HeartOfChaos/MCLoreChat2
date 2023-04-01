package org.github.heartofchaos.cmds;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.github.heartofchaos.Main;
import org.github.heartofchaos.utilities.GuiHandler;
import org.github.heartofchaos.utilities.UsersAndAccounts;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.user.User;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

public class Commands implements CommandExecutor, TabCompleter {

    public Integer randomCode() {
        int randomNum = ThreadLocalRandom.current().nextInt(1000, 9999);
        return randomNum;
    }

    DiscordApi api;
    Main main;

    //Use to get the discord ID of a player. Add to it upon successful password link.
    public static Map<Integer, Player> playerCodes;
    //Use this to get the discord ID of a player
    public static Map<Player, Integer> playerLinks;
    //Add all lore a player sends to an arraylist keyed to their Player.
    //In the arraylist, make the first entry of each a number corresponding to the message ID of the message it's
    // linked to. When a player edits a message, look through that player's playerLore and check all IDs to see if it
    //matches. If it does, then remove it and replace it with the new lore from the edit.
    public static Map<Player, ArrayList<ArrayList>> playerLore;
    //Add the messages a player has linked lore to into this map, and when a message is edited,
    // try to get a key from here and use it to edit the corresponding lore in playerLore.
    public static Map<Integer, ArrayList> messagePlayer;

    public static Map<Integer, Player> playerFromDiscordID = new HashMap<Integer, Player>();
    public static Map<Player, Integer> discordIDFromPlayer = new HashMap<Player, Integer>();
    UsersAndAccounts dataHandler = new UsersAndAccounts(main);
    public FileConfiguration getConfig(Player player) {
        dataHandler.generatePlayerFile(player.getUniqueId());
        return dataHandler.getPlayerConfig(player.getUniqueId());
    }

    public Commands(Main main) {
        this.api = main.getApi();
        this.main = main;

        this.playerCodes = main.playerCodes;
        this.playerLinks = main.playerLinks;
        this.playerLore = main.playerLore;
        this.messagePlayer = main.messagePlayer;
        this.playerFromDiscordID = main.playerFromDiscordID;
        this.discordIDFromPlayer = main.discordIDFromPlayer;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("mclore.dlore") && !sender.hasPermission("mclore.*")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission!");
            return true;
        }

        UsersAndAccounts dataHandler = new UsersAndAccounts(main);
        List<String> helpOptions = Arrays.asList("/dlore gui: Opens an interactive gui to add lore to the item held in your hand.",
                "/dlore token [token]: Sets the token of the bot for connecting to Discord. Remember to keep your bot token private.",
                "/dlore start: Starts the bot if it has a valid token, connecting it to Discord.",
                "/dlore stop: Stops the bot, disconnecting it from Discord. Can be started again.",
                "/dlore link: Gives you a four-digit code which you can DM the bot to link your Discord account.",
                "/dlore clear: Clears all of the lore you've added to your lore queue. Can't be reversed!",
                "/dlore set: Sets the lore of the item in your hand to the latest lore in your lore queue.",
                "/dlore send: Sends the lore of the item in your hand to your DMs.");


        if (args.length <= 0) {
            sender.sendMessage(ChatColor.GREEN + String.join("\n" + ChatColor.GREEN, helpOptions));
            return true;
        }

        long discordID;

        switch (args[0].toLowerCase()) {
            //Command to start the bot.
            case "start":
                if (!sender.hasPermission("mclore.start") && !sender.hasPermission("mclore.*")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission!");
                    return true;
                }
                if (main.getApi() != null) {
                    sender.sendMessage(ChatColor.RED + "The bot is already active!");
                    return true;
                }
                main.startBot(sender);
                break;
            //Command to stop the bot.
            case "stop":
                if (!sender.hasPermission("mclore.stop") && !sender.hasPermission("mclore.*")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission!");
                    return true;
                }
                if (main.getApi() == null) {
                    sender.sendMessage(ChatColor.RED + "The bot is already not active!");
                    return true;
                }
                main.stopBot(sender);
                sender.sendMessage(ChatColor.GREEN + "Bot stopped!");
                break;
            //Command to set the bot token.
            case "token":
                if (!sender.hasPermission("mclore.token") && !sender.hasPermission("mclore.*")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission!");
                    return true;
                }
                YamlConfiguration config = main.getConfig();

                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Please enter your token.");
                    break;
                }

                config.set("BotToken", args[1]);

                try {
                    config.save(main.getConfigFile());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                sender.sendMessage(ChatColor.GREEN + "Your token has been set!");
                break;
            //Command to link the player's account to a discord account.
            case "link":
                if (!sender.hasPermission("mclore.link") && !sender.hasPermission("mclore.*")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission!");
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                    return true;
                }
                if (main.getApi() == null) {
                    sender.sendMessage(ChatColor.RED + "The bot has not been started!");
                    return true;
                }

                Player player = (Player) sender;
                String completionMessage = "";
                Integer generatedCode = randomCode();
                Integer oldCode = 0;
                YamlConfiguration dataConfig = dataHandler.getGlobalData();

                //Removes any previous codes that exist for the player running the command.
                if (dataConfig.isSet("playerCodes." + player.getUniqueId())) {
                    sender.sendMessage(ChatColor.RED + "Generating new key!");
                    oldCode = dataConfig.getInt("playerCodes." + player.getUniqueId());
                    dataHandler.removeCode(Long.valueOf(oldCode));
                }
                //Makes certain that this code is not a repeat.
                while (dataHandler.isCodeValid(generatedCode)) {
                    generatedCode = randomCode();
                }
                //Unlinks the player's account if they're already linked to a discord account
                if (dataHandler.isLinked(player.getUniqueId())) {
                    dataHandler.removeLink(player.getUniqueId());
                    player.sendMessage(ChatColor.RED + "Your account has been unlinked from the Discord bot!");
                }
                //Adds the code to globaldata, which the discord bot can then reference for linking them
                dataHandler.addCode(generatedCode, player.getUniqueId());
                sender.sendMessage(ChatColor.GREEN + "Your code is: " + ChatColor.YELLOW + Integer.toString(generatedCode) +
                        ChatColor.GREEN + ". Do not share this with anyone! Use /dlore link to make a new code. Send this code to " +
                        ChatColor.YELLOW + main.getApi().getYourself().getDiscriminatedName() + ChatColor.GREEN + "in a dm to link your account.");

                break;
            //Command to open the lore gui
            case "gui":
                if (!sender.hasPermission("mclore.gui") && !sender.hasPermission("mclore.*")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission!");
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                    return true;
                }
                player = (Player) sender;
                if (!dataHandler.isLinked(player.getUniqueId())) {
                    sender.sendMessage(ChatColor.RED + "Your account must be linked to use this command!");
                    return true;
                }
                GuiHandler guiHandler = new GuiHandler(main);
                //Opens the lore gui for the player
                guiHandler.updateGui(player, dataHandler.getPlayerLore(player.getUniqueId()), 1);
                break;
            //Command to your lore queue
            case "clear":
                if (!sender.hasPermission("mclore.clear") && !sender.hasPermission("mclore.*")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission!");
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                    return true;
                }
                player = (Player) sender;
                if (!dataHandler.isLinked(player.getUniqueId())) {
                    sender.sendMessage(ChatColor.RED + "You are not linked to a discord account!");
                    return true;
                }
                discordID = dataHandler.getLinkedAccount(player.getUniqueId());
                YamlConfiguration accountConfig = dataHandler.getAccountConfig(discordID);
                File accountFile = dataHandler.getAccountFile(discordID);
                ArrayList<ArrayList<String>> newArray = new ArrayList<ArrayList<String>>();

                accountConfig.set("LoreQueue", newArray);
                dataHandler.saveConfig(accountFile, accountConfig);
                sender.sendMessage(ChatColor.GOLD + "Your lore queue has been emptied!");
                break;
            //The default result.
            case "set":
                if (!sender.hasPermission("mclore.set") && !sender.hasPermission("mclore.*")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission!");
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                    return true;
                }

                player = (Player) sender;
                ItemStack heldItem = player.getInventory().getItemInMainHand();
                ItemMeta heldMeta = heldItem.getItemMeta();
                ArrayList<ArrayList<String>> loreArray = dataHandler.getPlayerLore(player.getUniqueId());

                if (loreArray.size() <= 0) {
                    player.sendMessage(ChatColor.RED + "You do not have any lore in your queue!");
                    break;
                }
                ArrayList<String> newLore = loreArray.get(0);
                String name = "";
                name = newLore.get(0);
                newLore.remove(0);
                heldMeta.setLore(newLore);
                heldMeta.setDisplayName(name);
                heldItem.setItemMeta(heldMeta);
                player.sendMessage(ChatColor.GREEN + "Lore set!");
                break;
            case "send":
                if (!sender.hasPermission("mclore.send") && !sender.hasPermission("mclore.*")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission!");
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "Only players can use this command!");
                    return true;
                }
                player = (Player) sender;
                this.api = main.getApi();

                if (!dataHandler.isLinked(player.getUniqueId())) {
                    player.sendMessage(ChatColor.RED + "You are not linked to the Discord!");
                    return true;
                }
                discordID = dataHandler.getLinkedAccount(player.getUniqueId());
                CompletableFuture<User> userFuture = api.getUserById(discordID);
                userFuture.thenApply(user -> {
                    ItemStack itemInHand = player.getInventory().getItemInMainHand();
                    if (itemInHand.getType().isAir()) {
                        player.sendMessage(ChatColor.RED + "You must be holding an item to use this command!");
                        return null;
                    }
                    user.sendMessage("Name:" + itemInHand.getItemMeta().getDisplayName().replaceAll("§", "&") + System.lineSeparator() +
                            String.join(System.lineSeparator(), itemInHand.getItemMeta().getLore()).replaceAll("§", "&"));
                    player.sendMessage(ChatColor.GREEN + "Item lore sent to your DMs!");
                    return null;
                }).exceptionally(e -> {
                    player.sendMessage(ChatColor.RED + "Error: Could not find user with the ID your account is linked to.");
                    return null;
                });
                break;
            case "help":
                sender.sendMessage(ChatColor.GREEN + String.join("\n" + ChatColor.GREEN, helpOptions));
                break;
            case "ebook":
                player = (Player) sender;
                guiHandler = new GuiHandler(main);
                guiHandler.openBookGui(player);
                player.sendMessage(ChatColor.GREEN + "Book editor opened!");
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Missing arguments! Do /dlore help for help");
                return true;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("dlore")) {
            if (args.length == 1) {
                return Arrays.asList("start", "stop", "token", "link", "gui", "clear", "set", "send","ebook", "help");
            }
        }
        return null;
    }

}
//Old code for linking command
/*//Removes the previous key if a user already has a linking key.
                if (playerLinks.containsKey(player) || discordIDFromPlayer.containsKey(player)) {
                    sender.sendMessage(ChatColor.RED + "Generating new key!");
                    oldCode = playerLinks.get(player);
                    playerCodes.remove(playerLinks.get(player));
                    playerLinks.remove(oldCode);
                    //Change to UsersAndAccounts method to remove GlobalData.playerCodes.DiscordID code from playerCodes. Make a method to set
                    playerFromDiscordID.remove(discordIDFromPlayer.get(player));
                    discordIDFromPlayer.remove(player);
                }

                while (playerCodes.containsKey(generatedCode)) {
                    generatedCode = randomCode();
                }

                //Add a six-digit code to the playerCodes map
                playerCodes.put(generatedCode, player);
                playerLinks.put(player, generatedCode);*/