package com.example.backend;

import com.example.backend.handler.ItemHandler;
import com.example.backend.repository.ItemRepository;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * ============================================================================
 * MAIN SERVER CLASS: Main.java
 * ============================================================================
 * JAVA LEARNING NOTE - Main Method & Server Lifecycle:
 * In Java, `public static void main(String[] args)` is the mandatory entry point of any standalone desktop / server application.
 * 
 * KEY CONCEPTS DEMONSTRATED HERE:
 * 1. `HttpServer.create()`: Native Java HTTP server listening on TCP sockets.
 * 2. Binding Address `0.0.0.0`: Listening on ALL local network interfaces so Android Emulators (`10.0.2.2`), 
 *    physical Android phones over Wi-Fi, and localhost can all connect!
 * 3. Thread Pools (`Executors.newFixedThreadPool`): Allows processing multiple requests simultaneously.
 * 4. Graceful Shutdown Hooks (`Runtime.getRuntime().addShutdownHook`): Cleaning up resources when server stops.
 */
public class Main {

    private static final int PORT = 8080;
    private static final String JSON_FILE_PATH = "data/initial_data.json";

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   STARTING JAVA REST API BACKEND SERVER         ");
        System.out.println("=================================================");

        try {
            // 1. Resolve path to JSON seed data file
            String dataPath = resolveDataFilePath();

            // 2. Initialize Data Repository & load data from initial_data.json into memory
            ItemRepository repository = new ItemRepository(dataPath);

            // 3. Create HTTP Server bound to port 8080 on 0.0.0.0 (all network interfaces)
            InetSocketAddress address = new InetSocketAddress("0.0.0.0", PORT);
            HttpServer server = HttpServer.create(address, 0);

            // 4. Register REST API endpoints and handlers
            server.createContext("/api/items", new ItemHandler(repository));

            // 5. Configure multithreaded Executor service (10 worker threads)
            server.setExecutor(Executors.newFixedThreadPool(10));

            // 6. Start listening for incoming client requests
            server.start();

            System.out.println("SUCCESS: Server is running and listening on port " + PORT);
            System.out.println("-------------------------------------------------");
            System.out.println(" Available Endpoints:");
            System.out.println("  - GET  http://localhost:" + PORT + "/api/items         (Get all items)");
            System.out.println("  - GET  http://localhost:" + PORT + "/api/items?id=1    (Get item by ID)");
            System.out.println("  - POST http://localhost:" + PORT + "/api/items         (Create new item)");
            System.out.println("-------------------------------------------------");
            System.out.println(" For Android Integration:");
            System.out.println("  - Android Emulator URL: http://10.0.2.2:" + PORT + "/api/items");
            System.out.println("  - Physical Device URL:  http://<YOUR_PC_IP>:" + PORT + "/api/items");
            System.out.println("=================================================");

            // 7. Add shutdown hook to handle Ctrl+C cleanly
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n[Server] Stopping Java REST API Server...");
                server.stop(1);
                System.out.println("[Server] Server stopped successfully.");
            }));

        } catch (IOException e) {
            System.err.println("FATAL: Failed to start HTTP server on port " + PORT);
            e.printStackTrace();
        }
    }

    /**
     * Resolves relative path to data/initial_data.json whether executed from project root or subfolder.
     */
    private static String resolveDataFilePath() {
        File file = new File(JSON_FILE_PATH);
        if (file.exists()) {
            return JSON_FILE_PATH;
        }
        // Fallback for execution from nested directories
        File fallback = new File("../" + JSON_FILE_PATH);
        if (fallback.exists()) {
            return fallback.getPath();
        }
        return JSON_FILE_PATH;
    }
}
