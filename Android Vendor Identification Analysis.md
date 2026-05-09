# Chat Conversation

Note: _This is purely the output of the chat conversation and does not contain any raw data, codebase snippets, etc. used to generate the output._

### User Input

I have an assignment for my internship, is that I have to indentify what vendors are liable behind a subproduct of the whole product (a functionality inside an app) and I have to provide evidences in the final report. The approach that they told me is divided into certain steps, the basic reconnaisance and the technical analysis. The former can easily be done as its just doing osint (googling for information and installing the app for verification and surface analysis). The difficult part lies in the former part which demands technical skills, which I have to inspect the code of the app, analyze its libraries used, the functions and codes which involves the subproduct that we mentioned (we have to verify its presence accordingly) and this is considered evidence as well. The problem is I dont have any prior skill to android hacking, I only have experienced some RE skills with ELF file from CTF so I dont know how can we have a decompiled code of the app (and a legitmate banking app), what environment for the process and so on. Please give me insights and information.

### Planner Response

Great question — this is a very practical and common task in vendor/supply-chain analysis for mobile apps. Since you already have some RE fundamentals from CTF, you'll pick this up quickly. Let me put together a comprehensive guide for you.

*Edited relevant file*

### Planner Response

Here's a comprehensive guide I've put together. The key takeaways for your situation:

### TL;DR — You're Closer Than You Think

1. **Your ELF RE skills transfer directly** — Android apps contain `.so` (ELF shared libraries) under `lib/`, and you can analyze them with the same tools (Ghidra, radare2).

2. **The Java/Kotlin side is actually easier** — Unlike binary RE, you get near-source-code quality output from **JADX**, which decompiles Dalvik bytecode back to readable Java.

3. **Vendor identification is mostly pattern matching** — Third-party SDKs reveal themselves through their Java package namespaces (`com.vendorname.sdk`), native library names (`libvendor.so`), manifest entries, and string references. You don't need deep exploitation skills for this.

### Your Minimum Viable Setup (Windows)
- **Java JDK 11+** → Required runtime
- **JADX-GUI** → Your primary tool. Open the APK, browse the decompiled source tree, and search for vendor indicators
- **apktool** → For decoded resources and manifest

### The Core Workflow
1. Get the APK (from device via `adb pull` or from APKMirror)
2. Open it in JADX-GUI
3. Browse the package tree — vendor SDKs have recognizable namespaces
4. Search for vendor names, API URLs, version strings
5. Check native `.so` library names, `assets/`, and `AndroidManifest.xml`
6. Screenshot everything for your report

The full guide covers obfuscation handling, common vendor package patterns (eKYC, payments, analytics), evidence collection templates, and learning resources. Let me know if you want me to dive deeper into any specific area!

### User Input

If the app is a legitmate banking app, which is modern and recent that can only be installed from Google Play. What method should I use amongst the 3 methods you mentioned?

### Planner Response

Great question — this matters a lot because modern Play Store apps have specific delivery quirks.

## Short Answer: **Method 1 (ADB from your device)** is your best bet

Here's why, and why the other two are problematic for your case:

### Method 2 (APK Mirror Sites) — Maybe, but check first
- APKMirror is trustworthy and often has banking apps, but **many banks request exclusion** from these sites
- If your target banking app is regional or niche, it may simply not be listed
- Worth checking first since it's the easiest — but don't count on it

### Method 3 (gplaydl / Play Store downloaders) — Unreliable
- These tools frequently **break** because Google constantly changes their API
- They require your Google account auth tokens, which is risky
- Many simply don't work anymore in 2026

### Method 1 (ADB Pull from Device) — ✅ Most Reliable

This is the standard approach professionals use. Install the app on a **real Android phone** you own, then pull it:

```bash
# 1. Enable Developer Options + USB Debugging on your phone
#    Settings → About Phone → Tap "Build Number" 7 times
#    Settings → Developer Options → Enable USB Debugging

# 2. Connect phone via USB, verify connection
adb devices

# 3. Find the package name
adb shell pm list packages | findstr bank

# 4. Get the APK path(s)
adb shell pm path com.example.bankapp

# 5. Pull the APK(s)
adb pull /data/app/~~randomstring/com.example.bankapp/base.apk ./base.apk
```

### ⚠️ The Split APK Problem

Here's the catch — modern banking apps from Play Store almost always use **Android App Bundles (AAB)**, which means when you run `pm path`, you'll see **multiple files**:

```
package:/data/app/~~abc123/com.bank.app/base.apk
package:/data/app/~~abc123/com.bank.app/split_config.arm64_v8a.apk
package:/data/app/~~abc123/com.bank.app/split_config.xxhdpi.apk
package:/data/app/~~abc123/com.bank.app/split_config.en.apk
```

**Pull all of them:**
```bash
# Pull the entire directory
adb pull /data/app/~~abc123/com.bank.app/ ./bank_app_splits/
```

**The good news:** JADX handles this fine — you can open `base.apk` directly, which contains **all the code** (DEX files, manifest, libraries). The split APKs are mostly density/architecture/language-specific resources. For vendor identification, `base.apk` alone is usually sufficient.

### What You Need
- Your personal Android phone (or a colleague's)
- A USB cable
- **ADB** — install via Android SDK Platform Tools:
  ```
  # Download from: https://developer.android.com/tools/releases/platform-tools
  # Extract, add to PATH, done.
  ```

This is the cleanest and most legally defensible method too — you're analyzing an app you legitimately installed from the official source, which strengthens your report's credibility.

Want me to walk you through the full ADB setup on Windows?

### User Input

I have some question before putting my hands on the technical stuff is that in terms of Legal and Ethical considerations that you mentioned. The assignment of mine states that: "Nghiên cứu tài liệu kỹ thuật/SDK: Tìm kiếm các thư viện (libraries) hoặc SDK mà ứng dụng sử dụng thông qua các bài viết kỹ thuật hoặc phân tích gói ứng dụng " The former is through reading articles related to technicals and the other is analyzing code. My concern is that, we have the ability to analyze the code from a modern legimate installed app from Google play (right?), but when analyzing, does the app or its backend system can log or alert because of someone (me) trying to access the internal code? Im kinda worried this part, so could you give ur insights. 

### User Input

Continue

### Planner Response

This is a very valid concern, and I'm glad you're thinking about it. Let me put your mind at ease with a clear technical explanation.

## The Short Answer: **No. Static analysis is completely invisible to the app and its backend.**

## Why? Understanding Static vs. Dynamic Analysis

There are two fundamentally different categories of analysis:

| | Static Analysis | Dynamic Analysis |
|---|---|---|
| **What it is** | Analyzing the APK **file** offline on your computer | **Running** the app and interacting with it live |
| **Network activity** | ❌ Zero — no internet needed | ✅ Yes — app talks to its servers |
| **Detectable?** | ❌ **Impossible to detect** | ⚠️ Potentially detectable |
| **Your task** | ✅ **This is what you're doing** | Not required for your assignment |

### What Happens When You Use JADX

```
You pull the APK from your phone
         ↓
The APK sits on YOUR computer as a local file (like a .zip)
         ↓
You open it in JADX (a Java program running on YOUR machine)
         ↓
JADX reads the file bytes and decompiles DEX → Java
         ↓
You browse the decompiled source code in JADX's GUI
```

**At no point in this entire process does:**
- Your computer contact the bank's servers
- The app "know" it's being analyzed
- Any alert get triggered anywhere
- Any log get created on the backend

It's literally the same as opening a `.zip` file and reading its contents. The APK is just a file sitting on your disk. JADX is a fully offline tool — it doesn't even need internet to function.

### Think of it this way

Imagine you bought a physical book from a bookstore. You bring it home, open it, and read it. The bookstore and the author have **no way of knowing** that you're reading page 47 right now. That's exactly what static analysis is — you have a copy of the file, and you're reading it locally.

### When **would** detection be possible?

Only during **dynamic analysis**, which is NOT what your assignment requires:

| Activity | Risk | Your Assignment? |
|----------|------|:---:|
| Decompiling APK in JADX | None — fully offline | ✅ Yes |
| Reading decompiled code | None — it's a local file | ✅ Yes |
| Searching strings in APK | None — file operations | ✅ Yes |
| Running the app on a rooted phone | App may detect root & refuse to run | ❌ No |
| Intercepting network traffic (MITM proxy) | App's cert pinning may detect & log | ❌ No |
| Hooking functions with Frida | Anti-tampering may detect | ❌ No |

## Regarding Your Assignment Scope

Your assignment text says:

> *"Tìm kiếm các thư viện (libraries) hoặc SDK mà ứng dụng sử dụng thông qua các bài viết kỹ thuật hoặc **phân tích gói ứng dụng**"*

"Phân tích gói ứng dụng" = analyzing the application package. This is explicitly authorizing you to do static analysis on the APK file. You're doing exactly what they asked — inspecting what libraries/SDKs are bundled inside the app package.

## Summary

> **You are safe.** Pulling an APK and decompiling it with JADX is a completely offline, undetectable operation. No backend system can log or alert on it because there is zero network interaction. This is standard practice in the security industry and is precisely what your assignment is asking you to do.

Go ahead and get your hands dirty — no one will know except your report readers. 🙂 Want me to walk you through the ADB + JADX setup on your Windows machine?

### User Input

does JADX has a built in Anroid? Im thinking of either having a Android VM or we connect to my physicial phone (which have risk).

### Planner Response

Good question — let me clear up the confusion.

## JADX Has Nothing to Do with Android

**JADX is purely a desktop application** (Windows/Mac/Linux). It has no built-in Android, no emulator, no phone connection. It simply reads an APK **file** from your hard drive and decompiles it. That's it.

The Android device/emulator is only needed for **one thing**: getting the APK file in the first place.

```
┌─────────────────────┐         ┌──────────────────┐
│  Android             │  pull   │  Your Windows PC  │
│  (phone or emulator) │ ──────→ │                    │
│                      │  .apk   │  APK file on disk  │
│  Install app from    │  file   │       ↓             │
│  Play Store          │         │  Open in JADX      │
└─────────────────────┘         │  (fully offline)    │
                                └──────────────────────┘
     ONLY needed for             Everything else
     getting the APK             happens here
```

## Your Options to Get the APK

### Option 1: Your Physical Phone — ✅ Recommended, Low Risk

You're probably worried about security risks, so let me address them:

| Concern | Reality |
|---------|---------|
| "Enabling USB Debugging is dangerous" | It only allows ADB commands **while the phone is plugged into YOUR computer**. Disable it after you're done. |
| "ADB pull can damage my phone" | `adb pull` is **read-only** — it copies a file FROM the phone. It cannot modify or damage anything. |
| "The banking app might detect something" | You're not modifying the app or rooting the phone. The app has zero awareness of ADB. |

**What you'd do:**
```bash
# Enable Developer Options → USB Debugging
# Plug in phone → approve the USB debugging prompt
# Pull the APK
adb pull /data/app/.../base.apk ./bankapp.apk
# Unplug phone → disable USB Debugging
# Done. Now work entirely on your PC with JADX.
```

The phone interaction lasts **~2 minutes**. After that, you never touch it again.

### Option 2: Android Emulator — ⚠️ More Complex

You **can** use Android Studio's emulator, but there are complications:

| Issue | Detail |
|-------|--------|
| **Play Store access** | You must choose a system image labeled **"Google Play"** (not just "Google APIs") to have the Play Store |
| **Banking app detection** | Many banking apps **detect emulators and refuse to install or run** |
| **Heavy resource usage** | Android Studio + emulator needs ~8-16GB RAM |
| **Google account needed** | You need to sign into Play Store on the emulator |

If the banking app blocks emulators (which is likely), this option fails entirely.

### Option 3: Skip Android Entirely — 🎯 Try This First

Before setting up anything, **check if the APK is already available online**:

1. **Go to [APKMirror.com](https://www.apkmirror.com/)** → search for the app
2. **Go to [APKPure.com](https://apkpure.com/)** → search for the app

If it's there, just **download the APK file directly to your PC** and open it in JADX. No phone, no emulator, no ADB — nothing.

## My Recommendation

```
Step 1: Check APKMirror / APKPure first
        → If found: download APK → open in JADX → done ✅
        → If not found: go to Step 2

Step 2: Use your physical phone (2-minute process)
        → Install app from Play Store
        → Enable USB Debugging
        → adb pull the APK
        → Disable USB Debugging
        → Open APK in JADX → done ✅
```

**Your physical phone is perfectly safe for this.** You're just copying a file. It's no different from plugging in a USB drive and copying a document off it. No root, no modification, no risk.

Want to start by checking if the app is on APKMirror?

### User Input

Could you give me commands to install jdk, adb and jedx/jedx-gui for my ubuntu VM

### Planner Response

Here are clean, copy-paste ready commands for Ubuntu:

## 1. JDK (Java 17 — stable, widely compatible)

```bash
sudo apt update && sudo apt upgrade -y
sudo apt install openjdk-17-jdk -y

# Verify
java -version
```

## 2. ADB (Android Debug Bridge)

```bash
sudo apt install adb -y

# Verify
adb version
```

## 3. JADX-GUI

JADX is **not in the apt repo** — you need to download the release binary:

```bash
# Install unzip if not present
sudo apt install unzip wget -y

# Download latest JADX release (check https://github.com/skylot/jadx/releases for latest version)
wget https://github.com/skylot/jadx/releases/download/v1.5.1/jadx-1.5.1.zip

# Extract to /opt/jadx
sudo unzip jadx-1.5.1.zip -d /opt/jadx

# Make binaries executable
sudo chmod +x /opt/jadx/bin/jadx
sudo chmod +x /opt/jadx/bin/jadx-gui

# Create symlinks so you can run it from anywhere
sudo ln -s /opt/jadx/bin/jadx /usr/local/bin/jadx
sudo ln -s /opt/jadx/bin/jadx-gui /usr/local/bin/jadx-gui

# Verify
jadx --version
```

## 4. Launch JADX-GUI

```bash
jadx-gui
```

> [!NOTE]
> JADX-GUI is a **graphical app** — it requires a desktop environment (GNOME, XFCE, etc.). If your Ubuntu VM is headless (no GUI), use the CLI version instead:
> ```bash
> jadx -d ./output_folder ./bankapp.apk
> ```
> This decompiles the APK into the `output_folder` directory.

## 5. Verify Everything Works Together

```bash
# Quick check all tools
java -version       # Should show: openjdk 17.x.x
adb version         # Should show: Android Debug Bridge version x.x.x
jadx --version      # Should show: jadx-1.5.1
```

---

Once everything is installed, your full workflow will be:

```bash
# 1. Connect phone, pull APK
adb devices                          # Confirm phone is detected
adb shell pm list packages | grep <bankname>   # Find package name
adb pull /data/app/~<hash>/<package>/base.apk ./bankapp.apk

# 2. Open in JADX-GUI
jadx-gui bankapp.apk
```

Let me know if you hit any issues during setup!

### User Input

Do you have any legitmate source which have simple .apk file for testing? I did try to find in maddiestone's Android RE Workshop but the .apk files r inaccessible.

### Planner Response

Let me quickly check what's currently accessible.

*Read URL content from /OWASP/owasp-mastg/blob/master/README.md *

*Read URL content from /OWASP/MASTG-Hacking-Playground *

*Read URL content from /dineshshetty/Android-InsecureBankv2 *

*Read URL content from /OWASP/owasp-mastg/releases *

*Read URL content from /dineshshetty/Android-InsecureBankv2/blob/master/InsecureBankv2.apk *

*Read URL content from /OWASP/owasp-mastg/tree/master/Crackmes *

*Viewed [content.md](file:///C:/Users/PC/.gemini/antigravity/brain/50d0c2df-96d5-43f5-a511-1824e51e840c/.system_generated/steps/37/content.md) *

### Planner Response

Good — I confirmed the sources. Here are verified, directly downloadable APKs for practice:

## ✅ Verified Legitimate Practice APKs

### 1. OWASP MASTG "UnCrackable" Apps — 🎯 Best for Your Purpose

These are **purpose-built RE practice APKs** by OWASP, widely used in security training. Download directly with `wget`:

```bash
# UnCrackable Level 1 — Simple, great for first-timers
wget https://github.com/OWASP/mastg/raw/master/Crackmes/Android/Level_01/UnCrackable-Level1.apk

# UnCrackable Level 2 — Slightly more complex
wget https://github.com/OWASP/mastg/raw/master/Crackmes/Android/Level_02/UnCrackable-Level2.apk

# UnCrackable Level 3 — Includes native libs (.so) — useful for your ELF skills
wget https://github.com/OWASP/mastg/raw/master/Crackmes/Android/Level_03/UnCrackable-Level3.apk
```

These are small, clean APKs. **Level 1 is where you should start.**

---

### 2. InsecureBankv2 — Simulates a Banking App

A deliberately vulnerable **fake banking app** specifically designed for security analysis practice:

```bash
# Clone the repo and grab the APK
wget https://github.com/dineshshetty/Android-InsecureBankv2/raw/master/InsecureBankv2.apk
```

This one is particularly relevant for you since it mimics a banking app's structure, and has multiple third-party-like components to identify.

---

### 3. MASTG Hacking Playground (Feature-Rich)

```bash
wget https://github.com/OWASP/MASTG-Hacking-Playground/raw/master/Android/MSTG-Android-Java.apk
```

---

## Quickstart on Your Ubuntu VM

```bash
# Download Level 1 (smallest, best to start)
wget https://github.com/OWASP/mastg/raw/master/Crackmes/Android/Level_01/UnCrackable-Level1.apk

# Open in JADX-GUI
jadx-gui UnCrackable-Level1.apk
```

Once JADX opens it, practice:
1. **Browse the package tree** — find what packages/classes exist
2. **Use `Ctrl+Shift+F`** — search for strings like "secret", "check", "verify"
3. **Read the decompiled Java** — get comfortable with the output format

This will directly translate to the skills you need for your actual banking app assignment. Want me to walk you through what you'll see inside Level 1?

### User Input

Does by running this command: jadx-gui UnCrackable-Level1.apk, will it create additional directory to hold other files?

### Planner Response

**No.** Running `jadx-gui UnCrackable-Level1.apk` does **not** create any files or directories automatically.

## What actually happens

```
jadx-gui UnCrackable-Level1.apk
         ↓
Opens the GUI window
         ↓
Decompiles the APK entirely IN MEMORY
         ↓
Displays the source tree in the GUI
         ↓
You browse code interactively
```

Everything lives in RAM — nothing is written to disk unless **you explicitly tell it to**.

---

## If You Want to Save to Disk (Optional)

JADX-GUI has a **"Save as Gradle project"** option:

```
File → Save as Gradle project → Choose output folder
```

Or use the CLI version which **does** write to disk by design:

```bash
# This creates an output folder with all decompiled files
jadx -d ./UnCrackable-Level1-source/ UnCrackable-Level1.apk

# Output structure:
UnCrackable-Level1-source/
├── sources/          ← Decompiled .java files
│   └── sg/vantagepoint/uncrackable1/
│       ├── MainActivity.java
│       └── ...
└── resources/        ← AndroidManifest.xml, res/, assets/
```

---

## Summary

| Command | Creates files on disk? |
|---------|----------------------|
| `jadx-gui app.apk` | ❌ No — GUI only, in memory |
| `jadx -d ./output/ app.apk` | ✅ Yes — saves decompiled source |
| GUI → "Save as Gradle project" | ✅ Yes — you choose where |

For your workflow, **`jadx-gui` alone is perfectly fine** — you don't need the files saved to disk. You just browse, search, and screenshot what you need for the report.

### User Input

Inside UnCrackable-Level1.apk what file/folder should I be mindful of and do static analysis the most?

### Planner Response

Great question — let me map this to what you'll actually **see inside JADX-GUI** when you open the APK.

## What JADX Shows You

When you open the APK, JADX presents two main panels:

```
Left Panel (Tree)
├── 📁 Source Code          ← Java/Kotlin decompiled code
└── 📁 Resources            ← XML files, assets, manifest
```

---

## Priority Order for Static Analysis

### 🥇 #1 — `AndroidManifest.xml` (Always Start Here)

```
Resources → AndroidManifest.xml
```

This is the **app's blueprint**. Read it first, every time. It tells you:

| What to look for | Why |
|-----------------|-----|
| `package=` attribute | The app's root package name |
| `<activity>`, `<service>`, `<receiver>` | All entry points of the app |
| `<uses-permission>` | What the app can access (camera, storage, internet...) |
| `<meta-data>` | Often contains API keys or SDK config |
| Third-party components | Any vendor activity/service registered here |

For UnCrackable L1, you'll see it's a single-activity app — simple and clean.

---

### 🥈 #2 — Source Code Tree (The Core of Your Analysis)

```
Source Code
└── sg.vantagepoint          ← The app's own package
    ├── a/                   ← Utility/helper classes (often obfuscated)
    │   ├── a.java           ← Crypto helper
    │   └── b.java           ← Another helper
    └── uncrackable1/
        └── MainActivity.java  ← 🎯 START HERE — main app logic
```

**For UnCrackable L1 specifically:**
- `MainActivity.java` — contains the check logic, the secret string verification, everything important
- The `a/` subpackage — contains supporting crypto/util functions called by MainActivity

**For your real banking app assignment**, this is where you hunt for:
```
com.vendorname.*    ← Third-party SDK packages (your evidence)
com.bankapp.*       ← App's own code that CALLS the vendor SDK
```

---

### 🥉 #3 — `resources/res/values/strings.xml`

```
Resources → res → values → strings.xml
```

Often overlooked but contains:
- Hardcoded API keys
- Vendor SDK keys/tokens (e.g., `<string name="jumio_api_key">abc123</string>`)
- App configuration referencing third-party services

---

### 4 — `assets/` folder

```
Resources → assets/
```

Look for:
- `.json` config files (SDK initialization configs)
- `.html` files (vendor terms, embedded web content)
- ML model files (`.tflite`) — strong indicator of eKYC/face detection SDKs
- Certificates or keys

---

### 5 — Native Libraries (if present)

```
Resources → lib/
├── arm64-v8a/
│   └── *.so files
```

UnCrackable **Level 3** has `.so` files — that's where your ELF RE skills come in. Level 1 doesn't have native libs, keeping it simple.

---

## Visual Summary — What to Focus On

```
JADX Tree
│
├── 📄 AndroidManifest.xml     ← 1st thing to read ALWAYS
│
├── 📁 Source Code
│   └── sg.vantagepoint
│       └── uncrackable1
│           └── MainActivity  ← 2nd — core app logic
│
└── 📁 Resources
    ├── res/values/strings.xml ← 3rd — hardcoded values
    ├── assets/                ← 4th — config files
    └── lib/                   ← 5th — native .so (if any)
```

---

## Translating This to Your Banking App

When you eventually open the real banking app in JADX, you follow the exact same order — but at step 2 (Source Code), instead of reading logic, you're **scanning the package list** for vendor namespaces like `com.facetec`, `com.jumio`, etc. That package tree IS your primary evidence.

Want me to walk through what you'll actually find in MainActivity.java of UnCrackable L1?