<h1 align="center">🎮 Game Launcher & Performance Booster</h1>

<p align="center">
  <img src="https://img.shields.io/badge/Android-10--15%20(API%2029--35)-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android API"/>
  <img src="https://img.shields.io/badge/Kotlin-1.9+-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin"/>
  <img src="https://img.shields.io/badge/Jetpack%20Compose-latest-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose"/>
  <img src="https://img.shields.io/badge/Hilt-DI-FF4088?style=for-the-badge&logo=dagger&logoColor=white" alt="Hilt"/>
</p>

<p align="center">
  <strong>🚀 Ang pinaka-powerful na Android performance booster para sa mobile gaming!</strong>
</p>

---

<h2 align="center">👤 Developer</h2>

<p align="center">
  <img src="https://github.com/Willy-kali.png" width="120" style="border-radius:50%"/><br/><br/>
  <strong>Willy Gailo</strong><br/>
  🔗 <a href="https://github.com/Willy-kali">GitHub</a> • 
  <a href="https://www.facebook.com/https.willy.jr.carnasa.gailo2026.2027">Facebook</a>
</p>

---

<h2 align="center">✨ Features</h2>

<div align="center">

| 🎯 Feature | 📝 Description | 🔥 Status |
|------------|----------------|-----------|
| **📊 Real-time Monitoring** | CPU, RAM, GPU, at battery stats | ✅ Available |
| **🎯 Floating FPS Counter** | Live FPS overlay habang naglalaro | ✅ Available |
| **🎮 Game Library** | Auto-detect ng lahat ng installed games | ✅ Available |
| **⚡ Per-Game Boosts** | Custom FPS (30-165), WiFi lock per game | ✅ Available |
| **📺 Immersive Controls** | DND mode, max brightness automation | ✅ Available |
| **🏠 Home Widget** | Quick shortcut sa home screen | ✅ Available |
| **⚙️ Quick Settings Tile** | Toggle booster sa notification panel | ✅ Available |
| **🤖 Game Detector** | Auto-boost pag nag-open ng game | ✅ Available |
| **📈 Stats History** | Track ng play time per session | ✅ Available |
| **🧹 RAM Optimizer** | Kill background apps instantly | ✅ Available |
| **📦 Storage Optimizer** | Run fstrim para sa speed boost | ✅ Available |
| **🌐 Network Manager** | Monitor ping at connection status | ✅ Available |

</div>

---

<h2 align="center">🛠️ Tech Stack</h2>

<div align="center">

| Category | Technology |
|----------|-------------|
| **UI Framework** | ☝️ Jetpack Compose + Material Design 3 |
| **Dependency Injection** | 💉 Hilt (Dagger) |
| **Local Storage** | 🗄️ Room Database + DataStore Preferences |
| **Architecture** | 🏗️ MVVM + Repository Pattern |
| **Build System** | ⚙️ Gradle Kotlin DSL |
| **Language** | 🟣 Kotlin 1.9+ |

</div>

---

<h2 align="center">🚀 Quick Start</h2>

<div align="center">

### 📋 Requirements

☝️ Android Studio Hedgehog (2023.1.1) or newer  
☕ JDK 17  
🖥️ Android SDK 34 (Android 14)  
📱 Test device: Android 10-15 (API 29-35)

### 🔧 Build Instructions

```bash
# Clone the repository
git clone https://github.com/Willy-kali/Game-Launcher-Performance-Booster.git
cd "Game launcher"

# Build debug APK
./gradlew assembleDebug

# Output location
# app/build/outputs/apk/debug/app-debug.apk
```

### 📱 Install sa Device

```bash
# Enable USB Debugging sa device
# Settings > About Phone > Tap "Build Number" 7 times
# Settings > System > Developer Options > Enable USB Debugging

adb install app/build/outputs/apk/debug/app-debug.apk
```

</div>

---

<h2 align="center">🔐 Permissions Needed</h2>

<div align="center">

| 🔑 Permission | 📝 Purpose |
|---------------|-------------|
| `SYSTEM_ALERT_WINDOW` | FPS overlay display sa top ng games |
| `FOREGROUND_SERVICE` | Background boosting services |
| `WRITE_SETTINGS` | Automatic brightness control |
| `KILL_BACKGROUND_PROCESSES` | RAM optimization |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Keep service running |
| `ACCESS_WIFI_STATE` | WiFi lock feature |

</div>

---

<h2 align="center">📱 Supported Android Versions</h2>

<p align="center">
  <img src="https://img.shields.io/badge/Android%2010-API%2029-3DDC84?style=flat" alt="API 29"/>
  <img src="https://img.shields.io/badge/Android%2011-API%2030-3DDC84?style=flat" alt="API 30"/>
  <img src="https://img.shields.io/badge/Android%2012-API%2031-3DDC84?style=flat" alt="API 31"/>
  <img src="https://img.shields.io/badge/Android%2013-API%2033-3DDC84?style=flat" alt="API 33"/>
  <img src="https://img.shields.io/badge/Android%2014-API%2034-3DDC84?style=flat" alt="API 34"/>
  <img src="https://img.shields.io/badge/Android%2015-API%2035-3DDC84?style=flat" alt="API 35"/>
</p>

---

<h2 align="center">🔧 Troubleshooting</h2>

<div align="center">

<details>
<summary><strong>🖥️ Max Brightness Not Working?</strong></summary>

1. Buksan ang **Settings** tab sa app
2. I-tap ang **Grant System Settings**
3. I-enable ang **Modify system settings** para sa Game Launcher

</details>

<details>
<summary><strong>🎯 FPS Overlay Not Showing?</strong></summary>

1. Go to **Settings → Apps → Game Launcher → Permissions**
2. I-enable ang **Display over other apps**
3. Restart ang app

</details>

<details>
<summary><strong>🔇 DND Not Working?</strong></summary>

1. Go to **Settings → Apps → Game Launcher → Notifications**
2. I-enable ang **Do Not Disturb access**
3. Grant permission sa popup dialog

</details>

<details>
<summary><strong>⚡ Booster Not Starting Automatically?</strong></summary>

1. Go to **Settings → Apps → Game Launcher**
2. Battery → **Unrestricted** (para di mapatay ng system)
3. Enable **Auto-Start** kung available sa device

</details>

</div>

---

<h2 align="center">📂 Project Structure</h2>

<div align="center">

```
app/src/main/java/com/gamelauncher/
├── 🎮 core/              # Managers (Device, FPS, Performance, etc.)
├── 📦 data/              # Database, Models, Repository
│   ├── local/            # Room Database & DAOs
│   ├── model/            # Data classes
│   ├── preference/       # DataStore settings
│   └── repository/      # Data layer
├── 💉 di/                # Hilt Dependency Injection
├── 📡 receivers/         # Broadcast Receivers (Boot)
├── 🛎️ services/          # Background Services
│   ├── GameBoosterService
│   ├── GameDetectorService
│   ├── OverlayService
│   └── GameBoosterTileService
├── 🎨 ui/                # UI Components (Compose)
│   ├── dashboard/        # Dashboard Screen + ViewModel
│   ├── games/            # Games Library Screen
│   ├── settings/         # Settings Screen
│   └── theme/           # App Theme & Colors
└── 🏠 widgets/           # Home Screen Widgets
```

</div>

---

<h2 align="center">🤝 Contributing</h2>

<p align="center">
Contributions are welcome! 🎉

1. 🍴 Fork the Project
2. 🌿 Create your Feature Branch (<code>git checkout -b feature/AmazingFeature</code>)
3. 💾 Commit your Changes (<code>git commit -m 'Add some AmazingFeature'</code>)
4. 📤 Push to the Branch (<code>git push origin feature/AmazingFeature</code>)
5. 🔁 Open a Pull Request
</p>

---

<h2 align="center">📜 License</h2>

<p align="center">
This project is licensed under the MIT License - see the <a href="LICENSE">LICENSE</a> file for details.
</p>

---

<h2 align="center">🌟 Showcase</h2>

<p align="center">
  <em>🚀 Built with ❤️ for Filipino mobile gamers!</em><br/>
  <em>📍 Philippines 🇵🇭</em>
</p>

---

<div align="center">

### 🎮 Game Launcher & Performance Booster

<sub>Optimizing gaming performance one device at a time ⚡</sub>

[![GitHub stars](https://img.shields.io/github/stars/Willy-kali/Game-Launcher-Performance-Booster?style=social)](https://github.com/Willy-kali/Game-Launcher-Performance-Booster/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/Willy-kali/Game-Launcher-Performance-Booster?style=social)](https://github.com/Willy-kali/Game-Launcher-Performance-Booster/network/members)

</div>
