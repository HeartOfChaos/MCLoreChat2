package org.github.heartofchaos.utilities;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.github.heartofchaos.Main;
import org.javacord.api.DiscordApi;

import java.io.File;
import java.util.*;

public class UserDataHandler implements Listener {

//    static Main plugin;
//    UUID uuid;
//    File userFile;
//    FileConfiguration userConfig;
//    List<String> blankLore = new ArrayList();
//
//    public static Map<Integer, Player> playerCodes;
//    public static Map<Player, Integer> playerLinks;
//    public static Map<Player, ArrayList<ArrayList>> playerLore;
//    public static Map<Integer, ArrayList> messagePlayer;
//    DiscordApi api;
//
//    public static Map<Integer, Player> playerFromDiscordID = new HashMap<Integer, Player>();
//    public static Map<Player, Integer> discordIDFromPlayer = new HashMap<Player, Integer>();
//
//    public UserDataHandler(Main main) {
//        this.plugin = main;
//        this.api = main.getApi();
//
//        this.playerCodes = main.playerCodes;
//        this.playerLinks = main.playerLinks;
//        this.playerLore = main.playerLore;
//        this.messagePlayer = main.messagePlayer;
//        this.playerFromDiscordID = main.playerFromDiscordID;
//        this.discordIDFromPlayer = main.discordIDFromPlayer;
//    }
//
//    public void createUser(final Player player) {
//        uuid = player.getUniqueId();
//        File userFile = new File(this.plugin.getDataFolder(), uuid + ".yml");
//        if ( !(userFile.exists()) ) {
//
//            try {
//                YamlConfiguration userConfig = YamlConfiguration.loadConfiguration(userFile);
//                userConfig.set(uuid + ".recentLore", blankLore);
//                userConfig.set(uuid + ".discordUser", 0);
//                userConfig.save(userFile);
//
//            } catch (Exception error) {
//                error.printStackTrace();
//            }
//
//        }
//    }
//
//    public static YamlConfiguration getUserFile(Player player) {
//        UUID uuid = player.getUniqueId();
//        File userFile = new File(plugin.getDataFolder(), uuid + ".yml");
//        YamlConfiguration userConfig = YamlConfiguration.loadConfiguration(userFile);
//        return userConfig;
//    }
//
//    public static void saveUserFile(final Player player) {
//        try {
//            UUID uuid = player.getUniqueId();
//            File userFile = new File(plugin.getDataFolder(), uuid + ".yml");
//            YamlConfiguration userConfig = YamlConfiguration.loadConfiguration(userFile);
//            getUserFile(player).save(userFile);
//        } catch (Exception error) {
//            error.printStackTrace();
//        }
//    }
//
//    //A method which adds players if already set and registers a new message with player if not set addMessage(Integer messageID, Player player)
//
//    public static void addMessage(Long messageID, Player player) {
//        YamlConfiguration dataConfig = plugin.getDataConfig();
//        File dataFile = new File(plugin.getDataFolder() + File.separator + "LoreBotDataStorage.yml");
//        ArrayList<Player> playerList = new ArrayList<Player>();
//
//
//        if (!dataConfig.isSet(messageID.toString())) {
//            playerList.add(player);
//            dataConfig.set(messageID.toString(), playerList);
//            try {
//                dataConfig.save(dataFile);
//            } catch (Exception error) {
//                error.printStackTrace();
//            }
//        } else {
//            playerList.addAll((Collection<? extends Player>) dataConfig.get(messageID.toString()));
//            playerList.add(player);
//            dataConfig.set(messageID.toString(), playerList);
//            try {
//                dataConfig.save(dataFile);
//            } catch (Exception error) {
//                error.printStackTrace();
//            }
//        }
//
//    }
//
//    @EventHandler
//    public void joinCreateUser(PlayerJoinEvent event) {
//
//        UsersAndAccounts testConfigHandler = new UsersAndAccounts(plugin);
//        testConfigHandler.generatePlayerFile(event.getPlayer().getUniqueId());
//
//        Player player = event.getPlayer();
//        File userFile = new File(plugin.getDataFolder(), player.getUniqueId() + ".yml");
//        YamlConfiguration userConfig = YamlConfiguration.loadConfiguration(userFile);
//        this.api = plugin.getApi();
//        if (!userConfig.isSet(player.getUniqueId() + ".discordUser")) return;
//            Integer userDiscordID = Integer.parseInt(userConfig.get(player.getUniqueId() + ".discordUser").toString());
//
//            if (api.getUserById(userDiscordID).isCompletedExceptionally()) return;
//            if (playerFromDiscordID.containsKey(userDiscordID) && discordIDFromPlayer.containsKey(player))  return;
//
//            try {
//                    playerFromDiscordID.remove(userDiscordID);
//            } catch (Exception error) {
//
//            } try {
//                    discordIDFromPlayer.remove(player);
//            } catch (Exception error) {
//
//            }
//
//            playerFromDiscordID.put(userDiscordID, player);
//            discordIDFromPlayer.put(player, userDiscordID);
//        }
    }



