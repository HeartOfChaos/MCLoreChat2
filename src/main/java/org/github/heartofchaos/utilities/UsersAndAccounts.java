package org.github.heartofchaos.utilities;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.github.heartofchaos.Main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class UsersAndAccounts {

    /*

    User: A config named after a UUID, with a config file storing which account they're linked to.
    - Stored under DataFolder/PlayerAccounts
    - Name of the config is the User's UUID.
    - Contained under Account.Linked is the string of numbers is the DiscordID. Used for DiscordIDFromPlayer
    -
    -
    Account: A config file named under the discord ID of the account used to store the account's queued lore.
    - Stored in DataFolder/DiscordAccounts
    - Contains an arraylist of arraylists of Lore.
    - LinkedPlayers.[UUID], an entry for every linked player.
    - PlayerFromDiscordID: Provide an array of all players linked to this account. Used for interaction commands
      /lore viewlinks and /lore unlink [username to UUID]
    - QueuedLore.Lore.[DiscordMessageID] contains an arraylist used for Lore, with the first line being the displayname.
      Whenever a discord message is edited, it edits the corresponding QueuedLore.[DiscorMessageID] entry.
    -
    GlobalData:
    - Sets playerCodes.[discordID] to the UUID of the player who runs /lore link
    -
    -
    Methods in this class:
    - GetLinkedAccount(UUID): Returns the linked account, returns null if no linked
      account, returns a config file for the account if the value is set.
      to the User.
    - GetLinkedPlayers(DiscordID): Returns an arraylist of UUIDs linked to the Discord Account for every
      Account: LinkedPlayers entry
    - GetUserLore(DiscordID): Returns an arraylist with all entries from QueuedLore.Lore.
    - GenerateGlobalData:
      Creates a config named GlobalData with the following values:
       - PlayerCodes.[LINKINGCODE].[DiscordID] (For PlayerCodes.DiscordID)
       -
       -
    - XGenerateAccountFile(DiscordID):
      -
    - XGeneratePlayerFile(UUID):
      -
    - LinkPlayer(UUID, DiscordID): Adds the UUID to DiscordID.linkedPlayers.[UUID]
    -

    Problems?
    -
    -
    -
    -
     */



    static Main main;

    public UsersAndAccounts(Main main) {
        this.main = main;
    }

    public void generatePlayerFile(UUID uuid) {
        File playerFile = new File(main.getDataFolder() + File.separator + "PlayerAccounts", uuid.toString() + ".yml");
        YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);

        if (playerFile.exists()) return;
        playerConfig.set("LinkedAccount", 0);
        try {
            playerConfig.save(playerFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public File getPlayerFile(UUID uuid) {
        generatePlayerFile(uuid);
        File playerFile = new File(main.getDataFolder() + File.separator + "PlayerAccounts", uuid.toString() + ".yml");
        return playerFile;
    }

    public YamlConfiguration getPlayerConfig(UUID uuid) {
        generatePlayerFile(uuid);
        File playerFile = new File(main.getDataFolder() + File.separator + "PlayerAccounts", uuid.toString() + ".yml");
        YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
        return playerConfig;
    }

    public void generateAccountFile(long discordID) {
        File accountFile = new File(main.getDataFolder() + File.separator + "DiscordAccounts", discordID + ".yml");
        YamlConfiguration accountConfig = YamlConfiguration.loadConfiguration(accountFile);
        ArrayList players = new ArrayList<>();
        ArrayList<ArrayList> lore = new ArrayList<>();

        if (accountFile.exists()) return;
        accountConfig.set("LinkedPlayers", players);
        accountConfig.set("LoreQueue", lore);
        try {
           accountConfig.save(accountFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public File getAccountFile(long discordID) {
        generateAccountFile(discordID);
        File accountFile = new File(main.getDataFolder() + File.separator + "DiscordAccounts", discordID + ".yml");
        return accountFile;
    }

    public YamlConfiguration getAccountConfig(long discordID) {
        generateAccountFile(discordID);
        File accountFile = new File(main.getDataFolder() + File.separator + "DiscordAccounts", discordID + ".yml");
        YamlConfiguration accountConfig = YamlConfiguration.loadConfiguration(accountFile);
        return accountConfig;
    }

    public ArrayList getLinkedUsers(long discordID) {
        File accountFile = new File(main.getDataFolder() + File.separator + "DiscordAccounts", discordID + ".yml");
        YamlConfiguration accountConfig = YamlConfiguration.loadConfiguration(accountFile);
        Set linkedPlayers = accountConfig.getConfigurationSection("LinkedPlayers").getKeys(false);
        ArrayList results = new ArrayList<>();
        results.addAll(linkedPlayers);

        return results;
    }

    public long getLinkedAccount(UUID uuid) {
        generatePlayerFile(uuid);
        YamlConfiguration playerConfig = getPlayerConfig(uuid);
        if (!playerConfig.isSet("LinkedAccount")) return 0;
        long discordID =  Long.valueOf((int) playerConfig.get("LinkedAccount"));

        return discordID;
    }

    public void generateGlobalData() {
        File dataFile = new File(main.getDataFolder(), "GlobalData.yml");
        YamlConfiguration dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        if (dataFile.exists()) return;

        try {
            dataConfig.save(dataFile);
        }catch (Exception error) {
            error.printStackTrace();
        }
    }

    public YamlConfiguration getGlobalData() {
        generateGlobalData();
        File dataFile = new File(main.getDataFolder(), "GlobalData.yml");
        return YamlConfiguration.loadConfiguration(dataFile);
    }

    public File getGlobalDataFile() {
        File dataFile = new File(main.getDataFolder(), "GlobalData.yml");
        return dataFile;
    }

    public void addCode(long code, UUID uuid) {
        generateGlobalData();
        File dataFile = getGlobalDataFile();
        YamlConfiguration dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        dataConfig.set("codes." + code, uuid.toString());
        dataConfig.set("playerCodes." + uuid, code);
        try {
            dataConfig.save(dataFile);
        } catch (Exception error) {
            error.printStackTrace();
        }
    }

    public boolean isCodeValid(long code) {
        //Gives us the variables for the DataConfig
        generateGlobalData();
        File dataFile = getGlobalDataFile();
        YamlConfiguration dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        if (!dataConfig.isSet("codes." + code)) return false;
        return true;
    }

    //The "long" here is the generated code which points to the path we're trying to remove.
    public void removeCode(long code) {
        //This makes sure that the config file we're trying to read and edit exists.
        generateGlobalData();
        File dataFile = getGlobalDataFile();
        YamlConfiguration dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        //This is a guard statement making sure that the long someone sent us is a valid code.
        if (!dataConfig.isSet("codes." + code)) return;
        //This points to the UUID of the player who originally generated this code, so that it can be removed.
        String existingUUID = (String) dataConfig.get("codes." + code);
        /*Here, both values are removed from the config. Except only the "playerCodes." entry is removed. I even tried
        to make "code" here a String variable since existingUUID is a string variable, but that did not work.*/
        dataConfig.set("playerCodes." + existingUUID, null);
        dataConfig.set("codes." + code, null);
        try {
            dataConfig.save(dataFile);
        } catch (Exception error) {
            error.printStackTrace();
        }
    }

    public boolean isLinked(UUID uuid) {
        if ((int) getPlayerConfig(uuid).get("LinkedAccount") > 1) {
           return true;
        }
        return false;
    }

    public void removeLink(UUID uuid) {
        long discordID = Long.valueOf((int) getPlayerConfig(uuid).get("LinkedAccount"));
        File accountFile = getAccountFile(discordID);
        YamlConfiguration accountConfig = getAccountConfig(discordID);
        File playerFile = getPlayerFile(uuid);
        YamlConfiguration playerConfig = getPlayerConfig(uuid);
        //Sets the value of both linked accounts related to this UUID to null
        accountConfig.set("LinkedPlayers." + uuid.toString(), null);
        playerConfig.set("LinkedAccount", 0);
        //Saves the changes made to the config
        saveConfig(accountFile, accountConfig);
        saveConfig(playerFile, playerConfig);
    }

    /*Use this to add the UUID to Account.LinkedPlayers, remove the code from GlobalData, and set the account id to the
    Player config under LinkedAccount*/
    public void linkCode(long discordID, UUID uuid, long code) {
        if (!isCodeValid(code)) return;
        //Gives us the variables for the DataConfig
        generateGlobalData();
        File dataFile = getGlobalDataFile();
        YamlConfiguration dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        //Gives us the variables for the Account config
        generateAccountFile(discordID);
        YamlConfiguration accountConfig = getAccountConfig(discordID);
        File accountFile = getAccountFile(discordID);
        //Gives us the variables for the player config
        generatePlayerFile(uuid);
        YamlConfiguration playerConfig = getPlayerConfig(uuid);
        File playerFile = getPlayerFile(uuid);

        //Adds the UUID to Account.LinkedPlayers
        accountConfig.set("LinkedPlayers", uuid.toString());
        //Removes the code from GlobalData
        this.removeCode(code);
        //Sets the LinkedAccount under PlayerConfig
        playerConfig.set("LinkedAccount", discordID);
        //Saves all configs
        try {
            dataConfig.save(dataFile);
            playerConfig.save(playerFile);
            accountConfig.save(accountFile);
        } catch (Exception error) {
            error.printStackTrace();
        }
    }

    public void saveConfig(File file, YamlConfiguration config) {
        try {
            config.save(file);
        } catch (Exception error) {
            error.printStackTrace();
        }
    }

}
