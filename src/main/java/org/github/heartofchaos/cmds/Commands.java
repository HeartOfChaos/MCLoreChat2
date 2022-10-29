package org.github.heartofchaos.cmds;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.github.heartofchaos.Main;
import org.github.heartofchaos.utilities.GuiHandler;
import org.github.heartofchaos.utilities.UserDataHandler;
import org.github.heartofchaos.utilities.UsersAndAccounts;
import org.javacord.api.DiscordApi;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Commands implements CommandExecutor {

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

    public FileConfiguration getConfig(Player player) {
        UserDataHandler dataHandler = new UserDataHandler(main);
        dataHandler.createUser(player);
        return dataHandler.getUserFile(player);
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

        UsersAndAccounts dataHandler = new UsersAndAccounts(main);

        if (args.length <= 0) {
            sender.sendMessage(ChatColor.RED + "WRITE HELP MESSAGE!");
            return true;
        }


        switch (args[0].toLowerCase()) {
            //Command to start the bot.
            case "start":
                if (main.getApi() != null) {
                    sender.sendMessage(ChatColor.RED + "The bot is already active!");
                    return true;
                }
                main.startBot(sender);
                break;
            //Command to stop the bot.
            case "stop":
                if (main.getApi() == null) {
                    sender.sendMessage(ChatColor.RED + "The bot is already not active!");
                    return true;
                }
                main.stopBot(sender);
                break;
            //Command to set the bot token.
            case "token":
                YamlConfiguration config = main.getConfig();

                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Please enter your token.");
                }

                config.set("BotToken", args[1]);

                try {
                    config.save(main.getConfigFile());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            //Command to link the player's account to a discord account.
            case "link":
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
                if ((int) dataHandler.getPlayerConfig(player.getUniqueId()).get("LinkedAccount") > 1) {
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
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                    return true;
                }
                player = (Player) sender;
                GuiHandler guiHandler = new GuiHandler(main);
                //Opens the lore gui for the player
                guiHandler.updateGui(player);
                break;
            //Command to reset the plugin.
            case "reset":
                break;
            //The default result.
            default:
                sender.sendMessage(ChatColor.RED + "Missing arguments! Do /dlore help for help");
                return true;
        }

        return true;
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