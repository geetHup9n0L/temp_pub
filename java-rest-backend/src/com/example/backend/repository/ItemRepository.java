package com.example.backend.repository;

import com.example.backend.model.Item;
import com.example.backend.util.JsonUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ============================================================================
 * REPOSITORY CLASS: ItemRepository.java
 * ============================================================================
 * JAVA LEARNING NOTE - Repository Pattern & Data Access:
 * In software architecture, a Repository abstracts data persistence (reading/writing to files or database).
 * 
 * KEY JAVA CONCEPTS DEMONSTRATED HERE:
 * 1. File I/O (NIO Paths & Files): Reading text content from initial_data.json into memory.
 * 2. Java Collections (List & ArrayList): Storing objects dynamically in memory.
 * 3. Thread Safety (`synchronized`): Standard Java HTTP servers handle multiple client requests 
 *    concurrently on multiple threads. Using `synchronized` ensures two clients adding items at 
 *    the exact same time don't corrupt the list.
 * 4. AtomicInteger: Auto-generating unique IDs safely.
 */
public class ItemRepository {

    private final String jsonFilePath;
    private final List<Item> items = Collections.synchronizedList(new ArrayList<>());
    private final AtomicInteger idGenerator = new AtomicInteger(0);

    /**
     * Repository Constructor.
     * 
     * @param jsonFilePath Relative or absolute path to the JSON seed data file.
     */
    public ItemRepository(String jsonFilePath) {
        this.jsonFilePath = jsonFilePath;
        loadDataFromJsonFile();
    }

    /**
     * Reads the JSON file from disk on server initialization, converts the JSON 
     * array into Java Item objects, and stores them in our local memory list.
     */
    private void loadDataFromJsonFile() {
        Path path = Paths.get(jsonFilePath);
        File file = path.toFile();

        System.out.println("[ItemRepository] Initializing database from: " + file.getAbsolutePath());

        if (!file.exists()) {
            System.err.println("[ItemRepository] WARNING: Initial JSON file not found at " + file.getAbsolutePath() + ". Starting with empty database.");
            return;
        }

        try {
            // Read entire file content into a String (Java NIO feature)
            String jsonContent = Files.readString(path);
            
            // Parse JSON into Java List<Item> using our JsonUtils
            List<Item> loadedItems = JsonUtils.parseJsonArray(jsonContent);
            
            synchronized (items) {
                items.clear();
                items.addAll(loadedItems);
            }

            // Find the highest existing ID to set the auto-increment ID counter
            int maxId = 0;
            for (Item item : loadedItems) {
                if (item.getId() > maxId) {
                    maxId = item.getId();
                }
            }
            idGenerator.set(maxId);

            System.out.println("[ItemRepository] Successfully loaded " + loadedItems.size() + " items into local memory list.");

        } catch (IOException e) {
            System.err.println("[ItemRepository] ERROR reading JSON file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Retrieves a copy of all items in local memory.
     * 
     * @return List of all Items
     */
    public List<Item> findAll() {
        synchronized (items) {
            return new ArrayList<>(items);
        }
    }

    /**
     * Finds a single item by its ID.
     * Demonstrates Java Optional (a container object which may or may not contain a non-null value).
     * 
     * @param id Item integer ID
     * @return Optional containing the item if found, or empty Optional if not found.
     */
    public Optional<Item> findById(int id) {
        synchronized (items) {
            for (Item item : items) {
                if (item.getId() == id) {
                    return Optional.of(item);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Adds a new item to our local memory database and assigns it a unique ID.
     * Also saves the updated state back to initial_data.json so changes persist!
     * 
     * @param item Item to save
     * @return Saved item with auto-assigned ID
     */
    public synchronized Item save(Item item) {
        // Auto-increment ID if not set or 0
        if (item.getId() <= 0) {
            item.setId(idGenerator.incrementAndGet());
        }

        items.add(item);
        System.out.println("[ItemRepository] Saved new Item: " + item);

        // Persist back to initial_data.json file asynchronously
        persistToJsonFile();

        return item;
    }

    /**
     * Saves the current list of items back to the JSON file on disk.
     */
    private void persistToJsonFile() {
        try {
            String jsonOutput = JsonUtils.itemListToJson(items);
            Files.writeString(Paths.get(jsonFilePath), jsonOutput);
            System.out.println("[ItemRepository] Persisted updated data to " + jsonFilePath);
        } catch (IOException e) {
            System.err.println("[ItemRepository] Failed to write updated JSON file: " + e.getMessage());
        }
    }
}
