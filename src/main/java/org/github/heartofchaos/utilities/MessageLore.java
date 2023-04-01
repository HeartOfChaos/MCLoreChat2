package org.github.heartofchaos.utilities;

import org.bukkit.ChatColor;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.configuration.serialization.SerializableAs;
import org.github.heartofchaos.Main;
import org.javacord.api.entity.message.Message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@SerializableAs("MessageLore")
public class MessageLore implements ConfigurationSerializable {


    //If true, set "\&" to PLACEHOLDERAMPERSAND, if false set PLACEHOLDERAMPERSAND to "&"
    public static ArrayList<String> ampersandPreserve(ArrayList<String> strings, Boolean mode) {
        for (int i = 0; i < strings.size(); i++) {
            if (mode) {
                strings.get(i).replaceAll("-&", "PLACEHOLDERAMPERSAND");
            } else {
                strings.get(i).replaceAll("PLACEHOLDERAMPERSAND", "&");
            }
        }
        return strings;
    }

    Main main;
    String itemName;
    ArrayList<String> itemLore = new ArrayList<String>();
    long messageID;

    Message message;

    public MessageLore(Main main, Message message) {
        this.main = main;
        this.message = message;
        String parsedString = message.getContent();
        //Creates an array list which will store the string to use when a player enters > before a line.
        ArrayList<String> currentColor = new ArrayList<String>();
        //This is the lore that the game will run over and check to see what each line of it is supposed to be on the item.
        ArrayList<String> lore = ampersandPreserve(new ArrayList<String>(Arrays.asList(parsedString.split("\n"))), true);
        //This is what will be passed as the "Lore" value to the MessageLore object.
        ArrayList<String> newLore = new ArrayList<String>();
        //Sets the default lore color to white.
        currentColor.add(ChatColor.WHITE + "");
        //The "name" value for the MessageLore object.
        String name = "";
        newLore.add("");

        for(int i = 0; i < lore.size(); i++) {

            if (lore.get(i).split(":")[0].equalsIgnoreCase("Name")) {
                itemName = ChatColor.translateAlternateColorCodes('&', lore.get(i).split(":")[1]);
                continue;
            } else if (lore.get(i).startsWith(">")) {
                currentColor.clear();
                currentColor.add(ChatColor.translateAlternateColorCodes('&', lore.get(i).replaceFirst(">", "")));
                continue;
            } else {
                newLore.add(currentColor.get(0) + lore.get(i));
            }
        }
        newLore = ampersandPreserve(newLore, false);
        itemLore = newLore;
        itemName = name;
        messageID = message.getId();

    }

    //Getters
    public ArrayList<String> getItemLore() {
        return this.itemLore;
    }
    public long getMessageID() {
        return messageID;
    }
    public String getItemName() {
        return itemName;
    }
    public Message getMessage() {
        return message;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("message", message);
        return map;
    }

    public MessageLore deserialize(Map<String, Object> map) {
        return new MessageLore(this.main, (Message) map.get("message"));
    }
}
