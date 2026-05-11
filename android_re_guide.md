# Android App Reverse Engineering for Vendor/Library Analysis

## The Big Picture

Your goal is: **Given an Android app (APK), identify which third-party vendors/SDKs power a specific sub-feature (e.g., eKYC, biometric auth, payment gateway, chat, etc.), and collect evidence.**

This is essentially **software composition analysis (SCA)** combined with **static reverse engineering**.

---

## 1. Understanding the Target: What is an APK?

An APK (Android Package) is just a **ZIP file** with a specific structure:

```
app.apk (ZIP archive)
├── AndroidManifest.xml      ← App metadata, permissions, components (encoded binary XML)
├── classes.dex               ← Compiled Java/Kotlin bytecode (Dalvik Executable)
├── classes2.dex              ← Additional DEX files (multidex apps)
├── res/                      ← Resources (layouts, images, strings)
├── assets/                   ← Raw assets (configs, ML models, web content)
├── lib/                      ← Native libraries (.so files — this is your ELF experience!)
│   ├── armeabi-v7a/
│   ├── arm64-v8a/
│   └── x86_64/
├── META-INF/                 ← Signing info, certificates
└── resources.arsc            ← Compiled resource table
```

> [!TIP]
> The `.so` files under `lib/` are standard ELF shared libraries — your CTF RE skills apply directly here! You can analyze them with Ghidra, IDA, or radare2 just like any other ELF binary.

---

## 2. Essential Tools

### Primary Decompilation Tools

| Tool | Purpose | Output | Link |
|------|---------|--------|------|
| **JADX** | DEX → Java source (decompiler) | Readable `.java` files | [github.com/skylot/jadx](https://github.com/skylot/jadx) |
| **apktool** | Decode resources & smali | Decoded XML, smali code | [github.com/iBotPeaches/Apktool](https://github.com/iBotPeaches/Apktool) |
| **APKLab** | VS Code extension wrapping jadx + apktool | Integrated RE in VS Code | VS Code Marketplace |
| **Bytecode Viewer** | Multi-decompiler GUI | Java source via multiple engines | [github.com/AstroViking/bytecodeviewer](https://github.com/AstroViking/bytecodeviewer) |

### For Native Libraries (.so files)

| Tool | Purpose |
|------|---------|
| **Ghidra** (free, NSA) | Full-featured disassembler/decompiler for ELF |
| **IDA Free/Pro** | Industry standard disassembler |
| **radare2 / Cutter** | Open-source RE framework |

### Supporting Tools

| Tool | Purpose |
|------|---------|
| **dex2jar** | Convert DEX to JAR (for use with JD-GUI) |
| **JD-GUI** | Java decompiler GUI (view JAR files) |
| **Frida** | Dynamic instrumentation (runtime hooking) |
| **MobSF** | Automated mobile security framework (static + dynamic) |
| **strings** / **grep** | Quick string extraction from binaries |
| **APKiD** | Identifies packers, obfuscators, and protections used |

> [!IMPORTANT]
> **For your use case (vendor identification), JADX is your #1 tool.** It provides the most readable Java/Kotlin decompilation and has excellent search capabilities. Start here.

---

## 3. Environment Setup

### Option A: Windows (Your Current Setup) — Recommended to Start

```
1. Install Java JDK 11+ (required by JADX and apktool)
   → https://adoptium.net/

2. Download JADX GUI (portable, no install needed)
   → https://github.com/skylot/jadx/releases
   → Extract jadx-gui.exe and run it

3. Download apktool
   → https://apktool.org/docs/install
   → Place apktool.bat and apktool.jar in your PATH

4. Install Ghidra (for .so analysis, you already may have this from CTF)
   → https://ghidra-sre.org/
```

### Option B: Linux VM (More Powerful, Recommended for Dynamic Analysis)

```
1. Set up a Kali Linux or Ubuntu VM
2. Install tools:
   sudo apt install jadx apktool dex2jar default-jdk
   pip3 install frida-tools
3. For dynamic analysis, use Android Emulator (Android Studio) or Genymotion
```

### Option C: Use MobSF for Automated Analysis

```
# Docker (easiest)
docker run -it --rm -p 8000:8000 opensecurity/mobile-security-framework-mobsf
# Then upload APK via browser at http://localhost:8000
```

> [!NOTE]
> For **static analysis only** (which is your primary task), Windows + JADX is perfectly sufficient. You don't need a full Android hacking lab.

---

## 4. How to Obtain the APK

### Method 1: From the Device (if app is installed)
```bash
# Connect device via ADB
adb devices

# Find the package name
adb shell pm list packages | grep <keyword>

# Get the APK path
adb shell pm path com.example.bankapp

# Pull the APK
adb pull /data/app/com.example.bankapp-1/base.apk ./app.apk
```

### Method 2: From Third-Party APK Mirror Sites
- [APKMirror](https://www.apkmirror.com/) — Trusted, verified signatures
- [APKPure](https://apkpure.com/)
- [Evozi APK Downloader](https://apps.evozi.com/apk-downloader/) — Downloads from Play Store

### Method 3: From Google Play (with tools)
```bash
# Using gplaydl or similar tools
pip install gplaydl
gplaydl -d com.example.bankapp
```

> [!WARNING]
> **Split APKs / App Bundles**: Modern apps often use Android App Bundles (AAB), which means the APK is split into multiple files (base.apk + split configs). You may need to pull all splits and merge them, or use a tool like **SAI (Split APK Installer)** to get the full app. APKMirror provides bundled APKs (XAPK/APKM) that include all splits.

---

## 5. Step-by-Step: Static Analysis for Vendor Identification

### Step 1: Initial Recon with JADX

```
1. Open JADX-GUI
2. File → Open → Select your APK
3. Wait for decompilation (can take minutes for large banking apps)
4. You now have a browsable source tree
```

### Step 2: Identify Third-Party Libraries by Package Names

In JADX, expand the **Source Code** tree. Third-party SDKs are identifiable by their **Java package names**:

```
com.example.bankapp          ← The app's own code
├── com.google.firebase       ← Firebase (Google)
├── com.facebook.sdk          ← Facebook SDK
├── io.sentry                 ← Sentry (error tracking)
├── com.jumio                 ← Jumio (eKYC vendor!)
├── com.onfido                ← Onfido (eKYC vendor!)
├── com.facetec              ← FaceTec (liveness detection!)
├── com.visa.checkout         ← Visa SDK
├── com.stripe               ← Stripe payments
├── okhttp3                   ← OkHttp (networking)
├── retrofit2                 ← Retrofit (API client)
├── com.airbnb.lottie        ← Lottie animations
├── org.bouncycastle         ← Bouncy Castle (crypto)
└── ...
```

> [!TIP]
> **This is your primary evidence.** Screenshot the package tree showing the vendor's SDK packages present in the app. The package namespace (`com.vendorname.sdk`) directly maps to a vendor.

### Step 3: Examine `AndroidManifest.xml`

```xml
<!-- Look for vendor-specific activities, services, receivers -->
<activity android:name="com.jumio.sdk.JumioActivity" />
<service android:name="com.onfido.sdk.capture.CaptureService" />

<!-- Look for vendor-specific permissions -->
<uses-permission android:name="com.vendor.SPECIAL_PERMISSION" />

<!-- Look for API keys in metadata -->
<meta-data android:name="com.vendor.API_KEY" android:value="..." />
```

### Step 4: Search for Vendor Indicators

Use JADX's **search** feature (`Ctrl+Shift+F` for text search):

| Search For | Why |
|-----------|-----|
| Vendor name (e.g., "Jumio", "Onfido", "FaceTec") | Direct references |
| SDK version strings (e.g., "SDK_VERSION", "sdkVersion") | Version evidence |
| API endpoints (e.g., "api.vendor.com") | Network communication |
| License strings (e.g., "LICENSE", "Copyright") | Attribution |
| Known class names from vendor docs | Confirm SDK integration |

### Step 5: Analyze Native Libraries (.so files)

```bash
# Extract the APK (it's just a ZIP)
unzip app.apk -d app_extracted

# List native libraries
ls app_extracted/lib/arm64-v8a/

# Example output:
# libjumio_native.so        ← Vendor-specific native lib
# libfacetec.so              ← FaceTec native lib
# libflutter.so              ← Flutter framework (if Flutter app)
# libc++_shared.so           ← Standard C++ lib
```

```bash
# Extract strings from .so files for clues
strings libjumio_native.so | grep -i "jumio\|version\|copyright"

# Use readelf to check dependencies
readelf -d libjumio_native.so
```

> [!NOTE]
> Native library names are strong evidence. A file named `libonfido.so` or `libfacetec.so` is a clear indicator of that vendor's SDK.

### Step 6: Check `assets/` and `res/` Directories

```
assets/
├── vendor_config.json         ← SDK configuration files
├── ml_models/                 ← ML models (often from eKYC vendors)
│   └── face_detection.tflite
├── vendor_terms.html          ← Vendor's T&C
└── ...

res/
├── layout/
│   └── activity_vendor_*.xml  ← UI layouts from vendor SDK
├── drawable/
│   └── vendor_logo.png        ← Vendor branding assets
└── values/
    └── strings.xml            ← May contain vendor-related strings
```

### Step 7: Analyze Gradle Dependencies (if available)

Sometimes you can find build artifacts or dependency info:

```bash
# Search for build config or dependency declarations
grep -r "implementation\|compile\|dependency" app_extracted/ 2>/dev/null

# Look for version info in BuildConfig classes
# In JADX: navigate to com.example.app.BuildConfig
```

---

## 6. Mapping Sub-Features to Vendors — Common Patterns

Here's a reference for common sub-products and the vendors typically behind them:

### eKYC / Identity Verification
| Vendor | Package Name Pattern | Key Indicators |
|--------|---------------------|----------------|
| Jumio | `com.jumio.*` | `libJumio*.so`, JumioActivity |
| Onfido | `com.onfido.*` | OnfidoConfig, CaptureActivity |
| FaceTec | `com.facetec.*` | `libfacetec.so`, FaceTecSDK |
| Veriff | `com.veriff.*` | VeriffSdk, VeriffActivity |
| Sumsub | `com.sumsub.*` | SNSMobileSDK |
| Liveness / iProov | `com.iproov.*` | IProov class |

### Payment / Financial
| Vendor | Package Name Pattern |
|--------|---------------------|
| Stripe | `com.stripe.*` |
| Braintree | `com.braintreepayments.*` |
| Adyen | `com.adyen.*` |
| PayPal | `com.paypal.*` |

### Analytics / Crash Reporting
| Vendor | Package Name Pattern |
|--------|---------------------|
| Firebase | `com.google.firebase.*` |
| Sentry | `io.sentry.*` |
| Amplitude | `com.amplitude.*` |
| Mixpanel | `com.mixpanel.*` |

### Push Notifications
| Vendor | Package Name Pattern |
|--------|---------------------|
| Firebase Cloud Messaging | `com.google.firebase.messaging.*` |
| OneSignal | `com.onesignal.*` |
| Braze (Appboy) | `com.braze.*` / `com.appboy.*` |

---

## 7. Collecting Evidence for Your Report

For each vendor identified, document:

| Evidence Type | How to Collect | Tool |
|--------------|----------------|------|
| **Package namespace** | Screenshot the package tree in JADX | JADX-GUI |
| **Class files** | Screenshot key classes (e.g., `VendorSDK.init()`) | JADX-GUI |
| **AndroidManifest entries** | Copy relevant XML snippets | apktool / JADX |
| **Native library names** | List `.so` files with vendor names | File explorer / `ls` |
| **String references** | Search results for vendor name / URLs | JADX search / `strings` |
| **Network endpoints** | Find API URLs pointing to vendor domains | JADX search / `grep` |
| **Configuration files** | Extract from `assets/` directory | unzip / apktool |
| **SDK version** | Find version constants in decompiled code | JADX-GUI |
| **UI resources** | Vendor-branded layouts, images | apktool (decoded res/) |

> [!IMPORTANT]
> **Screenshot everything.** Your report should include visual evidence of each finding. JADX-GUI makes this easy — just screenshot the relevant code/tree views.

---

## 8. Dealing with Obfuscation (ProGuard / R8)

Banking apps **will** use code obfuscation. You'll see code like:

```java
// Obfuscated
public class a {
    public void b(String c) {
        d.e(c, "f");
    }
}
```

**How to work around this:**

1. **Focus on third-party SDKs** — Many vendors ship their SDK **without obfuscation** (or with their own separate ProGuard config that keeps public API names). The app's own code will be obfuscated, but vendor SDK packages often retain readable names.

2. **String analysis** — Strings are rarely obfuscated. Search for:
   - Vendor names, URLs, API keys
   - Error messages (often contain vendor identifiers)
   - Log tags

3. **Resource-based identification** — Resource files (`res/`, `assets/`) are generally not obfuscated.

4. **Native libraries** — `.so` file names and their exported symbols are usually not obfuscated.

5. **Use APKiD** to identify what obfuscation/packing is used:
   ```bash
   pip install apkid
   apkid app.apk
   ```

---

## 9. Practical Walkthrough Example

Let's say your task is: **"Identify the eKYC vendor in BankApp XYZ"**

```
Step 1: Download APK from APKMirror
Step 2: Open in JADX-GUI
Step 3: Browse package tree → spot "com.facetec" namespace
Step 4: Open com.facetec.sdk.FaceTecSDK → find version string "9.6.73"
Step 5: Search "facetec" → find initialization in com.bankapp.kyc.KycManager
Step 6: Check AndroidManifest.xml → find FaceTecActivity declared
Step 7: Check lib/arm64-v8a/ → find libfacetec.so
Step 8: Check assets/ → find facetec ML models

Evidence summary:
 ✓ Package: com.facetec.sdk (screenshot)
 ✓ Version: 9.6.73 (screenshot of constant)
 ✓ Integration point: KycManager.startVerification() calls FaceTecSDK.init()
 ✓ Native lib: libfacetec.so present
 ✓ Manifest: FaceTecActivity registered
 → Conclusion: eKYC liveness detection is powered by FaceTec SDK v9.6.73
```

---

## 10. Legal & Ethical Considerations

> [!CAUTION]
> - **Only analyze apps you have authorization to analyze.** Since this is an internship assignment, ensure your company has proper authorization.
> - **Do not redistribute** decompiled code or APKs.
> - **Do not bypass security controls** (certificate pinning, root detection) unless explicitly authorized.
> - **Do not access live backend systems** — stick to static analysis.
> - **Document your methodology** in your report to show you followed a legitimate process.
> - Reverse engineering for **interoperability and security research** is generally permissible, but always follow your company's legal guidance.

---

## 11. Quick-Start Checklist

```
[ ] Install Java JDK 11+
[ ] Download JADX-GUI (latest release)
[ ] Download apktool
[ ] Obtain the target APK (from device or APKMirror)
[ ] Open APK in JADX-GUI
[ ] Browse package tree for vendor namespaces
[ ] Search for vendor names, URLs, version strings
[ ] Check AndroidManifest.xml for vendor components
[ ] List native .so libraries for vendor-specific files
[ ] Inspect assets/ for config files and models
[ ] Screenshot and document all findings
[ ] Compile evidence into your report
```

---

## 12. Recommended Learning Resources

- **[OWASP Mobile Testing Guide (MASTG)](https://mas.owasp.org/MASTG/)** — The gold standard for mobile app security testing methodology
- **[JADX Documentation](https://github.com/skylot/jadx/wiki)** — How to use JADX effectively
- **[Android Internals](https://newandroidbook.com/)** — Deep dive into Android architecture
- **[maddiestone's Android RE Workshop](https://github.com/maddiestone/AndroidAppRE)** — Beginner-friendly Android RE training by a Google Project Zero researcher
