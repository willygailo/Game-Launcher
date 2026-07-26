# 🧠 Skills & Architecture Reference — GAME SPACE

This document maps out the core architecture and skill sets required to maintain and expand **GAME SPACE**.

---

## 🏛️ Project Architecture Map

```
Game_Launcher_Pro/
├── README.md                                          # Master documentation & features
├── agentSkills.md                                     # Integrated AI agent skills
├── GEMINI_BUILD_INSTRUCTIONS.md                       # Build & deployment guide
├── skills.md                                          # Skill map & architecture guide
└── android/                                           # Pure Native Java Android Project
    ├── build.gradle                                   # Root buildscript (AGP 8.7.3)
    ├── settings.gradle                                # Settings file (Zero Flutter dependencies)
    └── app/
        ├── build.gradle                               # App buildscript (JDK 17 + Shizuku API)
        └── src/main/
            ├── AndroidManifest.xml                    # Android Manifest & ShizukuProvider
            ├── res/                                   # XML Layouts & Material Design 3 UI
            └── java/com/gamespace/app/                # Pure Java Source Code
                ├── MainActivity.java                  # Fragment Manager AppCompatActivity
                ├── ui/                                # UI Fragments (Home, HzFps, Profiles, Permissions)
                ├── receivers/                         # BootReceiver.java
                ├── utils/                             # ShizukuExecutor.java, ShellExecutor.java, DeviceDetector.java
                └── channels/                          # Platform channels
```

---

## ⚡ Core Engine Capability Matrix

| Feature | Executor Class | Mechanism |
| :--- | :--- | :--- |
| **Refresh Rate Locking** | `ShizukuExecutor.java` / `ShellExecutor.java` | `settings put system peak_refresh_rate <hz>` |
| **Thermal Bypass** | `ShizukuExecutor.java` / `ShellExecutor.java` | `cmd thermal override-status 0` |
| **2D Games Tuning** | `ShizukuExecutor.java` / `ShellExecutor.java` | `setprop windowsmgr.max_events_per_sec 300` & `setprop persist.sys.scrollingcache 3` |
| **Graphics Unlocking** | `ShizukuExecutor.java` / `ShellExecutor.java` | `setprop debug.egl.force_msaa 1` & `setprop debug.hwui.renderer vulkan` |
| **1-Tap Permission Grant** | `ShizukuExecutor.java` | `pm grant <package> android.permission.WRITE_SECURE_SETTINGS` |
