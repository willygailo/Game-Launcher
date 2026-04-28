# 🎮 Game Launcher & Performance Booster

**A powerful Android performance booster for mobile gaming**

---

### 👤 Developer

**Willy Gailo**  
[GitHub](https://github.com/Willy-kali) | [Facebook](https://www.facebook.com/https.willy.jr.carnasa.gailo2026.2027)

---

## ✨ Features

| Feature | Description |
|---------|-------------|
| **Real-time Monitoring** | CPU, RAM, GPU, battery stats |
| **Floating FPS Counter** | Live FPS overlay on games |
| **Game Library** | Auto-detects installed games |
| **Per-Game Boosts** | Custom FPS (30-165), WiFi lock |
| **Immersive Controls** | DND, max brightness |
| **Home Widget** | Home screen shortcut |
| **Quick Settings** | Toggle from notification panel |
| **Game Detector** | Auto-boost when game opens |
| **Stats History** | Session play time tracking |

---

## 🛠️ Tech Stack

- Jetpack Compose + Material 3
- Hilt for Dependency Injection
- Room + DataStore for storage

---

## 🚀 Build

```bash
cd "Game launcher"
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 ./gradlew assembleDebug
```

**Output:** `app/build/outputs/apk/debug/app-debug.apk`

---

## 🔐 Required Permissions

- `SYSTEM_ALERT_WINDOW` - FPS overlay display
- `FOREGROUND_SERVICE` - Background boosting
- `WRITE_SETTINGS` - Brightness control
- `KILL_BACKGROUND_PROCESSES` - RAM optimization

---

## 📱 Android Support

**Android 10 (API 29)** to **Android 15 (API 35)**

---

## 🔧 Troubleshooting

**Max Brightness Not Working:**
1. Go to **Settings** tab
2. Tap **Grant System Settings**
3. Enable **Modify system settings**

**FPS Overlay Not Showing:**
1. Go to Settings → Apps → Game Launcher → Permissions
2. Enable **Display over other apps**

**DND Not Working:**
1. Go to Settings → Apps → Game Launcher → Notifications
2. Enable **Do Not Disturb access**

---

*Built with ❤️ by Willy Gailo*