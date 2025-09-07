package org.github.heartofchaos.utilities;

import org.bukkit.ChatColor;
import org.github.heartofchaos.Main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoreParser {

    static String parsedString;
    Main main;

    public LoreParser(Main main) {
        this.main = main;
    }

    // If true, set "\&" to PLACEHOLDERAMPERSAND, if false set PLACEHOLDERAMPERSAND to "&"
    public static ArrayList<String> ampersandPreserve(ArrayList<String> strings, Boolean mode) {
        for (int i = 0; i < strings.size(); i++) {
            if (mode) {
                strings.set(i, strings.get(i).replaceAll("-&", "PLACEHOLDERAMPERSAND"));
            } else {
                strings.set(i, strings.get(i).replaceAll("PLACEHOLDERAMPERSAND", "&"));
            }
        }
        return strings;
    }


    public static String parseColors(String input) {
        if (input == null) return "";

        Pattern hexPattern = Pattern.compile("(?i)&#([0-9A-F]{6})");
        Matcher matcher = hexPattern.matcher(input);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String hex = matcher.group(1);
            String replacement = toSectionHex(hex);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        input = sb.toString();


        Pattern altHexPattern = Pattern.compile("(?i)&x((?:&[0-9A-F]){6})");
        matcher = altHexPattern.matcher(input);
        sb = new StringBuffer();

        while (matcher.find()) {
            String grouped = matcher.group(1);
            String hex = grouped.replaceAll("&", "");
            String replacement = toSectionHex(hex);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        input = sb.toString();

        //Legacy codes
        return ChatColor.translateAlternateColorCodes('&', input);
    }

    private static String toSectionHex(String hex) {
        StringBuilder out = new StringBuilder("\u00A7x");
        for (char c : hex.toCharArray()) {
            out.append('\u00A7').append(c);
        }
        return out.toString();
    }

    public static ArrayList<String> parseLoreFormat(String parsedString) {

        ArrayList<String> currentColor = new ArrayList<>();
        ArrayList<String> lore = ampersandPreserve(new ArrayList<>(Arrays.asList(parsedString.split("\n"))), true);
        ArrayList<String> newLore = new ArrayList<>();
        currentColor.add(ChatColor.WHITE + "");
        String name = "";
        newLore.add("");

        for (int i = 0; i < lore.size(); i++) {
            String line = lore.get(i);

            if (line.split(":")[0].equalsIgnoreCase("Name")) {
                name = parseColors(line.split(":", 2)[1]);
                continue;
            } else if (line.startsWith(">")) {
                currentColor.clear();
                currentColor.add(parseColors(line.replaceFirst(">", "")));
                continue;
            } else {
                newLore.add(currentColor.get(0) + parseColors(line));
            }
        }
        newLore = ampersandPreserve(newLore, false);

        newLore.set(0, name);
        return newLore;
    }
}
