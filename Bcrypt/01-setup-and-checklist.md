# BCrypt for Android: Setup & Developer Checklist

This document details how to integrate BCrypt password hashing into an Android Java project and provides an actionable **Developer & OWASP Security Checklist**.

---

## 1. Project Dependencies & Gradle Setup

There are two primary BCrypt implementations used in Android Java development:

### Option A: `at.favre.lib:bcrypt` (Modern Standard - Recommended)
Provides high performance, side-channel protections, `char[]` array support, and strict format validation.

#### Groovy (`app/build.gradle`)
```groovy
dependencies {
    // Favre BCrypt Password Hashing Library
    implementation 'at.favre.lib:bcrypt:0.10.2'
}
```

### Option B: `org.mindrot:jbcrypt` (Classic / Lightweight)
Legacy, lightweight BCrypt library widely used in Java projects.

#### Groovy (`app/build.gradle`)
```groovy
dependencies {
    // Classic jBCrypt
    implementation 'org.mindrot:jbcrypt:0.4'
}
```

---

## 2. Developer & OWASP Security Checklist

Use this checklist when implementing BCrypt password hashing in your Android application.

### Phase A: Hashing Configuration Checklist
- [ ] **NO Plaintext Storage:** Never store raw user passwords in databases, SharedPreferences, log files, or temporary variables.
- [ ] **Do NOT Re-Invent Salting:** Allow BCrypt to generate its own cryptographically secure 128-bit random salt automatically for every password hash.
- [ ] **Tune the Cost Factor (Log-Rounds):** Choose a cost factor between `10` and `12` for mobile devices.
  - *Cost 10:* ~50-100ms per hash on modern smartphones (Optimal balance of security and UX).
  - *Cost 12:* ~300-500ms per hash (Higher security, suitable for high-security mobile banking apps).
- [ ] **Asynchronous Threading (ANR Prevention):** Never run `BCrypt.hash` on the Main/UI Thread! Always execute hashing inside a background thread pool (`ExecutorService` or `CompletableFuture`).
- [ ] **Use Safe Character Arrays (`char[]`):** Pass user password inputs as `char[]` instead of immutable `String` instances to clear process RAM immediately using `Arrays.fill(passwordChar, '\0')`.

### Phase B: OWASP MASVS Password Policy & Verification
- [ ] **Do NOT Limit Password Length Arbitrarily:** BCrypt handles input up to 72 bytes natively. Truncate or pre-hash with SHA-256 if passwords exceed 72 bytes.
- [ ] **Constant-Time Verification:** Use standard BCrypt verification methods (`BCrypt.verifyer().verify()` or `BCrypt.checkpw()`) which perform constant-time comparisons to prevent timing attacks.
- [ ] **Never Return Specific Authentication Errors:** On login failure, return a generic message ("Invalid username or password") to avoid user enumeration vulnerabilities.

---

## 3. Benchmark & Cost Factor Guidance for Android

Because mobile devices rely on battery power and mobile CPUs, choosing the cost factor requires balancing security against user experience:

| Cost Factor ($2^N$ iterations) | Iteration Count | Approx. Time on Mobile CPU | Android Suitability |
| :--- | :--- | :--- | :--- |
| **8** | 256 | ~15 ms | ⚠️ Too fast (Vulnerable to GPU cracking) |
| **10** | 1,024 | ~60 - 100 ms | ✅ **Recommended default for Android** |
| **12** | 4,096 | ~300 - 500 ms | ✅ High Security / Mobile Banking |
| **14+** | 16,384 | > 2,000 ms | ❌ Too slow (Will freeze mobile UX) |

---

## 4. Background Execution Pattern (Java `ExecutorService`)

To ensure the UI remains smooth and responsive while hashing:

```java
package com.example.vulnerablestorageapp.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.os.Handler;
import android.os.Looper;

public class AppExecutors {
    private static final ExecutorService BACKGROUND = Executors.newFixedThreadPool(4);
    private static final Handler MAIN_THREAD = new Handler(Looper.getMainLooper());

    public interface TaskCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }

    public static <T> void runInBackground(TaskSupplier<T> supplier, TaskCallback<T> callback) {
        BACKGROUND.execute(() -> {
            try {
                T result = supplier.get();
                MAIN_THREAD.post(() -> callback.onSuccess(result));
            } catch (Exception e) {
                MAIN_THREAD.post(() -> callback.onError(e));
            }
        });
    }

    public interface TaskSupplier<T> {
        T get() throws Exception;
    }
}
```
