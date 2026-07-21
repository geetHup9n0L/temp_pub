# SQLCipher for Android: Vulnerable-to-Secure Code Conversion & Migration

This document provides a side-by-side comparison of **intentionally vulnerable code** vs. **secured SQLCipher code**, along with a database migration script to encrypt existing plaintext database files.

---

## 1. Vulnerable vs. Secure Code Comparison

### Scenario: Storing User Login Credentials in Internal Storage

#### ❌ VULNERABLE CODE (Standard Plaintext SQLite)
**Vulnerability:** Stores credentials in `/data/data/com.example.app/databases/users.db` without encryption. Rooted devices, ADB backups, or local exploits can read plain passwords.

```java
// Vulnerable Database Helper using standard Android SQLite
package com.example.vulnerablestorageapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class VulnerableDatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "users_vulnerable.db";
    private static final int DB_VERSION = 1;

    public VulnerableDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Plaintext SQLite database table creation
        db.execSQL("CREATE TABLE users (id INTEGER PRIMARY KEY, username TEXT, password TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }

    public boolean registerUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase(); // ❌ No passphrase required! File is readable plaintext on disk.
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", password); // ❌ Storing raw/plaintext password
        long result = db.insert("users", null, values);
        db.close();
        return result != -1;
    }
}
```

---

#### ✅ SECURE CODE (SQLCipher + KeyStore Passphrase)
**Remediation:** Uses `net.zetetic.database.sqlcipher.SQLiteOpenHelper` with hardware-backed KeyStore key management.

```java
// Secure Database Helper using SQLCipher
package com.example.vulnerablestorageapp;

import android.content.ContentValues;
import android.content.Context;
import net.zetetic.database.sqlcipher.SQLiteDatabase;
import net.zetetic.database.sqlcipher.SQLiteOpenHelper;
import java.util.Arrays;

public class SecureDatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "users_secure.db";
    private static final int DB_VERSION = 1;

    public SecureDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Table schema inside 256-bit AES encrypted database
        db.execSQL("CREATE TABLE users (id INTEGER PRIMARY KEY, username TEXT, password_hash TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }

    public boolean registerUser(Context context, String username, String password) {
        char[] passphrase = null;
        try {
            // Retrieve or generate key stored securely in Android KeyStore
            passphrase = com.example.vulnerablestorageapp.security.KeyManager.getOrCreateDatabasePassphrase(context);
            
            // ✅ Open DB with passphrase
            SQLiteDatabase db = this.getWritableDatabase(passphrase);
            
            ContentValues values = new ContentValues();
            values.put("username", username);
            values.put("password_hash", password); // In production, also hash with Argon2/bcrypt
            
            long result = db.insert("users", null, values);
            db.close();
            return result != -1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            // ✅ Wipe passphrase from RAM array
            if (passphrase != null) {
                Arrays.fill(passphrase, '\0');
            }
        }
    }
}
```

---

## 2. Migrating Plaintext SQLite Database to SQLCipher

If your app already has an unencrypted SQLite database (`users_vulnerable.db`) on existing user devices, you can encrypt it using `sqlcipher_export`.

### Migration Utility Function

```java
package com.example.vulnerablestorageapp.security;

import android.content.Context;
import net.zetetic.database.sqlcipher.SQLiteDatabase;
import java.io.File;

public class DatabaseMigrator {

    public static boolean encryptExistingDatabase(Context context, String oldDbName, String newDbName, char[] passphrase) {
        File unencryptedDbFile = context.getDatabasePath(oldDbName);
        File encryptedDbFile = context.getDatabasePath(newDbName);

        if (!unencryptedDbFile.exists()) {
            return false;
        }

        SQLiteDatabase database = null;
        try {
            // 1. Open the existing unencrypted database file with an empty key
            database = SQLiteDatabase.openDatabase(
                    unencryptedDbFile.getAbsolutePath(),
                    "",
                    null,
                    SQLiteDatabase.OPEN_READWRITE
            );

            // 2. Attach the new database file and set its encryption password
            String hexKey = String.valueOf(passphrase);
            database.execSQL(String.format("ATTACH DATABASE '%s' AS encrypted KEY '%s';", 
                    encryptedDbFile.getAbsolutePath(), hexKey));

            // 3. Export scheme and data into encrypted database
            database.execSQL("SELECT sqlcipher_export('encrypted');");

            // 4. Detach encrypted database
            database.execSQL("DETACH DATABASE encrypted;");

            // 5. Close old database and delete plaintext database file
            database.close();
            unencryptedDbFile.delete();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (database != null && database.isOpen()) {
                database.close();
            }
            return false;
        }
    }
}
```

---

## 3. OWASP MASVS / MASTG Compliance Verification

To verify that your app passes OWASP MASTG-STORAGE checks:

1. **Pull Database via ADB:**
   ```bash
   adb shell "su -c 'cp /data/data/com.example.vulnerablestorageapp/databases/users_secure.db /sdcard/'"
   adb pull /sdcard/users_secure.db .
   ```
2. **Attempt Reading with standard SQLite CLI:**
   ```bash
   sqlite3 users_secure.db "SELECT * FROM users;"
   ```
   *Expected Output:* `Runtime error: file is not a database` (Confirms full file encryption!).
3. **Inspect Process Memory / Heap Dumps:**
   - Verify that database passphrase arrays (`char[]`) are zeroed out after opening connections, leaving no raw keys in memory heap dumps.
