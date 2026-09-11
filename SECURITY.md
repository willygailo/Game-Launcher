# Security Policy & Ban-Safety Guarantee

The **Game Launcher PRO** team takes application security, privacy, and player safety seriously. This document outlines our ban-safety principles, supported versions, and how to report vulnerabilities or safety concerns.

---

## 🛡️ 100% Ban-Safety Guarantee & Compliance Policy

Game Launcher PRO is strictly designed to operate within legal Android framework parameters and elevated OS-level user permissions.

### What Game Launcher PRO Does:
* ✅ **Legal Framework APIs:** Uses Android `GameManager`, `DisplayManager`, `PowerManager`, ADPF (`PerformanceHintManager`), and `WifiManager` to optimize hardware resources.
* ✅ **Elevated Android Shell (Shizuku / ADB):** Interfaces with official Android system services (`SurfaceFlinger`, `cmd game`, `cmd power`, `setprop`, `sysctl`) to configure display refresh rates and kernel governors.
* ✅ **User-Accessible Configuration Edits:** Modifies standard text configuration files (`UserCustom.ini`, `playerprefs.xml`, `Active.sav`) located in application-accessible directories (`/sdcard/Android/data/` or via SAF).

### What Game Launcher PRO NEVER Does:
* ❌ **No In-Memory Modification:** Never injects code into running game processes, hooks memory addresses, or attaches debuggers (`ptrace`).
* ❌ **No Binary Patching:** Never modifies game executables, APK signatures, or native shared libraries (`.so` files).
* ❌ **No Network Packet Tampering:** Does not intercept, decrypt, or alter in-game network packets or multiplayer server communication.

---

## 🔒 Supported Versions

| Version | Supported | Status |
| :--- | :---: | :--- |
| **v16.x (Current / Latest)** | ✅ Yes | Actively maintained & supported with security updates |
| **v15.x and below** | ❌ No | Deprecated — please upgrade to the latest release |

---

## 🚨 Reporting a Vulnerability or Ban-Risk Concern

If you discover a security vulnerability, an unintended privilege escalation bug, or a potential anti-cheat flag concern in any supported game:

1. **Do NOT open a public GitHub issue** describing the exploit or vulnerability.
2. **Submit a Private Report:**
   - Use [GitHub Security Advisories](https://github.com/willygailo/Game-Launcher/security/advisories/new) to report the vulnerability privately.
   - Or contact the project maintainer via Facebook: [Willy Jr Carnasa Gailo](https://web.facebook.com/https.willy.jr.carnasa.gailo2026.2027) or GitHub [@willygailo](https://github.com/willygailo).
3. **Include Details:**
   - Description of the vulnerability or concern.
   - Steps to reproduce the issue.
   - Affected device model, Android OS version, and game title/package ID.
   - Logs or screenshots if applicable.

---

## ⏱️ Response & Disclosure Process

* **Acknowledgment:** We aim to acknowledge reports within **48 hours**.
* **Assessment & Fix:** If confirmed, a fix will be developed in a private branch and verified across our automated test suites.
* **Public Release:** A patched release will be published alongside public credit to the reporter (unless requested to remain anonymous).

Thank you for helping keep Game Launcher PRO secure, performant, and safe for all players!
