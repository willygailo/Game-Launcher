# 🎮 GAME SPACE — Pure Native Android Gaming Optimizer & FPS Unlocker (Java Only)

**GAME SPACE** is a **100% Pure Native Android Application (built in Java JDK 17)** engineered for system-level gaming optimizations, display refresh rate locking (90Hz / 120Hz / 144Hz), graphics unlocking (4x MSAA & Vulkan HWUI), CPU/GPU scheduling, touch sampling rate enhancements, and thermal throttling bypass across **all Android chipsets and OEM brands** (Xiaomi, Infinix, Tecno, Samsung, Realme, OnePlus, etc.).

---

## ⚡ Execution Engines (Root & Non-Rooted Shizuku ADB)

GAME SPACE features a **3-Tiered Execution Engine** designed to deliver maximum performance on both rooted and non-rooted Android devices:

| Execution Engine | Requirements | Capabilities |
| :--- | :--- | :--- |
| **👑 Root Mode (`su`)** | Magisk / KernelSU / APatch elevated access | Full system property tuning (`setprop`), `sysfs` kernel CPU/GPU governors, touch sampling rate, TCP network buffer size + ADB settings commands. |
| **⚡ Shizuku ADB Mode (`shizuku`)** | Shizuku app active via Wireless ADB or USB ADB | **Full Non-Rooted Support**: Lock Max Refresh Rate (90Hz/120Hz/144Hz), Force 4x MSAA graphics, Vulkan GPU rendering backend, Android 12+ Game Mode API performance overrides, and Thermal Throttling bypass (`cmd thermal`). |
| **ℹ️ Read-Only / Info Mode** | Standard non-rooted device without Shizuku | Displays live system metrics, hardware specs, thermal status, battery percentage, and interactive setup instructions. |

---

## ☕ Pure Native Java Code Architecture (`android/app/src/main/java/com/gamespace/app/`)

```
app/src/main/
├── AndroidManifest.xml                                # ShizukuProvider & permissions
└── java/com/gamespace/app/
    ├── MainActivity.java                              # Main Pure Java Android AppCompatActivity
    ├── BootReceiver.java                              # Pure Java BOOT_COMPLETED broadcast receiver
    ├── receivers/
    │   └── BootReceiver.java                          # Auto-applies tweaks on boot
    └── utils/
        ├── DeviceDetector.java                        # Pure Java Snapdragon, MediaTek, Exynos, Tensor detector
        ├── ShellExecutor.java                         # Pure Java ProcessBuilder su shell executor
        └── ShizukuExecutor.java                       # Pure Java Shizuku Binder API executor & auto-perm granter
```

---

## 📱 1-Tap Shizuku Direct Connection (Non-Rooted Devices)

No manual ADB commands required! GAME SPACE connects directly to the Shizuku app API:
1. Open the Shizuku app on your device.
2. Launch **GAME SPACE**, go to **Permissions**, and tap **REQUEST SHIZUKU PERMISSION**.
3. Done! Shizuku connects directly to GAME SPACE to run 90Hz/120Hz/144Hz refresh rate locking, MSAA graphics, and thermal overrides automatically.

---

## 🚀 Building the Pure Java Release APK

Navigate to the `android/` directory and compile with Gradle:

```bash
cd android
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
./gradlew assembleRelease
```

### 📦 Output APK Location:
Your compiled release APK will be saved at:
`android/app/build/outputs/apk/release/app-release.apk`
