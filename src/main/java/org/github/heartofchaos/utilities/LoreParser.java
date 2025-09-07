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
        int breakLength = -1; // -1 means no line breaking
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
            } else if (line.split(":")[0].equalsIgnoreCase("break")) {
                try {
                    String[] parts = line.split(":", 2);
                    if (parts.length > 1) {
                        breakLength = Integer.parseInt(parts[1].trim());
                    }
                } catch (NumberFormatException e) {
                    // Ignore invalid integers as requested
                }
                continue;
            } else {
                String processedLine = currentColor.get(0) + parseColors(line);

                // Apply line breaking if breakLength is set
                if (breakLength > 0) {
                    ArrayList<String> brokenLines = breakLine(processedLine, breakLength, currentColor.get(0));
                    newLore.addAll(brokenLines);
                } else {
                    newLore.add(processedLine);
                }
            }
        }
        newLore = ampersandPreserve(newLore, false);

        newLore.set(0, name);
        return newLore;
    }

    // Helper method to break lines at word boundaries, excluding color codes from length calculation
    private static ArrayList<String> breakLine(String line, int maxLength, String colorPrefix) {
        ArrayList<String> result = new ArrayList<>();

        // Strip color codes to get only visible text for length calculation
        String visibleText = stripAllColorCodes(line);

        if (visibleText.length() <= maxLength) {
            result.add(line);
            return result;
        }

        String remainingLine = line;

        while (stripAllColorCodes(remainingLine).length() > maxLength) {
            // Find break point in visible text
            String currentVisible = stripAllColorCodes(remainingLine);
            int visibleBreakPoint = -1;

            // Find the last space within the visible character limit
            for (int i = Math.min(maxLength, currentVisible.length() - 1); i >= 0; i--) {
                if (currentVisible.charAt(i) == ' ') {
                    visibleBreakPoint = i;
                    break;
                }
            }

            // If no space found within limit, break at the limit
            if (visibleBreakPoint == -1) {
                visibleBreakPoint = Math.min(maxLength, currentVisible.length());
            }

            // Find the actual position in the colored string that corresponds to this visible position
            int actualBreakPoint = findActualPositionFromVisible(remainingLine, visibleBreakPoint);

            // Add the broken line (trimmed)
            String brokenPart = remainingLine.substring(0, actualBreakPoint).trim();
            result.add(brokenPart);

            // Prepare remaining text with color prefix
            String remaining = remainingLine.substring(actualBreakPoint).trim();
            if (!remaining.isEmpty()) {
                remainingLine = colorPrefix + remaining;
            } else {
                break;
            }
        }

        // Add the final remaining part if it exists
        String finalVisible = stripAllColorCodes(remainingLine);
        if (!finalVisible.isEmpty()) {
            result.add(remainingLine);
        }

        return result;
    }

    // Helper method to find actual position in colored string based on visible character position
    private static int findActualPositionFromVisible(String coloredString, int visiblePosition) {
        int visibleCount = 0;
        int actualPosition = 0;

        while (actualPosition < coloredString.length() && visibleCount < visiblePosition) {
            if (isColorCodeStart(coloredString, actualPosition)) {
                // Skip the entire color code
                actualPosition += getColorCodeLength(coloredString, actualPosition);
            } else {
                // Regular visible character
                visibleCount++;
                actualPosition++;
            }
        }

        return actualPosition;
    }

    // Helper method to check if position is start of a color code
    private static boolean isColorCodeStart(String str, int pos) {
        if (pos >= str.length()) return false;

        char c = str.charAt(pos);
        if (c == '§' && pos + 1 < str.length()) {
            return true;
        }
        if (c == '&' && pos + 1 < str.length()) {
            char next = str.charAt(pos + 1);
            return "0123456789abcdefklmnorx".indexOf(Character.toLowerCase(next)) != -1;
        }

        return false;
    }

    // Helper method to get the length of a color code starting at position
    private static int getColorCodeLength(String str, int pos) {
        if (pos >= str.length()) return 0;

        char c = str.charAt(pos);
        if (c == '§' && pos + 1 < str.length()) {
            char next = str.charAt(pos + 1);
            if (Character.toLowerCase(next) == 'x') {
                // Hex color code: §x§1§2§3§4§5§6 (14 characters total)
                if (pos + 13 < str.length()) {
                    boolean isHexCode = true;
                    for (int i = 2; i < 14; i += 2) {
                        if (str.charAt(pos + i) != '§') {
                            isHexCode = false;
                            break;
                        }
                    }
                    if (isHexCode) return 14;
                }
            }
            return 2; // Regular color code
        }

        if (c == '&' && pos + 1 < str.length()) {
            char next = str.charAt(pos + 1);
            if (Character.toLowerCase(next) == 'x') {
                // Hex color code: &x&1&2&3&4&5&6 (14 characters total)
                if (pos + 13 < str.length()) {
                    boolean isHexCode = true;
                    for (int i = 2; i < 14; i += 2) {
                        if (str.charAt(pos + i) != '&') {
                            isHexCode = false;
                            break;
                        }
                    }
                    if (isHexCode) return 14;
                }
            }
            return 2; // Regular color code
        }

        return 0;
    }

    // Helper method to strip all color codes (both § and & formats, including hex)
    private static String stripAllColorCodes(String input) {
        if (input == null) return "";

        StringBuilder result = new StringBuilder();
        int i = 0;

        while (i < input.length()) {
            if (isColorCodeStart(input, i)) {
                i += getColorCodeLength(input, i);
            } else {
                result.append(input.charAt(i));
                i++;
            }
        }

        return result.toString();
    }


}
