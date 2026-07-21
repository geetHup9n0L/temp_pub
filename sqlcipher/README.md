# SQLCipher for Android: Security & Developer Reference Guide

Welcome to the comprehensive reference and developer documentation for **SQLCipher for Android** using Java. This documentation is structured in an **Android Developer Documentation style** and tailored for security interns and developers working on mobile application security (specifically **OWASP MASVS / MASTG-STORAGE** compliance).

---

## 📌 Context & Security Background

### OWASP MASVS/MASTG-STORAGE Overview
In mobile app security testing, **OWASP MASVS-STORAGE** focuses on ensuring sensitive user data (such as login credentials, authentication tokens, and PII) is stored securely on the mobile device.

- **Vulnerability (Plaintext Data Storage):** standard Android SQLite (`android.database.sqlite`) stores databases on internal storage (`/data/data/com.example.app/databases/`) as unencrypted, raw database files. If a device is rooted, backed up (via ADB backup), or physical access/local privilege escalation occurs, attackers can read sensitive database contents directly using standard SQLite tools (`sqlite3`).
- **Remediation (At-Rest Database Encryption):** **SQLCipher for Android** provides transparent, full-database 256-bit AES encryption for SQLite database files. All data, tables, schema, indices, and temporary files are encrypted at rest.

---

## 📚 Documentation Index

1. **[01. Setup & Developer Checklist](file:///c:/Users/PC/Documents/Agent/SQLCipher-Android-Guide/01-setup-and-checklist.md)**
   - Step-by-step setup guide for Gradle.
   - Actionable Developer & Security Checklist.
   - Secure Key Management via Android KeyStore.

2. **[02. API Reference & Syntax Guide](file:///c:/Users/PC/Documents/Agent/SQLCipher-Android-Guide/02-api-reference-and-syntax.md)**
   - Java package structure (`net.zetetic.database.sqlcipher`).
   - Class, interface, and method definitions (`SQLiteDatabase`, `SQLiteOpenHelper`, `SQLiteDatabaseHook`).
   - SQLCipher PRAGMA commands reference (`key`, `rekey`, `cipher_migrate`, `cipher_memory_security`).

3. **[03. Vulnerable App Remediation & Migration Guide](file:///c:/Users/PC/Documents/Agent/SQLCipher-Android-Guide/03-vulnerable-to-secure-migration.md)**
   - Side-by-side code diff: standard SQLite vs. SQLCipher.
   - Plaintext to encrypted database migration script (`sqlcipher_export`).
   - Hardcoded Key vs. KeyStore-backed Key comparison.

---

## 🚀 Quick Summary of Key differences

| Feature | Standard Android SQLite | SQLCipher for Android |
| :--- | :--- | :--- |
| **Package Name** | `android.database.sqlite.*` | `net.zetetic.database.sqlcipher.*` |
| **Encryption** | None (Plaintext on disk) | 256-bit AES encryption (PBKDF2 HMAC SHA-1/SHA-512) |
| **Initialization** | Automatic | Requires `System.loadLibrary("sqlcipher")` or `SQLiteDatabase.loadLibs(context)` |
| **Opening DB** | `getWritableDatabase()` | `getWritableDatabase(char[] passphrase)` |
| **Memory Security** | Standard Java heap memory | Wipes key material, optional `PRAGMA cipher_memory_security` |
