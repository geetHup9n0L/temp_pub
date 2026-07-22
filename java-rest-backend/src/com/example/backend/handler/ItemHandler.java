package com.example.backend.handler;

import com.example.backend.model.Item;
import com.example.backend.repository.ItemRepository;
import com.example.backend.util.JsonUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ============================================================================
 * HANDLER CLASS: ItemHandler.java
 * ============================================================================
 * JAVA LEARNING NOTE - HTTP Handlers & REST Endpoints:
 * In Java HTTP servers, an `HttpHandler` intercepts client HTTP requests for a specific path 
 * (e.g. `/api/items`).
 * 
 * KEY JAVA & REST CONCEPTS DEMONSTRATED HERE:
 * 1. Interface Implementation (`implements HttpHandler`): Overriding the `handle(HttpExchange exchange)` method.
 * 2. HTTP Methods: Differentiating GET (retrieve data) vs POST (create data) vs OPTIONS (CORS preflight).
 * 3. Input & Output Streams: Reading bytes from the request body (`InputStream`) and writing bytes to the response body (`OutputStream`).
 * 4. HTTP Headers & Status Codes: Setting `Content-Type: application/json` and returning standard codes like 200 OK, 201 Created, 400 Bad Request, 404 Not Found.
 * 5. Cross-Origin Resource Sharing (CORS): Allowing mobile apps and web browsers to make requests without getting blocked by CORS policies.
 */
public class ItemHandler implements HttpHandler {

    private final ItemRepository repository;

    /**
     * Constructor injecting the ItemRepository dependency.
     * 
     * @param repository The shared in-memory repository instance.
     */
    public ItemHandler(ItemRepository repository) {
        this.repository = repository;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Set CORS headers so Android devices, emulators, or web browsers can connect freely
        setCorsHeaders(exchange);

        String method = exchange.getRequestMethod().toUpperCase();
        System.out.println("[ItemHandler] Received HTTP " + method + " request for URI: " + exchange.getRequestURI());

        try {
            switch (method) {
                case "GET":
                    handleGet(exchange);
                    break;
                case "POST":
                    handlePost(exchange);
                    break;
                case "OPTIONS":
                    // CORS preflight requests simply respond with 204 No Content
                    exchange.sendResponseHeaders(204, -1);
                    break;
                default:
                    // Method Not Allowed
                    sendJsonResponse(exchange, 405, "{\"error\": \"Method not allowed. Use GET or POST.\"}");
                    break;
            }
        } catch (Exception e) {
            System.err.println("[ItemHandler] Exception processing request: " + e.getMessage());
            e.printStackTrace();
            sendJsonResponse(exchange, 500, "{\"error\": \"Internal Server Error: " + e.getMessage() + "\"}");
        }
    }

    /**
     * Handles HTTP GET requests:
     * - `GET /api/items` -> Returns list of all items (200 OK)
     * - `GET /api/items?id=2` -> Returns item with id 2 (200 OK or 404 Not Found)
     */
    private void handleGet(HttpExchange exchange) throws IOException {
        Map<String, String> queryParams = parseQueryParams(exchange.getRequestURI().getQuery());

        // Check if an 'id' query parameter was passed, e.g. /api/items?id=2
        if (queryParams.containsKey("id")) {
            try {
                int id = Integer.parseInt(queryParams.get("id"));
                Optional<Item> itemOptional = repository.findById(id);

                if (itemOptional.isPresent()) {
                    sendJsonResponse(exchange, 200, itemOptional.get().toJson());
                } else {
                    sendJsonResponse(exchange, 404, "{\"error\": \"Item with ID " + id + " not found.\"}");
                }
            } catch (NumberFormatException e) {
                sendJsonResponse(exchange, 400, "{\"error\": \"Invalid ID format. Must be an integer.\"}");
            }
        } else {
            // Return all items
            List<Item> items = repository.findAll();
            String jsonResponse = JsonUtils.itemListToJson(items);
            sendJsonResponse(exchange, 200, jsonResponse);
        }
    }

    /**
     * Handles HTTP POST requests:
     * Reads JSON from request body, creates a new Item, saves it into repository,
     * and returns the created Item JSON with status code 201 Created.
     */
    private void handlePost(HttpExchange exchange) throws IOException {
        // Read body input stream into String
        InputStream is = exchange.getRequestBody();
        String requestBody = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        System.out.println("[ItemHandler] Received POST request body: " + requestBody);

        if (requestBody.trim().isEmpty()) {
            sendJsonResponse(exchange, 400, "{\"error\": \"Request body cannot be empty.\"}");
            return;
        }

        // Parse JSON string into Java Item object
        Item newItem = JsonUtils.parseJsonObject(requestBody);

        if (newItem == null || newItem.getName() == null || newItem.getName().trim().isEmpty()) {
            sendJsonResponse(exchange, 400, "{\"error\": \"Invalid Item data. 'name' is required.\"}");
            return;
        }

        // Save into repository (auto-assigns ID and updates JSON file)
        Item savedItem = repository.save(newItem);

        // Return 201 Created status with the newly saved item as JSON
        sendJsonResponse(exchange, 201, savedItem.toJson());
    }

    /**
     * Helper method to send a JSON HTTP response back to the client.
     * 
     * @param exchange     The HTTP exchange object
     * @param statusCode   HTTP status code (e.g. 200, 201, 400, 404, 500)
     * @param jsonResponse The JSON string payload
     */
    private void sendJsonResponse(HttpExchange exchange, int statusCode, String jsonResponse) throws IOException {
        byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
            os.flush();
        }
    }

    /**
     * Sets standard Cross-Origin Resource Sharing (CORS) headers.
     */
    private void setCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    /**
     * Utility method to parse URL query parameters like `id=2&category=Electronics` into a Map.
     */
    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.trim().isEmpty()) {
            return map;
        }
        for (String param : query.split("&")) {
            String[] pair = param.split("=");
            if (pair.length > 1) {
                map.put(pair[0], pair[1]);
            } else if (pair.length == 1) {
                map.put(pair[0], "");
            }
        }
        return map;
    }
}
