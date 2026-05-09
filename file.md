TLS Handshake:

https://www.youtube.com/watch?v=ZkL10eoG1PY&list=LL&index=26&t=41s

OWASP Mobile Application Security:

https://mas.owasp.org/MASTG/

JADX: 

https://github.com/skylot/jadx/wiki

Android RE:

https://github.com/maddiestone/AndroidAppRE

___
1. OWASP MASTG "UnCrackable" Apps
```bash
# UnCrackable Level 1 — Simple, great for first-timers
wget https://github.com/OWASP/mastg/raw/master/Crackmes/Android/Level_01/UnCrackable-Level1.apk
# UnCrackable Level 2 — Slightly more complex
wget https://github.com/OWASP/mastg/raw/master/Crackmes/Android/Level_02/UnCrackable-Level2.apk
# UnCrackable Level 3 — Includes native libs (.so) — useful for your ELF skills
wget https://github.com/OWASP/mastg/raw/master/Crackmes/Android/Level_03/UnCrackable-Level3.apk
```

2. InsecureBankv2 — Simulates a Banking App
```bash
# Clone the repo and grab the APK
wget https://github.com/dineshshetty/Android-InsecureBankv2/raw/master/InsecureBankv2.apk
```

Beginner:
```bash
# Download Level 1 (smallest, best to start)
wget https://github.com/OWASP/mastg/raw/master/Crackmes/Android/Level_01/UnCrackable-Level1.apk
# Open in JADX-GUI
jadx-gui UnCrackable-Level1.apk
```
