<div align="center">

<a href="https://github.com/willygailo/Game-Launcher">
  <img src="docs/assets/hero_banner.gif" alt="Game Launcher PRO Hero Banner" width="100%" style="border-radius: 12px; box-shadow: 0 10px 30px rgba(0, 240, 255, 0.2);" />
</a>

<br/>
<br/>

# ⚡ GAME LAUNCHER PRO ⚡
### 🚀 *Ultimate Android High-Refresh Gaming & Hardware Suite (2026 Edition)*

<br/>

[![Latest Release](https://img.shields.io/badge/Release-v17.7.0--PRO-00F0FF?style=for-the-badge&logo=github&logoColor=black&labelColor=0D1117)](https://github.com/willygailo/Game-Launcher/releases/latest)
[![Android Support](https://img.shields.io/badge/Android-13--16%20(API%2033--36)-3DDC84?style=for-the-badge&logo=android&logoColor=white&labelColor=0D1117)](#-quick-start)
[![Display Lock](https://img.shields.io/badge/Display-90Hz%20to%20185Hz-FF0055?style=for-the-badge&logo=speedtest&logoColor=white&labelColor=0D1117)](#-features)
[![Wi-Fi 7 MLO](https://img.shields.io/badge/Wi--Fi%207-802.11be%20MLO%20320MHz-7928CA?style=for-the-badge&logo=wi-fi&logoColor=white&labelColor=0D1117)](#-wi-fi-5g6g7g-turbo--gaming-dns)
[![PH SIM Supercharger](https://img.shields.io/badge/PH%20Telco-TNT%20%7C%20TM%205G%20Turbo-FF9900?style=for-the-badge&logo=signal&logoColor=white&labelColor=0D1117)](#-philippine-cellular-data-supercharger-tnt--tm)
[![License](https://img.shields.io/badge/License-MIT-00E5FF?style=for-the-badge&labelColor=0D1117)](LICENSE)
[![Ban Safety](https://img.shields.io/badge/Ban--Safety-100%25%20Verified-00FF66?style=for-the-badge&logo=shield&labelColor=0D1117)](SECURITY.md)

<br/>

[📥 **Download Latest APK (v17.7.0)**](https://github.com/willygailo/Game-Launcher/releases/latest) • [✨ **Features**](#-features) • [🇵🇭 **PH Telco Boost**](#-philippine-cellular-data-supercharger-tnt--tm) • [📶 **Wi-Fi 7 & DNS**](#-wi-fi-5g6g7g-turbo--gaming-dns) • [🎮 **Supported Games**](#-supported-games) • [🚀 **Quick Start**](#-quick-start) • [🤝 **Contributing**](CONTRIBUTING.md) • [🛡️ **Security**](SECURITY.md)

<br/>

---

</div>

## 🌟 Overview

**Game Launcher PRO** is a lightweight, high-performance game optimization suite engineered for competitive mobile esports on Android 13 through Android 16. Powered by modern C++17 native engines and non-root Shizuku integrations, it unlocks extreme display refresh rates (**90Hz – 185Hz**), eliminates thermal frame throttling, injects custom game engine configs, optimizes Philippine mobile data (**TNT / Smart** & **TM / Globe**), and unleashes **Wi-Fi 7 (802.11be MLO)** with ultra-low-latency Gaming DNS.

---

## ✨ 2026 Core Features

* 🔥 **Extreme Refresh Rate Unlocker**: Uncaps 90Hz, 120Hz, 144Hz, 165Hz, and 185Hz display modes with zero 60Hz drop or touch throttling.
* ⚡ **Native C++ Thread Affinity & Scheduling**: Directly pins critical game render loops and audio threads to performance/prime CPU cores (`sched_setaffinity`).
* 🇵🇭 **Philippine Cellular Data Supercharger**: Dedicated hardware profiles for **TNT / Smart** and **TM / Globe** with custom MTU, APN gateways, baseband keepalive, and anti-stall recovery.
* 📶 **Wi-Fi 5G, 6G & Wi-Fi 7 MLO Turbo**: Leverages 802.11be Multi-Link Operation (MLO), 320 MHz buffer tuning, preamble puncturing, and target wake time (TWT) sleep suppression.
* 🛡️ **4-Way Gaming DNS-over-TLS Suite**: One-tap zero-loss DoT resolvers including **Cloudflare 1.1.1.1**, **Google 8.8.8.8**, **AdGuard Gaming** (anti-tracker/anti-ad), and **Quad9 Ultra-Fast**.
* 🎮 **Native Game Engine & Config Injectors**:
  * **Mobile Legends (MLBB)**: Fast battle data sync, resource caching, skill timing optimization, and graphics unlock.
  * **PUBG Mobile / BGMI**: 185 FPS configuration injection, Vulkan render pathing, and memory-mapped anti-throttle patchers.
  * **Call of Duty: Mobile & Warzone**: Shader pre-caching, touch sample rate uncap, and high-refresh profile injection.
* 🎭 **2026 Flagship Device Spoofing**: Complete hardware identity spoofing for ROG Phone 9 Pro, RedMagic 10 Pro+, Galaxy S26 Ultra, Snapdragon 8 Elite, Dimensity 9400, and Apple A18 Pro.
* 🎯 **1000Hz Ultra Touch Polling**: Cuts input latency down to the millisecond with instant touch-channel response.
* 📊 **Live FPS & 1% Low HUD**: Real-time draggable overlay monitoring frame rates, frame jitter, RAM, battery temperature, and active network latency.
* 🛡️ **100% Non-Root Shizuku Engine**: Seamless auto-grant system permissions with automatic configuration restoration upon game exit.

---

## 🇵🇭 Philippine Cellular Data Supercharger (TNT & TM)

Tired of ping spikes and data stalls in the middle of crucial ranked matches? Game Launcher PRO includes custom telco baseband tuning for the Philippines:

| Carrier Profile | Target APN | MTU Size | Priority Bands | DNS Resolver | Special Tuning |
| :--- | :---: | :---: | :---: | :---: | :--- |
| **TNT / Smart 5G Supercharger** | `smartdata` | `1460` | B1, B3, B28, B41, n78 | Cloudflare `1.1.1.1` DoT | 5G Network Slicing, TCP BBR Congestion, Baseband Anti-Power-Save |
| **TM / Globe 5G Supercharger** | `real.globe.com.ph` | `1440` | B3, B7, B28, B40, n78 | Google `8.8.8.8` DoT | Fast Cell Handoff, Data Stall Recovery, Radio Multimode Keepalive |
| **DITO Telecommunity** | `dito.ph` | `1460` | B1, B28, B41, n78 | Cloudflare `1.1.1.1` DoT | Low-Jitter 5G SA Routing & High-Speed Window Allocation |

*Each profile automatically activates TCP receive window expansion (`tcp_default_init_rwnd 60`), suppresses radio power-collapse, and configures carrier-specific DoT to eliminate local ISP routing hops.*

---

## 📶 Wi-Fi 5G/6G/7G Turbo & Gaming DNS

* **Wi-Fi 7 Multi-Link Operation (MLO)**: Transmits gaming packets concurrently across 5 GHz and 6 GHz / 7 GHz channels (`persist.vendor.wifi.mlo=1`) to eliminate channel congestion.
* **320 MHz Bandwidth Buffers**: Enlarges socket buffer pipelines to handle high-density Wi-Fi 7 throughput without packet queuing.
* **Zero-Sleep Gaming Lock**: Disables 802.11ax/be Target Wake Time sleep intervals during gaming (`persist.vendor.wifi.twt=0`), eliminating packet jitter.
* **Gaming DNS Suite**:
  * ⚡ **Cloudflare 1.1.1.1**: The world's fastest gaming resolver via `one.one.one.one`.
  * 🌐 **Google 8.8.8.8**: Reliable, low-hop global anycast routing via `dns.google`.
  * 🛡️ **AdGuard Gaming**: Blocks background telemetry, trackers, and ads stealing bandwidth via `dns.adguard-dns.com`.
  * ⚡ **Quad9 Ultra-Fast**: Security-hardened low-latency DNS routing via `dns.quad9.net`.

---

## 🎮 Supported Games

| Game | Category | Target FPS | Optimization Engine |
| :--- | :---: | :---: | :--- |
| **PUBG Mobile / BGMI** | Battle Royale | 🔥 **185 FPS** | C++ Vulkan pipeline injector, 185 FPS UserCustom patcher, anti-throttle |
| **Mobile Legends: Bang Bang** | MOBA | 🔥 **185 FPS** | Fast battle data sync, 120/185Hz display lock, skill timing low-jitter |
| **Call of Duty: Mobile / Warzone** | FPS | 🔥 **185 FPS** | Shader pre-caching, touch uncap, graphic config injector |
| **Free Fire & Free Fire MAX** | Battle Royale | 🔥 **185 FPS** | High-refresh frame pacing, 1000Hz touch sensitivity curve |
| **Genshin Impact & Wuthering Waves** | Action RPG | ⚡ **120 FPS** | Unity / Unreal Engine CPU core pinning & thermal governor lock |
| **Roblox & CarX Street** | Sandbox / Racing | 🔥 **185 FPS** | OpenGL / Vulkan multi-threaded scheduler & dynamic resolution lock |

---

## 🚀 Quick Start

### Prerequisites
* Android device running **Android 13, 14, 15, or 16** (API 33 – 36).
* [**Shizuku**](https://shizuku.rikka.app/) installed and started via **Wireless Debugging** (Non-Root) or Root.

### Installation & Launch
1. **Download APK**: Download the latest release from the [Releases Page](https://github.com/willygailo/Game-Launcher/releases/latest):
   - **`Game_Space.apk`** (Release, ProGuard-optimized, ~46 MB)
   - **`Game_Space_Debug.apk`** (Debug version with extended diagnostics logging)
2. **Launch Shizuku**: Ensure Shizuku is running and authorize Game Launcher PRO when prompted.
3. **Configure Network & Game Profile**:
   - Open **Settings** ➔ **Network Settings** to toggle your **TNT / Smart** or **TM / Globe** supercharger, **Wi-Fi 7 MLO**, and your preferred **Gaming DNS**.
   - Select your target game and desired refresh rate (up to 185Hz).
4. **Boost & Play**: Tap **BOOST & LAUNCH** to apply native optimizations and launch your game!

---

## 🛠️ Building From Source

```bash
# Clone the repository
git clone https://github.com/willygailo/Game-Launcher.git
cd Game-Launcher/android

# Run unit test suite
./gradlew testDebugUnitTest

# Build Release APK
./gradlew assembleRelease

# Output APK located at:
# app/build/outputs/apk/release/Game_Space.apk
```

---

## 🤝 Contributing & Community

We welcome contributions from game enthusiasts and developers!
- Read our [**Contributing Guide**](CONTRIBUTING.md) to set up your environment.
- Review our [**Security & Anti-Ban Policy**](SECURITY.md).
- Submit feature requests or report bugs on our [**Issue Tracker**](https://github.com/willygailo/Game-Launcher/issues).

---

## 👨‍💻 Developer & Author

<div align="center">

[![GitHub Developer](https://img.shields.io/badge/GitHub-willygailo-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/willygailo)
&nbsp;&nbsp;&nbsp;&nbsp;
[![Facebook Profile](https://img.shields.io/badge/Facebook-Willy%20Jr%20Carnasa%20Gailo-1877F2?style=for-the-badge&logo=facebook&logoColor=white)](https://web.facebook.com/https.willy.jr.carnasa.gailo2026.2027)

<br/>

⭐ **If Game Launcher PRO helped you rank up, don't forget to star the repository!** ⭐

</div>

---

## 📄 License

This project is open-source and licensed under the [MIT License](LICENSE).
