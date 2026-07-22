package com.example.backend.model;

/**
 * ============================================================================
 * MODEL CLASS: Item.java
 * ============================================================================
 * JAVA LEARNING NOTE - POJO (Plain Old Java Object):
 * In Java, a Model class (or POJO / Data Transfer Object) represents a real-world entity 
 * (in this case, an Item in our inventory database).
 * 
 * KEY JAVA CONCEPTS DEMONSTRATED HERE:
 * 1. Encapsulation: Fields are private so they cannot be altered directly from outside.
 * 2. Getters & Setters: Public methods that allow controlled access to read and update fields.
 * 3. Constructors: Special methods called when instantiating (creating) an object with `new Item(...)`.
 */
public class Item {
    
    // --- Member Variables (Fields / Attributes) ---
    private int id;
    private String name;
    private String category;
    private double price;
    private boolean inStock;

    /**
     * Default / No-argument Constructor.
     * Needed for frameworks or serialization tools that instantiate objects before populating fields.
     */
    public Item() {
    }

    /**
     * Parameterized Constructor.
     * Allows initializing all fields of the Item object when it is created.
     * 
     * @param id        Unique integer identifier
     * @param name      Item name
     * @param category  Item category
     * @param price     Item price in USD
     * @param inStock   Availability status
     */
    public Item(int id, String name, String category, double price, boolean inStock) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.inStock = inStock;
    }

    // ============================================================================
    // GETTERS AND SETTERS
    // ============================================================================

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isInStock() {
        return inStock;
    }

    public void setInStock(boolean inStock) {
        this.inStock = inStock;
    }

    // ============================================================================
    // UTILITY METHODS
    // ============================================================================

    /**
     * Converts this Item instance into a valid JSON String.
     * Demonstrates String building and escape sequences in Java.
     * 
     * @return Formatted JSON string representation of this item.
     */
    public String toJson() {
        return String.format(
            "{\"id\":%d,\"name\":\"%s\",\"category\":\"%s\",\"price\":%.2f,\"inStock\":%b}",
            id, escapeJsonString(name), escapeJsonString(category), price, inStock
        );
    }

    /**
     * Helper to escape special characters like quotes or backslashes in JSON strings.
     */
    private String escapeJsonString(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }

    @Override
    public String toString() {
        return "Item{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", price=" + price +
                ", inStock=" + inStock +
                '}';
    }
}
