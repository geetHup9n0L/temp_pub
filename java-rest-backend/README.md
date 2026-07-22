# Java REST API Backend - Beginner Learning Guide & Reference

Welcome! This is a clean, standard Java REST API backend server created to help you learn Java programming and understand how backend servers interact with mobile applications (such as Android apps written in Java).

---

## 📌 Project Overview & Features

- **Built with Pure Java**: Uses standard Java JDK built-in libraries (`com.sun.net.httpserver.HttpServer`). Requires **zero external JAR downloads** or framework overhead.
- **JSON File Database**: On startup, the server reads data from `data/initial_data.json` into an in-memory `ArrayList` of Java objects.
- **REST Endpoints**:
  - `GET /api/items`: Retrieves all items in JSON format.
  - `GET /api/items?id=1`: Retrieves a specific item by integer ID.
  - `POST /api/items`: Accepts a new JSON object in the request body, appends it to memory, and persists it back to `initial_data.json`.
- **CORS Support**: Includes Cross-Origin Resource Sharing (`Access-Control-Allow-Origin: *`) headers so mobile applications, emulators, and web frontends can make HTTP requests without browser/CORS blocks.

---

## 📁 Directory Structure

```text
java-rest-backend/
├── data/
│   └── initial_data.json            <-- Seed database file (JSON Array of items)
├── src/
│   └── com/example/backend/
│       ├── Main.java                <-- Application entry point & HTTP Server launcher
│       ├── model/
│       │   └── Item.java            <-- Data model POJO (Plain Old Java Object)
│       ├── repository/
│       │   └── ItemRepository.java  <-- In-memory & JSON file data persistence
│       ├── handler/
│       │   └── ItemHandler.java     <-- Route handler for GET, POST, and OPTIONS
│       └── util/
│           └── JsonUtils.java       <-- Helper functions for JSON parsing & generation
├── pom.xml                          <-- Optional Maven configuration file
├── ANDROID_INTEGRATION_GUIDE.md     <-- Complete guide on calling this API from Android Java
└── README.md                        <-- Detailed Java syntax & architecture tutorial (This file)
```

---

## 🚀 How to Compile & Run the Server

### Method 1: Using Standard `javac` and `java` (Recommended)

1. Open your terminal or Command Prompt and navigate to the project directory:
   ```bash
   cd c:\Users\PC\Documents\Agent\java-rest-backend
   ```

2. Compile all Java source files into a `bin` directory:
   ```bash
   javac -d bin src/com/example/backend/model/Item.java src/com/example/backend/util/JsonUtils.java src/com/example/backend/repository/ItemRepository.java src/com/example/backend/handler/ItemHandler.java src/com/example/backend/Main.java
   ```

3. Run the compiled Java program:
   ```bash
   java -cp bin com.example.backend.Main.Main
   ```

4. You will see output indicating the server has started on port 8080:
   ```text
   =================================================
      STARTING JAVA REST API BACKEND SERVER         
   =================================================
   [ItemRepository] Initializing database from: ...\data\initial_data.json
   [ItemRepository] Successfully loaded 4 items into local memory list.
   SUCCESS: Server is running and listening on port 8080
   ```

---

## 🎓 Java Syntax & Concept Breakdown (For Learners)

### 1. Packages (`package com.example.backend...`)
In Java, packages organize related classes into namespaces (similar to folders on a disk). This prevents naming conflicts.
- `package com.example.backend.model;` means this class belongs to the `model` package.

### 2. Classes & Encapsulation ([Item.java](file:///c:/Users/PC/Documents/Agent/java-rest-backend/src/com/example/backend/model/Item.java))
Java is an **Object-Oriented Programming (OOP)** language.
- **Encapsulation**: Member variables (like `private int id;`) are marked `private` so they cannot be corrupted directly from outside the class.
- **Getters & Setters**: Public methods like `getId()` and `setId(int id)` provide controlled access to private fields.
- **Constructors**: Methods called when initializing an object with `new Item(...)`.

### 3. Java Collections (`List` and `ArrayList`)
In Java, arrays have fixed sizes. `List<Item>` (specifically `ArrayList<Item>`) is a dynamic collection that automatically grows when items are added using `items.add(newItem)`.

### 4. Thread Safety (`synchronized` & `AtomicInteger`)
Because HTTP servers handle multiple incoming requests simultaneously on different threads, shared data structures like `ArrayList` must be synchronized:
- `Collections.synchronizedList(new ArrayList<>())` wraps the list so multiple threads can read/write safely without crashing or corrupting memory.
- `AtomicInteger` safely auto-increments IDs across concurrent requests.

### 5. Standard Java HTTP Server (`com.sun.net.httpserver.HttpServer`)
- **`HttpServer.create(address, 0)`**: Creates a TCP server socket.
- **`server.createContext("/api/items", handler)`**: Binds incoming requests matching the `/api/items` path to an `HttpHandler`.
- **`HttpExchange`**: Contains the incoming client request details (method, headers, body) and methods to write back the HTTP response.

### 6. File I/O (`java.nio.file.Files`)
- `Files.readString(Path)` reads text directly from a file into a Java `String`.
- `Files.writeString(Path, String)` writes a Java `String` directly to a file on disk.

---

## 🧪 Testing the REST API Endpoints

### 1. Retrieve All Items (`GET /api/items`)
Using PowerShell or Terminal:
```bash
curl http://localhost:8080/api/items
```
*Response (`200 OK`):*
```json
[
  {"id":1,"name":"Wireless Bluetooth Headphones","category":"Electronics","price":59.99,"inStock":true},
  {"id":2,"name":"Ergonomic Mechanical Keyboard","category":"Electronics","price":119.50,"inStock":true},
  {"id":3,"name":"Stainless Steel Water Bottle","category":"Fitness","price":24.90,"inStock":false},
  {"id":4,"name":"Java Programming Guide Book","category":"Books","price":39.95,"inStock":true}
]
```

### 2. Retrieve Item by ID (`GET /api/items?id=2`)
```bash
curl http://localhost:8080/api/items?id=2
```
*Response (`200 OK`):*
```json
{"id":2,"name":"Ergonomic Mechanical Keyboard","category":"Electronics","price":119.50,"inStock":true}
```

### 3. Create a New Item (`POST /api/items`)
Using PowerShell:
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/items" -Method Post -ContentType "application/json" -Body '{"name":"Gaming Mouse","category":"Electronics","price":49.99,"inStock":true}'
```
Or cURL:
```bash
curl -X POST http://localhost:8080/api/items -H "Content-Type: application/json" -d "{\"name\":\"Gaming Mouse\",\"category\":\"Electronics\",\"price\":49.99,\"inStock\":true}"
```
*Response (`201 Created`):*
```json
{"id":5,"name":"Gaming Mouse","category":"Electronics","price":49.99,"inStock":true}
```
*(Notice how ID `5` was auto-assigned, and the item was saved into `data/initial_data.json`!)*

---

## 📱 Next Steps for Android Integration

Check out the companion guide: [ANDROID_INTEGRATION_GUIDE.md](file:///c:/Users/PC/Documents/Agent/java-rest-backend/ANDROID_INTEGRATION_GUIDE.md) to learn how to connect an Android app written in Java to this server!
