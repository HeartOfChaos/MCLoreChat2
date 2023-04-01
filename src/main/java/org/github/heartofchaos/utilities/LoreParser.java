package org.github.heartofchaos.utilities;

import org.bukkit.ChatColor;
import org.github.heartofchaos.Main;

import java.util.ArrayList;
import java.util.Arrays;

public class LoreParser {

    static String parsedString;
    Main main;

    public LoreParser(Main main) {
        this.main = main;
    }


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

    public static ArrayList<String> parseLoreFormat(String parsedString) {

        ArrayList<String> currentColor = new ArrayList<String>();
        ArrayList<String> lore = ampersandPreserve(new ArrayList<String>(Arrays.asList(parsedString.split("\n"))), true);
        ArrayList<String> newLore = new ArrayList<String>();
        currentColor.add(ChatColor.WHITE + "");
        String name = "";
        newLore.add("");

        for(int i = 0; i < lore.size(); i++) {

            if (lore.get(i).split(":")[0].equalsIgnoreCase("Name")) {
                name = ChatColor.translateAlternateColorCodes('&', lore.get(i).split(":")[1]);
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

        newLore.set(0, name);
        return newLore;
    }
}
