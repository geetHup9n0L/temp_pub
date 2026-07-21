# BCrypt for Android: API Reference & Syntax Guide

This document serves as an **API Reference and Syntax Guide** for implementing BCrypt password hashing in Android Java using `at.favre.lib:bcrypt` (modern) and `org.mindrot:jbcrypt` (classic).

---

## 🏛 1. Primary Library: `at.favre.lib.crypto.bcrypt`

Package: `at.favre.lib.crypto.bcrypt`

### A. Password Hashing Syntax

#### Method: `BCrypt.withDefaults().hashToString()`

```java
public String hashToString(int cost, char[] password)
```
- **Description:** Hashes a plaintext password using default settings ($2a$ version, 128-bit secure random salt) and returns a formatted BCrypt hash string.
- **Parameters:**
  - `cost`: Log2 iterations (e.g. `10` or `12`).
  - `password`: Password provided as a `char[]` (recommended for RAM security).
- **Return Value:** A 60-character BCrypt string formatted as `$2a$10$...`

#### Example Usage:
```java
import at.favre.lib.crypto.bcrypt.BCrypt;

char[] password = new char[]{'M', 'y', 'S', 'e', 'c', 'r', 'e', 't', '1', '2', '3'};
int costFactor = 10;

// Generate BCrypt hash string
String bcryptHash = BCrypt.withDefaults().hashToString(costFactor, password);

// System output example: $2a$10$e8b.V3Z7kI0u.a...
```

---

### B. Password Verification Syntax

#### Method: `BCrypt.verifyer().verify()`

```java
public BCrypt.Result verify(char[] password, char[] bcryptHash)
public BCrypt.Result verify(char[] password, String bcryptHash)
```
- **Description:** Verifies a candidate password against an existing BCrypt hash using constant-time comparison.
- **Parameters:**
  - `password`: The plaintext password provided during login.
  - `bcryptHash`: The stored hash retrieved from the database.
- **Return Object (`BCrypt.Result`):**
  - `result.verified`: `boolean` (`true` if password matches, `false` otherwise).
  - `result.valid`: `boolean` (format check validity).

#### Example Usage:
```java
import at.favre.lib.crypto.bcrypt.BCrypt;

char[] inputPassword = "UserEnteredPassword".toCharArray();
String storedHash = "$2a$10$e8b.V3Z7kI0u.a...";

BCrypt.Result result = BCrypt.verifyer().verify(inputPassword, storedHash);

if (result.verified) {
    // Password is valid - Proceed to login
} else {
    // Authentication failed
}
```

---

## 🏛 2. Secondary Library: `org.mindrot.jbcrypt.BCrypt`

Package: `org.mindrot.jbcrypt`

### A. Salt Generation

#### Method: `BCrypt.gensalt()`
```java
public static String gensalt(int log_rounds)
```
- **Description:** Generates a random salt string with a specified cost factor.
- **Example:**
  ```java
  String salt = BCrypt.gensalt(10);
  ```

### B. Password Hashing

#### Method: `BCrypt.hashpw()`
```java
public static String hashpw(String password, String salt)
```
- **Description:** Hashes a password string with the specified salt.
- **Example:**
  ```java
  String hashedPassword = BCrypt.hashpw("UserPassword", BCrypt.gensalt(10));
  ```

### C. Password Verification

#### Method: `BCrypt.checkpw()`
```java
public static boolean checkpw(String candidate, String hashed)
```
- **Description:** Checks if a plain text candidate password matches an existing BCrypt hash.
- **Example:**
  ```java
  boolean isMatch = BCrypt.checkpw("CandidatePassword", storedHashedPassword);
  ```

---

## 🔒 3. Memory Security: Scrubbing `char[]` in Java

Unlike `String` instances which are stored in the JVM String Pool and cannot be cleared until Garbage Collection, `char[]` arrays can be overwritten explicitly immediately after use.

```java
import java.util.Arrays;
import at.favre.lib.crypto.bcrypt.BCrypt;

public static String hashPasswordSecurely(char[] passwordCharArray) {
    try {
        return BCrypt.withDefaults().hashToString(10, passwordCharArray);
    } finally {
        // Zero-out password in process memory
        Arrays.fill(passwordCharArray, '\0');
    }
}
```
