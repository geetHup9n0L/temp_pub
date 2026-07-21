# BCrypt for Android: Vulnerable-to-Secure Conversion & Integration

This document provides a side-by-side comparison of **intentionally vulnerable code** storing plaintext credentials vs. **secure BCrypt hashed authentication**, and demonstrates how to combine **BCrypt Hashing with SQLCipher Encrypted Storage**.

---

## 1. Vulnerable vs. Secure Authentication Comparison

### Scenario: User Registration and Login in Android

#### ❌ VULNERABLE CODE (Plaintext Password Storage)
**Vulnerability:** Stores plaintext passwords directly in internal storage. Anyone with access to the database (or ADB backups/memory dumps) gains immediate access to user passwords.

```java
// Vulnerable Authentication Handler
package com.example.vulnerablestorageapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class VulnerableAuthManager {
    private VulnerableDatabaseHelper dbHelper;

    public VulnerableAuthManager(Context context) {
        dbHelper = new VulnerableDatabaseHelper(context);
    }

    public boolean register(String username, String plaintextPassword) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", plaintextPassword); // ❌ Storing Raw Plaintext Password!
        long id = db.insert("users", null, values);
        db.close();
        return id != -1;
    }

    public boolean login(String username, String plaintextPassword) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // ❌ Direct string match against plaintext password stored in database
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE username = ? AND password = ?",
                new String[]{username, plaintextPassword});
        boolean isAuthenticated = cursor.moveToFirst();
        cursor.close();
        db.close();
        return isAuthenticated;
    }
}
```

---

#### ✅ SECURE CODE (BCrypt Hashing + Background Threading)
**Remediation:** Hashes passwords with BCrypt (cost factor 10) on a background thread. Only the resulting 60-character salt-embedded hash is stored.

```java
// Secure Authentication Handler with BCrypt Hashing
package com.example.vulnerablestorageapp;

import android.content.ContentValues;
import android.content.Context;
import at.favre.lib.crypto.bcrypt.BCrypt;
import com.example.vulnerablestorageapp.utils.AppExecutors;
import java.util.Arrays;
import net.zetetic.database.sqlcipher.Cursor;
import net.zetetic.database.sqlcipher.SQLiteDatabase;

public class SecureAuthManager {
    private SecureDatabaseHelper dbHelper;
    private static final int BCRYPT_COST = 10;

    public SecureAuthManager(Context context) {
        dbHelper = new SecureDatabaseHelper(context);
    }

    public void registerUser(Context context, String username, char[] password, AppExecutors.TaskCallback<Boolean> callback) {
        AppExecutors.runInBackground(() -> {
            try {
                // 1. Hash password with BCrypt (Includes 128-bit auto-generated salt)
                String passwordHash = BCrypt.withDefaults().hashToString(BCRYPT_COST, password);

                // 2. Retrieve SQLCipher Database Key from Android KeyStore
                char[] dbKey = com.example.vulnerablestorageapp.security.KeyManager.getOrCreateDatabasePassphrase(context);
                SQLiteDatabase db = dbHelper.getWritableDatabase(dbKey);

                ContentValues values = new ContentValues();
                values.put("username", username);
                values.put("password_hash", passwordHash); // ✅ Only store BCrypt hash

                long id = db.insert("users", null, values);
                db.close();
                Arrays.fill(dbKey, '\0');
                return id != -1;
            } finally {
                // Scrub password array from memory
                Arrays.fill(password, '\0');
            }
        }, callback);
    }

    public void loginUser(Context context, String username, char[] inputPassword, AppExecutors.TaskCallback<Boolean> callback) {
        AppExecutors.runInBackground(() -> {
            try {
                // 1. Open SQLCipher database
                char[] dbKey = com.example.vulnerablestorageapp.security.KeyManager.getOrCreateDatabasePassphrase(context);
                SQLiteDatabase db = dbHelper.getReadableDatabase(dbKey);

                // 2. Query user record by username ONLY (do NOT query password in SQL)
                Cursor cursor = db.rawQuery("SELECT password_hash FROM users WHERE username = ?", new String[]{username});
                
                if (!cursor.moveToFirst()) {
                    cursor.close();
                    db.close();
                    Arrays.fill(dbKey, '\0');
                    return false; // User not found
                }

                String storedHash = cursor.getString(cursor.getColumnIndexOrThrow("password_hash"));
                cursor.close();
                db.close();
                Arrays.fill(dbKey, '\0');

                // 3. Constant-Time Verification of input password against stored BCrypt hash
                BCrypt.Result result = BCrypt.verifyer().verify(inputPassword, storedHash.toCharArray());
                return result.verified;
            } finally {
                Arrays.fill(inputPassword, '\0');
            }
        }, callback);
    }
}
```

---

## 2. Integrated Security Architecture

Combining **BCrypt** with **SQLCipher** and **Android KeyStore** provides defense-in-depth (**OWASP MASVS-STORAGE & AUTH**):

```
+-------------------------------------------------------------------+
|                        Android Application                        |
+-------------------------------------------------------------------+
                                  |
                                  v
+-------------------------------------------------------------------+
|                     Password Input (char[])                       |
+-------------------------------------------------------------------+
                                  |
            [ BCrypt Hashing (Cost 10 + Random Salt) ]
                                  v
+-------------------------------------------------------------------+
|               BCrypt Hash Output ($2a$10$...)                     |
+-------------------------------------------------------------------+
                                  |
    [ Inserted into SQLCipher 256-bit AES Encrypted Database ]
                                  |
    [ DB Key Protected by Android KeyStore (MasterKey/AES-GCM) ]
                                  v
+-------------------------------------------------------------------+
|                 Secured Device Internal Storage                   |
+-------------------------------------------------------------------+
```

### Security Benefits of Dual Protection
1. **If Database File is Stolen:** SQLCipher prevents an attacker from opening or viewing raw table data.
2. **If SQLCipher Encryption Key is Compromised:** The attacker only sees BCrypt hashes (`$2a$10$...`), making raw password recovery practically impossible due to CPU/GPU time cost.
3. **If Device RAM is Dumped:** `char[]` array scrubbing minimizes the lifespan of plaintext passwords in process memory heap.
