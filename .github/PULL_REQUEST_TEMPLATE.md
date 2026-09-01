## 📝 Description

Please provide a summary of the changes introduced in this pull request and the motivation behind them.

---

## 🎯 Type of Change

- [ ] 🐛 **Bug Fix:** Non-breaking change that fixes an issue or crash
- [ ] ✨ **New Feature:** Non-breaking enhancement or new capability
- [ ] 🎮 **New Game Preset:** Added config support / FPS preset for a mobile game
- [ ] ⚡ **Performance Optimization:** Improvement in CPU/GPU scheduling, memory, or refresh rate logic
- [ ] 🛡️ **Security / Ban-Safety:** Hardening against anti-cheat conflicts or privilege isolation
- [ ] 📚 **Documentation:** Updates to README, guides, or code comments

---

## 🛡️ Ban-Safety & Dual-Engine Compliance

- [ ] **100% Ban-Safe:** This PR does NOT introduce binary tampering, memory injection, or unauthorized game executable modifications.
- [ ] **Dual-Engine Fallback:** Changes that interact with the Android OS gracefully fall back to legal Android SDK APIs when Shizuku is not running.

---

## 🧪 Testing & Verification

Describe how these changes were tested:
- [ ] **Automated Unit Tests:** Ran `./gradlew testDebugUnitTest` (all tests passed with 0 failures)
- [ ] **JaCoCo Code Coverage:** Ran `./gradlew jacocoTestReport`
- [ ] **Lint Checks:** Ran `./gradlew lint`
- [ ] **Physical Device Testing:** Tested on physical device (specify device model and Android version below)

**Device Test Environment:**
- Device: `e.g., POCO F6 / Android 14 / Snapdragon 8s Gen 3`
- Shizuku Mode: `e.g., Wireless Debugging Active`
- Target Game(s) Tested: `e.g., PUBG Mobile / MLBB`

---

## ✅ Pull Request Checklist

- [ ] My code adheres to the project's coding standards.
- [ ] I have added/updated unit tests to cover new logic or bug fixes.
- [ ] I have tested my changes and verified that no existing features are broken.
- [ ] I have not committed any private keys, keystores, or `.env` files.
- [ ] My branch is rebased on the latest `main` branch.
