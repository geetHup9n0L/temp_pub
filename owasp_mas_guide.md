# OWASP MAS Guide — Evaluating a Banking App via Static Analysis

## Part 1: Understanding the OWASP MAS Framework

The OWASP Mobile Application Security (MAS) project has **two components** you need to understand:

```
OWASP MAS
├── MASVS  ← The STANDARD (what to check — the checklist)
│           "Mobile Application Security Verification Standard"
│
└── MASTG  ← The GUIDE (how to check — the methodology)
            "Mobile Application Security Testing Guide"
```

**Think of it like this:**
- **MASVS** = the exam paper (the questions / requirements)
- **MASTG** = the textbook (how to answer each question)

You use MASVS as your evaluation checklist, and reference MASTG for the actual testing procedures.

---

## Part 2: The 7 MASVS Categories Explained

The MASVS is divided into **7 security categories** (plus 1 privacy category). Each category has specific controls you evaluate against. Here they are:

---

### 1. MASVS-STORAGE — Data Storage

**What it means:** The app must securely store sensitive data and prevent unintentional data leaks.

| Control | Requirement |
|---------|-------------|
| **STORAGE-1** | The app securely stores sensitive data (credentials, tokens, PII are not stored in plaintext in shared preferences, databases, or logs) |
| **STORAGE-2** | The app prevents leakage of sensitive data (clipboard, backups, keyboard cache, screenshots) |

**What to look for in JADX:**
- Search for `SharedPreferences` → check if sensitive data (passwords, tokens) is stored without encryption
- Search for `SQLiteDatabase`, `Room` → check if databases are encrypted
- Search for `Log.d`, `Log.i`, `Log.e` → check if sensitive info is logged
- Check `AndroidManifest.xml` for `android:allowBackup="true"` (data leaks via backups)
- Search for `ClipboardManager` → check if sensitive data is copied to clipboard

```java
// ❌ BAD — storing token in plaintext SharedPreferences
SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
prefs.edit().putString("auth_token", token).apply();

// ✅ GOOD — using EncryptedSharedPreferences
EncryptedSharedPreferences.create("secure_prefs", masterKey, ...);
```

---

### 2. MASVS-CRYPTO — Cryptography

**What it means:** The app uses cryptography according to industry best practices (no weak algorithms, proper key management).

| Control | Requirement |
|---------|-------------|
| **CRYPTO-1** | The app employs current strong cryptography and uses it according to industry best practices |
| **CRYPTO-2** | The app performs key management according to industry best practices |

**What to look for in JADX:**
- Search for `Cipher`, `MessageDigest`, `SecretKeySpec` → identify crypto usage
- Look for weak algorithms: `DES`, `3DES`, `MD5`, `SHA1`, `RC4`, `ECB`
- Search for hardcoded keys: `"secretkey"`, `"password"`, `byte[]` arrays with fixed values
- Check for `SecureRandom` usage vs insecure random

```java
// ❌ BAD — using weak algorithm with hardcoded key
Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
SecretKeySpec key = new SecretKeySpec("mysecret".getBytes(), "DES");

// ✅ GOOD — using AES-GCM with KeyStore
Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
```

---

### 3. MASVS-AUTH — Authentication & Authorization

**What it means:** The app uses secure authentication and authorization mechanisms, and enforces them properly on both client and server side.

| Control | Requirement |
|---------|-------------|
| **AUTH-1** | The app uses secure authentication and authorization protocols and follows best practices |
| **AUTH-2** | The app performs local authentication securely |
| **AUTH-3** | The app secures sensitive operations with additional authentication |

**What to look for in JADX:**
- Search for `BiometricPrompt`, `FingerprintManager` → check biometric implementation
- Search for `KeyguardManager` → device lock verification
- Look for token handling patterns — are tokens validated? Do they expire?
- Check if sensitive operations (e.g., transfer money) require re-authentication

---

### 4. MASVS-NETWORK — Network Communication

**What it means:** The app secures all network communication and the integrity of data in transit.

| Control | Requirement |
|---------|-------------|
| **NETWORK-1** | The app secures all network traffic according to current best practices |
| **NETWORK-2** | The app performs identity pinning for all remote endpoints under the developer's control |

**What to look for in JADX:**
- Check for `network_security_config.xml` in Resources → is cleartext traffic allowed?
- Search for `http://` (not `https://`) → insecure URLs
- Search for `TrustManager`, `X509TrustManager` → is certificate validation disabled?
- Search for certificate pinning implementation (`CertificatePinner`, `OkHttp pin`)
- Check `AndroidManifest.xml` for `android:usesCleartextTraffic="true"`

```xml
<!-- ❌ BAD in AndroidManifest.xml -->
<application android:usesCleartextTraffic="true">

<!-- ✅ GOOD — network security config with pinning -->
<network-security-config>
    <domain-config>
        <domain>api.bank.com</domain>
        <pin-set>
            <pin digest="SHA-256">base64hash=</pin>
        </pin-set>
    </domain-config>
</network-security-config>
```

---

### 5. MASVS-PLATFORM — Platform Interaction

**What it means:** The app uses platform-provided security mechanisms correctly and protects IPC (inter-process communication).

| Control | Requirement |
|---------|-------------|
| **PLATFORM-1** | The app uses IPC mechanisms securely |
| **PLATFORM-2** | The app uses WebViews securely |
| **PLATFORM-3** | The app uses the user interface securely |

**What to look for in JADX:**
- Check `AndroidManifest.xml` for `exported="true"` activities/services without proper permissions
- Search for `WebView` → check if JavaScript is enabled (`setJavaScriptEnabled(true)`)
- Search for `addJavascriptInterface` → dangerous bridge between JS and native code
- Search for `Intent`, `PendingIntent` → check for intent injection vulnerabilities
- Check for `FLAG_SECURE` on sensitive screens (prevents screenshots)

```xml
<!-- ❌ BAD — exported component without protection -->
<activity android:name=".TransferActivity" android:exported="true"/>

<!-- ✅ GOOD — protected with permission -->
<activity android:name=".TransferActivity"
    android:exported="false"/>
```

---

### 6. MASVS-CODE — Code Quality

**What it means:** The app is up-to-date, properly signed, and has basic security hardening applied.

| Control | Requirement |
|---------|-------------|
| **CODE-1** | The app requires an up-to-date platform version |
| **CODE-2** | The app has a mechanism for enforcing app updates |
| **CODE-3** | The app only uses software components without known vulnerabilities |
| **CODE-4** | The app validates and sanitizes all untrusted inputs |

**What to look for in JADX:**
- Check `AndroidManifest.xml` for `minSdkVersion` → is it too old?
- Identify third-party libraries → check for known CVEs (this is your vendor identification work!)
- Search for `WebView.loadUrl()` with user input → potential XSS
- Check for input validation on forms

---

### 7. MASVS-RESILIENCE — Anti-Reverse Engineering & Tampering

**What it means:** The app detects and responds to tampering, reverse engineering, and running in hostile environments.

| Control | Requirement |
|---------|-------------|
| **RESILIENCE-1** | The app validates the integrity of the platform |
| **RESILIENCE-2** | The app implements anti-tampering mechanisms |
| **RESILIENCE-3** | The app implements anti-static analysis mechanisms |
| **RESILIENCE-4** | The app implements anti-dynamic analysis mechanisms |

**What to look for in JADX:**
- Search for root detection: `su`, `Superuser`, `Magisk`, `test-keys`
- Search for emulator detection: `Build.FINGERPRINT`, `goldfish`, `generic`
- Search for debugger detection: `Debug.isDebuggerConnected()`
- Search for integrity checks: `PackageManager.GET_SIGNATURES`
- Check if code obfuscation is present (yes, the obfuscation you're seeing IS a resilience measure!)

---

### 8. MASVS-PRIVACY — Privacy (Newer Addition)

| Control | Requirement |
|---------|-------------|
| **PRIVACY-1** | The app minimizes access to sensitive data and resources |
| **PRIVACY-2** | The app prevents identification of the user |
| **PRIVACY-3** | The app is transparent about data collection and usage |
| **PRIVACY-4** | The app offers user control over their data |

**What to look for:** Check permissions in manifest — does the app request more than it needs? Look for tracking SDKs (Firebase Analytics, Facebook SDK, Adjust, etc.)

---

## Part 3: How to Evaluate When the Code is Obfuscated

> [!IMPORTANT]
> **You do NOT need to fully reverse-engineer every obfuscated class.** Most MASVS categories can be evaluated using readable artifacts that survive obfuscation.

### What Obfuscation Actually Does (and Doesn't Do)

```
What ProGuard/R8 obfuscates:         What it CANNOT obfuscate:
─────────────────────────────         ─────────────────────────
✗ Class names → a.b.c                ✓ AndroidManifest.xml (always readable)
✗ Method names → a(), b()            ✓ resources.arsc / res/ (XML, images)
✗ Field names → f1, f2               ✓ assets/ (config files, web content)
✗ Control flow (partially)           ✓ String literals (mostly preserved!)
                                     ✓ API calls to Android framework
                                     ✓ API calls to third-party SDKs
                                     ✓ Native library names (.so files)
                                     ✓ network_security_config.xml
                                     ✓ Third-party SDK packages (often unobfuscated)
```

### Your Analysis Strategy — What Survives Obfuscation

Here's how to evaluate each MASVS category **even with heavy obfuscation**:

---

#### MASVS-STORAGE — ✅ Mostly Evaluable

Even in obfuscated code, **Android API calls remain readable**:

```java
// Even in obfuscated class a.b.c, JADX shows:
public void a(String str) {
    // These API names are NEVER obfuscated:
    SharedPreferences sharedPreferences = this.f123a.getSharedPreferences("prefs", 0);
    sharedPreferences.edit().putString("token", str).apply();
    Log.d("TAG", "saved: " + str);
}
```

**Search in JADX (Ctrl+Shift+F) for:**
```
SharedPreferences        → find all local storage
getSharedPreferences     → same
SQLiteDatabase           → database usage
openOrCreateDatabase     → database usage
Log.d   Log.i   Log.e   → logging
Log.v   Log.w           → more logging
MODE_WORLD_READABLE      → insecure file mode
ClipboardManager         → clipboard usage
```

> [!TIP]
> Android framework class names and method names (`SharedPreferences`, `Log.d`, etc.) can NEVER be obfuscated because they're part of the Android SDK, not the app's code. JADX will always show them in full.

---

#### MASVS-CRYPTO — ✅ Mostly Evaluable

Crypto APIs are Android/Java framework calls — always visible:

```java
// Even in obfuscated class, crypto calls are readable:
Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");  // ← algorithm visible!
MessageDigest md = MessageDigest.getInstance("MD5");           // ← weak hash visible!
SecretKeySpec key = new SecretKeySpec(bArr, "DES");            // ← weak algo visible!
```

**Search for:**
```
Cipher.getInstance       → find all encryption
MessageDigest            → find all hashing
SecretKeySpec            → find key creation
"DES"  "MD5"  "SHA1"     → find weak algorithms
"ECB"                    → find weak block mode
SecureRandom             → find random number generation
KeyStore                 → find key storage
"AndroidKeyStore"        → find hardware-backed keys
```

---

#### MASVS-NETWORK — ✅ Fully Evaluable (No Code Needed!)

This is almost entirely in **Resources** (which you confirmed are readable):

1. **`AndroidManifest.xml`** → check `usesCleartextTraffic`
2. **`res/xml/network_security_config.xml`** → full network security policy (pinning, cleartext rules)
3. **Search source for** `http://` → find insecure URLs (string literals survive obfuscation)
4. **Search for** `TrustManager` → find certificate validation overrides

---

#### MASVS-PLATFORM — ✅ Fully Evaluable

Almost entirely from **AndroidManifest.xml**:

```xml
<!-- All of this is always in cleartext, never obfuscated: -->
<activity android:exported="true" ... />
<receiver android:exported="true" ... />
<provider android:exported="true" ... />
<intent-filter> ... </intent-filter>
<uses-permission android:name="..." />
```

For WebView analysis, search for:
```
WebView                  → find WebView usage
setJavaScriptEnabled     → check if JS enabled
addJavascriptInterface   → dangerous JS bridge
loadUrl                  → what URLs are loaded
```

---

#### MASVS-CODE — ✅ Evaluable (This IS Your Vendor Task)

- **`minSdkVersion`, `targetSdkVersion`** → in `AndroidManifest.xml` (readable)
- **Third-party libraries** → vendor SDK packages are typically NOT obfuscated (their ProGuard rules preserve public APIs)
- **Known vulnerable libraries** → identify the library, check its version, look up CVEs

---

#### MASVS-RESILIENCE — ✅ Evaluable

Ironically, the fact that the code IS obfuscated is itself a **positive finding** for this category:

```
Obfuscated code?          → ✅ RESILIENCE-3 (anti-static analysis) is implemented
Root detection present?   → Search for "su", "Superuser", "Magisk", "test-keys"
Emulator detection?       → Search for "goldfish", "generic", "Build.FINGERPRINT"
Debugger detection?       → Search for "isDebuggerConnected"
Integrity checking?       → Search for "GET_SIGNATURES", "PackageInfo"
```

These are **string literals** — they survive obfuscation.

---

#### MASVS-PRIVACY — ✅ Evaluable

- **Permissions** → all in `AndroidManifest.xml`
- **Tracking SDKs** → identifiable by package names (analytics vendors don't obfuscate their SDK namespaces)

---

### Summary: What You MUST Look At

```
Priority 1 — AndroidManifest.xml (readable, ALWAYS)
├── Covers: STORAGE, NETWORK, PLATFORM, CODE, PRIVACY
├── Permissions, exported components, backup settings,
│   cleartext traffic, SDK versions, intent filters
│
Priority 2 — Resources (readable, you confirmed)
├── Covers: NETWORK, PLATFORM, PRIVACY
├── network_security_config.xml, layouts, strings.xml
│
Priority 3 — String Search in Source Code (survives obfuscation)
├── Covers: ALL CATEGORIES
├── Search for API names, URLs, crypto algorithms,
│   vendor names, log calls, hardcoded secrets
│
Priority 4 — Third-Party SDK Packages (usually unobfuscated)
├── Covers: CODE, PRIVACY
├── Vendor namespace identification (your main assignment)
│
Priority 5 — Native Libraries (.so files)
├── Covers: CODE, RESILIENCE
├── Library names, exported symbols, strings
```

---

## Part 4: Practical JADX Cheat Sheet for Your Assessment

### Searches to Run for Each MASVS Category

Copy these into JADX's text search (`Ctrl+Shift+F`):

```
=== MASVS-STORAGE ===
SharedPreferences
getSharedPreferences
MODE_WORLD_READABLE
MODE_WORLD_WRITEABLE
SQLiteDatabase
openOrCreateDatabase
Log.d(
Log.i(
Log.e(
Log.v(
Log.w(
ClipboardManager
allowBackup
EncryptedSharedPreferences
getExternalStorage

=== MASVS-CRYPTO ===
Cipher.getInstance
MessageDigest.getInstance
SecretKeySpec
"DES"
"MD5"
"SHA1"
"ECB"
"RC4"
"AES"
SecureRandom
AndroidKeyStore
KeyStore.getInstance
PBKDF2
hardcoded (look for byte[] constants)

=== MASVS-NETWORK ===
http://
https://
usesCleartextTraffic
CertificatePinner
TrustManager
X509TrustManager
checkServerTrusted
HostnameVerifier
ALLOW_ALL_HOSTNAME_VERIFIER
network_security_config
SSLSocket
OkHttpClient

=== MASVS-PLATFORM ===
WebView
setJavaScriptEnabled
addJavascriptInterface
loadUrl
exported
PendingIntent
FLAG_SECURE
intent-filter
deeplink

=== MASVS-CODE ===
minSdkVersion
targetSdkVersion
(check third-party package tree for vulnerable lib versions)

=== MASVS-RESILIENCE ===
isDebuggerConnected
Debug.
/su
Superuser
Magisk
test-keys
Build.FINGERPRINT
goldfish
generic
rooted
SafetyNet
PlayIntegrity
GET_SIGNATURES
PackageInfo

=== MASVS-PRIVACY ===
(check manifest permissions)
getDeviceId
getSubscriberId
ANDROID_ID
AdvertisingId
getSimSerialNumber
```

---

## Part 5: Report Structure Suggestion

For each MASVS category in your report:

```markdown
## MASVS-STORAGE Assessment

### Control: MASVS-STORAGE-1 — Secure Storage of Sensitive Data

**Finding:** The app stores authentication tokens in SharedPreferences
without encryption.

**Evidence:**
- [Screenshot of JADX showing SharedPreferences usage in class a.b.c]
- AndroidManifest.xml shows android:allowBackup="true"

**Severity:** High

**Recommendation:** Use EncryptedSharedPreferences or Android KeyStore

---
(repeat for each control)
```

---

## Key Takeaway

> [!IMPORTANT]
> **You do NOT need to reverse-engineer the obfuscated code line by line.** The vast majority of your OWASP MAS assessment can be done through:
> 1. Reading `AndroidManifest.xml` and resource files (always readable)
> 2. Searching for Android framework API calls (never obfuscated)
> 3. Searching for string literals (mostly preserved through obfuscation)
> 4. Identifying third-party SDK packages (usually unobfuscated)
>
> The obfuscation makes it harder to understand the app's **business logic**, but your job is to assess **security controls**, which are implemented using Android APIs that remain visible.
