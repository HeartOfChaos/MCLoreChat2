package org.github.heartofchaos.cmds;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.github.heartofchaos.Main;
import org.github.heartofchaos.utilities.UsersAndAccounts;
import org.javacord.api.DiscordApi;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.util.*;

public class DiscordCommands implements MessageCreateListener {

    Main main;
    DiscordApi api;

    public DiscordCommands(Main main) {
        this.api = main.getApi();
        this.main = main;
    }

    @Override
    public void onMessageCreate(MessageCreateEvent event) {

        UsersAndAccounts dataHandler = new UsersAndAccounts(main);

        try {
            if (dataHandler.isCodeValid(Integer.parseInt(event.getMessageContent()))) {

                Integer playerCode = Integer.parseInt(event.getMessageContent());
                Long authorID = Long.valueOf(event.getMessage().getAuthor().getId());
                UUID playerUUID = UUID.fromString((String) dataHandler.getGlobalData().get("codes." + playerCode));
                Player player = main.getServer().getPlayer(playerUUID);
                dataHandler.linkCode(authorID, playerUUID, playerCode);
                dataHandler.removeCode(Long.valueOf(playerCode));
                event.getChannel().sendMessage("Account linked! Code deleted. Repeat this process to link to a new account.");
                player.sendMessage(ChatColor.GREEN + "Your account has been linked to " + ChatColor.YELLOW +
                        event.getMessageAuthor().getDiscriminatedName() + ChatColor.GREEN + " on discord! Use /dlore link to unlink this account.");
                return;

            }
        } catch (NumberFormatException error) {

        }
    }
}

//Old Hashmap-using code
/* playerFromDiscordID.put(authorID, playerCodes.get(playerCode));
                discordIDFromPlayer.put(playerCodes.get(playerCode), authorID);
                Player player = playerFromDiscordID.get(authorID);

                YamlConfiguration playerConfig = getConfig(player);
                File userFile = new File(main.getDataFolder(), player.getUniqueId() + ".yml");

                playerLinks.remove(playerCodes.get(playerCode));
                playerCodes.remove(playerCode);

                playerConfig.set(player.getUniqueId() + ".discordUser", authorID);

                try {
                    playerConfig.save(userFile);
                } catch (Exception error) {
                    error.printStackTrace();
                }*/