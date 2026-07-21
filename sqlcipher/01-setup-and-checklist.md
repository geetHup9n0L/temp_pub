# SQLCipher for Android: Setup & Developer Checklist

This document details how to integrate SQLCipher into an Android Java project and provides an actionable **Developer & OWASP Security Checklist** for converting an intentionally vulnerable app into a secure application.

---

## 1. Environment & Project Dependencies

To use SQLCipher for Android, add the official Zetetic SQLCipher dependency and `androidx.sqlite` library to your `app/build.gradle` (or `build.gradle.kts`):

### Groovy (`app/build.gradle`)
```groovy
dependencies {
    // SQLCipher for Android Community Edition
    implementation 'net.zetetic:sqlcipher-android:4.5.6@aar' // Or latest 4.x release
    implementation 'androidx.sqlite:sqlite:2.4.0'
}
```

---

## 2. Native Library Initialization

Before calling any database methods or opening database helpers, load the native `sqlcipher` library into your process memory.

Place this initialization in your `Application` class or main `Activity` `onCreate()`:

```java
package com.example.vulnerablestorageapp;

import android.app.Application;
import net.zetetic.database.sqlcipher.SQLiteDatabase;

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Load SQLCipher native shared objects (.so files)
        System.loadLibrary("sqlcipher");
        
        // Alternatively, use legacy loadLibs helper:
        // SQLiteDatabase.loadLibs(this);
    }
}
```

> **Note:** Don't forget to register `MainApplication` in your `AndroidManifest.xml` under `<application android:name=".MainApplication">`.

---

## 3. Developer & Security Checklist

Use this checklist when implementing SQLCipher or converting a vulnerable SQLite database app to a secure one.

### Phase A: Database Layer Checklist
- [ ] **Replace Imports:** Change all `import android.database.sqlite.*` to `import net.zetetic.database.sqlcipher.*`.
- [ ] **Native Library Load:** Ensure `System.loadLibrary("sqlcipher")` is called before any database access.
- [ ] **Update Open Helper Constructor:** Ensure `SQLiteOpenHelper` calls supply a non-null passphrase when calling `getWritableDatabase(passphrase)` or `getReadableDatabase(passphrase)`.
- [ ] **Use Safe Primitive Types:** Pass passwords as `char[]` or `byte[]` instead of immutable `String` objects, so memory can be scrubbed immediately using `Arrays.fill(passphrase, '\0')`.
- [ ] **Enable Write-Ahead Logging (WAL):** Call `db.enableWriteAheadLogging()` for concurrent performance with encryption overhead.

### Phase B: OWASP MASTG-STORAGE Key Management Checklist
- [ ] **NO Hardcoded Keys:** Never hardcode database encryption passwords inside Java source code or strings resource files.
- [ ] **Android KeyStore Integration:** Generate a cryptographic key inside the hardware-backed **Android KeyStore** (`KeyGenerator` with `AndroidKeyStore` provider).
- [ ] **Encrypted Key Storage:** Encrypt the generated database passphrase using `MasterKey` and `EncryptedSharedPreferences` (or AES-GCM via KeyStore), storing only the ciphertext.
- [ ] **Wipe Key from RAM:** Zero out `char[]` / `byte[]` arrays containing the passphrase immediately after opening the database instance.
- [ ] **Disable ADB Backup:** Set `android:allowBackup="false"` in `AndroidManifest.xml` to prevent raw database backup extraction via ADB.

---

## 4. Hardware-Backed Key Generation Example

Here is a ready-to-use utility for managing the database encryption passphrase using Android KeyStore:

```java
package com.example.vulnerablestorageapp.security;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import java.security.SecureRandom;
import java.util.Base64;

public class KeyManager {
    private static final String PREF_NAME = "secure_db_prefs";
    private static final String KEY_DB_PASSPHRASE = "db_passphrase";

    public static char[] getOrCreateDatabasePassphrase(Context context) throws Exception {
        MasterKey masterKey = new MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();

        SharedPreferences sharedPreferences = EncryptedSharedPreferences.create(
                context,
                PREF_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );

        String existingPassphrase = sharedPreferences.getString(KEY_DB_PASSPHRASE, null);
        if (existingPassphrase == null) {
            byte[] randomBytes = new byte[32];
            new SecureRandom().nextBytes(randomBytes);
            String newPassphrase = Base64.getEncoder().encodeToString(randomBytes);
            sharedPreferences.edit().putString(KEY_DB_PASSPHRASE, newPassphrase).apply();
            return newPassphrase.toCharArray();
        }

        return existingPassphrase.toCharArray();
    }
}
```
