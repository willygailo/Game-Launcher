# NewPatch Extraction & Dump Verification Report

**Generated Date**: 2026-10-09  
**Source Directory**: `android/NewPatch`

---

## 1. Mobile Legends: Bang Bang (v2.2.16.12322 / Season 42+)
- **APKS Package**: `Mobile Legends_ Bang Bang_2.2.16.12322.apks` (157.8 MB)
- **Extracted Directory**: `android/NewPatch/dump_mlbb/`
- **Engine**: Unity 2019.4.33 (SOSplit=1, EngineBuild: 44588b7c...)
- **Version Manifest**:
  - `version.xml` -> `2.2.16.1232.1`
  - `realversion.xml` -> `2.2.16.1232.1` (client_code: 251609, plugin: 36332)
  - `mola_config.xml` -> `<target_res_version>1232.1</target_res_version>`
- **Integrity & Checksum Bypass**:
  - `BinaryPatchMD5.xml` (6,253 bytes) verified against Season 42+ asset checksums
  - `ResCheckConf.xml` (135,565 bytes) matches drone & camera security whitelist
- **Native Binary**:
  - `libil2cpp.so` (384,824 bytes, MD5: eb71177ed155806e3934b0d080baed15) - Moonton dynamic loader stub

---

## 2. Call of Duty: Mobile (v1.6.57.0)
- **APKS Package**: `Call of Duty_1.6.57.apks` (2.02 GB)
- **Extracted Directory**: `android/NewPatch/dump_codm/`
- **Engine**: Unity 2020 Monolithic IL2CPP Runtime
- **Metadata**:
  - `global-metadata.dat` (61.6 MB) extracted for IL2CPP method RVA resolution
- **Anticheat & Security Libraries**:
  - `libunity.so` (246.8 MB)
  - `libanort.so` (1.68 MB - Tencent ACE Anticheat)
  - `libanogs.so` (5.53 MB - Tencent ACE Guard)
  - `libsaf.so` (Security Anti-Fraud)
  - `libCrashSight.so` (CrashSight reporter)
- **Configuration & Bypass Suite**:
  - `MSDKConfig.ini` (SG ITOP SDK, anti-telemetry safe)
  - `MSDKBuglyConfig.json`, `MSDKRetMsg.json`, `settings.xml`
  - `ConfigCache/3771080214970436972/1.6.57.0/` configs

---

## 3. Project Integration Status
| Feature | Target Version | Project Engine / Patcher | Status |
| :--- | :--- | :--- | :--- |
| MLBB Drone View (1.5X..5X) | 2.2.16.1232.1 | `MlbbDroneViewPatcher.java` | 100% Verified |
| MLBB God Mode / Damage | 2.2.16.1232.1 | `MlbbConfigPatcher.java` | 100% Verified |
| MLBB Anti-Redownload Lock | 2.2.16.1232.1 | `MlbbDroneViewPatcher.chmod 444` | 100% Verified |
| CODM 185Hz / Graphic Max | 1.6.57.0 | `CodmConfigPatcher.java` | 100% Verified |
| CODM Anticheat Cloak | 1.6.57.0 | `codm_antiban_bypass.js` & `codm_hooks.js` | 100% Verified |
| CODM ConfigCache 1.6.57.0 | 1.6.57.0 | `assets/codm/Config/ConfigCache` | 100% Verified |
