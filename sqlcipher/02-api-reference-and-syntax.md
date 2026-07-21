# SQLCipher for Android: API Reference & Syntax Guide

This document acts as an **API Reference and Syntax Manual** for developers using `net.zetetic.database.sqlcipher` in Android Java.

---

## 🏛 Package Overview: `net.zetetic.database.sqlcipher`

SQLCipher for Android provides an exact wrapper API compatible with standard Android SQLite (`android.database.sqlite`). Below are the primary classes and interfaces.

---

## 1. Class: `SQLiteDatabase`

The primary class for managing and executing queries against an encrypted SQLite database.

### Core Methods

#### `openOrCreateDatabase`
Opens an existing database or creates a new encrypted database file.

```java
public static SQLiteDatabase openOrCreateDatabase(
    File file,
    byte[] password,
    SQLiteDatabase.CursorFactory factory,
    DatabaseErrorHandler errorHandler,
    SQLiteDatabaseHook hook
)
```
- **Parameters:**
  - `file`: Path to the database file on internal storage (e.g. `context.getDatabasePath("app.db")`).
  - `password`: `byte[]` or `char[]` passphrase used for keying/encrypting the database.
  - `factory`: Optional `CursorFactory` (pass `null` for default).
  - `errorHandler`: Optional custom error handler (pass `null` for default).
  - `hook`: Optional `SQLiteDatabaseHook` for pre/post-key PRAGMA commands (pass `null` if unused).

#### `execSQL`
Executes a raw single SQL statement that does NOT return data (e.g., `CREATE TABLE`, `INSERT`, `UPDATE`).

```java
public void execSQL(String sql) throws SQLException
public void execSQL(String sql, Object[] bindArgs) throws SQLException
```
- **Example:**
  ```java
  db.execSQL("CREATE TABLE users (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT, password_hash TEXT)");
  ```

#### `rawQuery`
Executes a raw SQL statement that returns a `Cursor` result set.

```java
public Cursor rawQuery(String sql, String[] selectionArgs)
```
- **Example:**
  ```java
  Cursor cursor = db.rawQuery("SELECT * FROM users WHERE username = ?", new String[]{"admin"});
  if (cursor.moveToFirst()) {
      String hash = cursor.getString(cursor.getColumnIndexOrThrow("password_hash"));
  }
  cursor.close();
  ```

#### `insert` / `update` / `delete`
Convenience methods for database CRUD operations.

```java
public long insert(String table, String nullColumnHack, ContentValues values)
public int update(String table, ContentValues values, String whereClause, String[] whereArgs)
public int delete(String table, String whereClause, String[] whereArgs)
```

---

## 2. Class: `SQLiteOpenHelper`

A helper class to manage database creation and version management.

### Constructor
```java
public SQLiteOpenHelper(
    Context context,
    String name,
    SQLiteDatabase.CursorFactory factory,
    int version
)
```

### Abstract Callbacks
```java
@Override
public abstract void onCreate(SQLiteDatabase db);

@Override
public abstract void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);
```

### Opening Methods

#### `getWritableDatabase`
Creates and/or opens a database that will be used for reading and writing.

```java
public SQLiteDatabase getWritableDatabase(char[] password)
public SQLiteDatabase getWritableDatabase(String password)
public SQLiteDatabase getWritableDatabase(byte[] password)
```
- **Example:**
  ```java
  MyDatabaseHelper helper = new MyDatabaseHelper(context);
  SQLiteDatabase db = helper.getWritableDatabase(passphraseCharArray);
  ```

---

## 3. Interface: `SQLiteDatabaseHook`

Provides hooks to execute custom SQLCipher PRAGMA statements **before** or **after** the key is applied to the database connection.

```java
public interface SQLiteDatabaseHook {
    void preKey(SQLiteConnection connection);
    void postKey(SQLiteConnection connection);
}
```

- **Example Usage (Customizing KDF iteration count or page size):**
  ```java
  SQLiteDatabaseHook hook = new SQLiteDatabaseHook() {
      @Override
      public void preKey(SQLiteConnection connection) {
          // Executed before PRAGMA key is applied
      }

      @Override
      public void postKey(SQLiteConnection connection) {
          // Executed after keying. Useful for setting WAL mode or cipher memory security
          connection.execute("PRAGMA cipher_memory_security = ON;", null, null);
      }
  };
  ```

---

## 4. SQLCipher PRAGMA Commands Reference

SQLCipher provides special `PRAGMA` commands to inspect and modify security configurations.

| PRAGMA Command | Syntax | Description |
| :--- | :--- | :--- |
| `cipher_version` | `PRAGMA cipher_version;` | Returns the current SQLCipher engine version (e.g. `4.5.6 community`). |
| `key` | `PRAGMA key = 'passphrase';` | Sets the key for the current database connection (handled automatically by `openOrCreateDatabase`). |
| `rekey` | `PRAGMA rekey = 'new_passphrase';` | Changes the key of an already encrypted database. |
| `cipher_migrate` | `PRAGMA cipher_migrate;` | Upgrades legacy SQLCipher v1/v2/v3 database formats to SQLCipher 4 format. |
| `cipher_memory_security` | `PRAGMA cipher_memory_security = ON;` | Zeroes out allocated memory buffers when freed to prevent key or plaintext leakage in process RAM. |
| `kdf_iter` | `PRAGMA kdf_iter = 256000;` | Configures the PBKDF2 key derivation iteration count (default in SQLCipher 4 is 256,000). |
| `cipher_page_size` | `PRAGMA cipher_page_size = 4096;` | Sets the byte size of encrypted pages (default 4096 bytes). |
