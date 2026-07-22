package com.example.backend.util;

import com.example.backend.model.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ============================================================================
 * UTILITY CLASS: JsonUtils.java
 * ============================================================================
 * JAVA LEARNING NOTE - Utilities & String Parsing:
 * A utility class contains static helper methods. You don't need to create an object (`new JsonUtils()`)
 * to call static methods; you invoke them directly using `JsonUtils.methodName()`.
 * 
 * WHY WRITE THIS WITHOUT THIRD-PARTY LIBRARIES?
 * By using standard Java Regular Expressions (Regex) and String operations, this server can compile 
 * and run on ANY machine with standard Java installed without downloading external Maven/Gradle packages!
 */
public class JsonUtils {

    // Private constructor prevents creating instances of this pure utility class
    private JsonUtils() {}

    /**
     * Converts a List of Item objects into a JSON Array string.
     * Example output: [{"id":1,"name":"..."}, {"id":2,"name":"..."}]
     * 
     * @param items List of Item objects
     * @return Formatted JSON Array string
     */
    public static String itemListToJson(List<Item> items) {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        for (int i = 0; i < items.size(); i++) {
            sb.append("  ").append(items.get(i).toJson());
            if (i < items.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Parses a raw JSON Array string into a Java List<Item>.
     * Used when reading initial_data.json on server startup.
     * 
     * @param jsonArrayStr Raw JSON string representing an array of objects
     * @return List of parsed Item objects
     */
    public static List<Item> parseJsonArray(String jsonArrayStr) {
        List<Item> items = new ArrayList<>();
        if (jsonArrayStr == null || jsonArrayStr.trim().isEmpty()) {
            return items;
        }

        // Match individual JSON objects `{...}` inside the array
        Pattern objectPattern = Pattern.compile("\\{[^{}]*\\}");
        Matcher matcher = objectPattern.matcher(jsonArrayStr);

        while (matcher.find()) {
            String objectStr = matcher.group();
            Item item = parseJsonObject(objectStr);
            if (item != null) {
                items.add(item);
            }
        }
        return items;
    }

    /**
     * Parses a single JSON Object string `{ "id": 1, "name": "...", ... }` into an Item Java object.
     * Used when processing incoming POST request bodies from clients/Android apps.
     * 
     * @param jsonStr Single JSON object string
     * @return Parsed Item object or null if parsing fails
     */
    public static Item parseJsonObject(String jsonStr) {
        if (jsonStr == null || jsonStr.trim().isEmpty()) {
            return null;
        }

        try {
            Item item = new Item();

            // Extract "id" (number)
            String idStr = extractValue(jsonStr, "id");
            if (idStr != null) {
                item.setId(Integer.parseInt(idStr.trim()));
            }

            // Extract "name" (string)
            String nameStr = extractValue(jsonStr, "name");
            if (nameStr != null) {
                item.setName(cleanStringValue(nameStr));
            }

            // Extract "category" (string)
            String categoryStr = extractValue(jsonStr, "category");
            if (categoryStr != null) {
                item.setCategory(cleanStringValue(categoryStr));
            }

            // Extract "price" (double / number)
            String priceStr = extractValue(jsonStr, "price");
            if (priceStr != null) {
                item.setPrice(Double.parseDouble(priceStr.trim()));
            }

            // Extract "inStock" (boolean)
            String inStockStr = extractValue(jsonStr, "inStock");
            if (inStockStr != null) {
                item.setInStock(Boolean.parseBoolean(inStockStr.trim()));
            }

            return item;

        } catch (Exception e) {
            System.err.println("[JsonUtils] Error parsing JSON Object: " + e.getMessage());
            return null;
        }
    }

    /**
     * Regex helper method to find the value of a key in a JSON object string.
     */
    private static String extractValue(String json, String key) {
        // Look for "key" : value (handling quotes, numbers, booleans)
        String regex = "\"" + key + "\"\s*:\s*(\"[^\"]*\"|true|false|null|[0-9.]+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * Removes leading and trailing quotes from a JSON string value.
     */
    private static String cleanStringValue(String val) {
        val = val.trim();
        if (val.startsWith("\"") && val.endsWith("\"") && val.length() >= 2) {
            return val.substring(1, val.length() - 1);
        }
        return val;
    }
}
