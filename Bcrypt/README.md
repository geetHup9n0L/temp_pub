# BCrypt Password Hashing for Android: Security & Developer Reference Guide

Welcome to the comprehensive reference and developer documentation for **BCrypt Password Hashing** in Android using Java. This guide is designed in an **Android Developer Documentation style** and tailored for security interns and developers working on mobile application security (**OWASP MASVS / MASTG-AUTH & STORAGE** compliance).

---

## 📌 Context & Security Background

### OWASP MASVS / MASTG Password Storage Principles
When storing user credentials locally or authenticating users, storing passwords in **plaintext** or using **weak/fast hashing algorithms** (MD5, SHA-1, SHA-256) is a severe vulnerability.

- **Vulnerability (Plaintext / Fast Hashing):** If an attacker extracts the database (even an encrypted one) or intercepts storage, MD5/SHA-256 hashes can be cracked in seconds using precomputed Rainbow Tables or GPU hash-cracking tools (Hashcat, John the Ripper).
- **Remediation (BCrypt Adaptive Hashing):** **BCrypt** is a cryptographic hash function based on the Blowfish cipher. It incorporates a **random salt** to defend against rainbow table attacks and an adjustable **work factor (cost parameter)** that makes password brute-forcing computationally expensive for attackers.

---

## 📚 Documentation Index

1. **[01. Setup & Developer Checklist](file:///c:/Users/PC/Documents/Agent/BCrypt-Android-Guide/01-setup-and-checklist.md)**
   - Gradle dependency configuration (`at.favre.lib:bcrypt` and `jbcrypt`).
   - Actionable Developer & Security Checklist for password hashing.
   - Selecting the optimal BCrypt Cost Factor for Android mobile hardware.

2. **[02. API Reference & Syntax Guide](file:///c:/Users/PC/Documents/Agent/BCrypt-Android-Guide/02-api-reference-and-syntax.md)**
   - API Reference for `at.favre.lib.crypto.bcrypt.BCrypt` and `org.mindrot.jbcrypt.BCrypt`.
   - Method signatures (`hashToString`, `verifyer().verify`, `hashpw`, `checkpw`).
   - Secure memory handling with `char[]` and asynchronous threading off the UI Thread.

3. **[03. Vulnerable App Remediation & Integration](file:///c:/Users/PC/Documents/Agent/BCrypt-Android-Guide/03-vulnerable-to-secure-migration.md)**
   - Side-by-side code diff: Vulnerable Plaintext Auth vs. Secure BCrypt Auth.
   - Combined architecture: **BCrypt Hashing + SQLCipher Database Encryption**.
   - Automatic cost-factor migration / re-hashing strategy on user login.

---

## 🚀 Quick Summary of Hashing Algorithms

| Algorithm | Type | Salt | GPU Crack Resistance | OWASP Recommendation |
| :--- | :--- | :--- | :--- | :--- |
| **MD5 / SHA-1** | Fast Hash | No | None (Instantly crackable) | ❌ Strongly Forbidden |
| **SHA-256 / SHA-512** | Fast Hash | Manual | Very Low (Billions of hashes/sec) | ❌ Not suited for passwords |
| **BCrypt** | Adaptive Key Derivation | Automatic (128-bit) | High (Configurable CPU/time cost) | ✅ Recommended |
| **Argon2id** | Memory-hard KDF | Automatic | Extremely High | ✅ Recommended |
