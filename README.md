# 🎮 GAME SPACE — Ultimate Android Gaming Optimizer

**GAME SPACE** is a high-performance Flutter Android application designed for system-level gaming optimizations via `setprop` hardware tweaks, GPU/CPU scheduling, touch sampling rate enhancements, and network latency optimizations across **all Android chipsets and brands** (Infinix, Tecno, Samsung, Xiaomi, Realme, etc.).

---

## ⚠️ Important Distribution & Root Disclaimer

> [!CAUTION]
> **Sideload / Direct APK Only**: Because this application modifies Android system properties via root elevated access (`su`), it violates Google Play Store policies regarding system parameter modification. This app is designed exclusively for **sideloading (direct APK installation)** on rooted devices (Magisk / KernelSU / APatch).

> [!NOTE]
> **Non-Rooted Compatibility**: If installed on a non-rooted device, the app automatically operates in **Read-Only / Info Mode**, displaying specs and system property defaults without executing modification commands.

---

## 🛠️ Architecture Overview

Built using **Flutter Clean Architecture** + **BLoC/Cubit**:

- **Presentation Layer**: Flutter widgets with dark neon glassmorphic design system.
- **Domain Layer**: Pure Dart entities, interfaces, and use cases.
- **Data Layer**: Repositories bridging Dart `MethodChannel` to native Android Kotlin code.
- **Native Android Layer**:
  - `ShellExecutor.kt`: Sanitized command input execution, exit code inspection, and SELinux validation.
  - `DeviceDetector.kt`: Multi-chipset detection (MediaTek Helio/Dimensity, Unisoc Tiger, Qualcomm Snapdragon, Samsung Exynos, Google Tensor, HiSilicon Kirin) with `/proc/cpuinfo` fallback for legacy Android 7-10 units.
  - `BootReceiver.kt`: Listens to `BOOT_COMPLETED` to re-apply non-`persist.*` runtime tweaks after system reboots.

---

## 🌍 Supported Languages

- 🇬🇧 English (`en`)
- 🇫🇷 French (`fr`)
- 🇸🇦 Arabic (`ar`)
- 🇪🇸 Spanish (`es`)
- 🇮🇩 Indonesian (`id`)
- 🇰🇪 Swahili (`sw`)

---

## 🚀 Getting Started

### Build APK
```bash
flutter build apk --release
```

### Install via ADB
```bash
adb install build/app/outputs/flutter-apk/app-release.apk
```

---

## 👤 Developer Profiles

- **Facebook**: [Willy Jr Carnasa Gailo](https://www.facebook.com/https.willy.jr.carnasa.gailo2026.2027)
- **GitHub**: [willygailo](https://github.com/willygailo)

