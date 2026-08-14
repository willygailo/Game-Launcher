# 🔄 1-TAP COMPLETE ANDROID SYSTEM & HARDWARE ENGINE RESET

> **All-In-One Master Reset Reference**: This guide provides a single, instant copy-paste master command to completely reset all display refresh rates, ANGLE/Vulkan graphics drivers, touch latency overrides, OEM performance governors, bypass charging, Game Mode overlays, and Private DNS settings back to factory Android defaults.

---

## ⚡ 1-TAP MASTER COPY-ALL RESET COMMAND

Copy and paste this **single command** directly into **Shizuku Terminal**, **Termux**, **ADB Shell (via PC)**, or **Local ADB**:

```bash
sh -c "settings delete system peak_refresh_rate; settings delete system min_refresh_rate; settings delete system user_refresh_rate; settings delete system default_refresh_rate; settings delete system miui_refresh_rate; settings delete system joyose_game_fps; settings delete system oplus_customize_screen_refresh_rate; settings delete system oppo_screen_refresh_rate; settings delete system oppo_display_refresh_rate; settings delete system vivo_screen_refresh_rate; settings delete system iqoo_game_fps_target; settings delete system infinix_refresh_rate_mode; settings delete system xos_display_refresh_rate; settings delete system tecno_refresh_rate_mode; settings delete system hios_display_refresh_rate; settings delete system transsion_refresh_rate_mode; settings delete system nubia_refresh_rate; settings delete system redmagic_game_mode; settings delete system honor_screen_refresh_rate; settings delete system hw_display_refresh_rate; settings delete system touch_prediction_latency; settings put system pointer_speed 5; settings delete global peak_refresh_rate; settings delete global min_refresh_rate; settings delete global user_refresh_rate; settings delete global default_refresh_rate; settings delete global display_downscale_disable; settings delete global mode_fps_override; settings delete global fps_limit; settings delete global sf_max_fps; settings delete global updatable_driver_all_apps; settings delete global updatable_driver_production_opt_in_apps; settings delete global game_driver_all_apps; settings delete global game_driver_opt_in_apps; settings delete global angle_gl_driver_all_angle; settings delete global angle_gl_driver_selection_pkgs; settings delete global game_auto_temperature_control; settings put global private_dns_mode opportunistic; settings delete global private_dns_specifier; settings delete secure user_refresh_rate; settings delete secure peak_refresh_rate; settings delete secure min_refresh_rate; settings delete secure refresh_rate_mode; settings delete secure match_content_frame_rate; settings delete secure oplus_customize_display_level; settings delete secure high_refresh_rate_apps_list; settings delete secure game_performance_mode; settings delete secure long_press_timeout; cmd connectivity set-private-dns-mode opportunistic; cmd game mode standard global; cmd window reset-app-refresh-rate global; cmd thermalservice override-status -1; cmd thermal override-status -1; cmd thermalservice reset; cmd power reset-hints; service call SurfaceFlinger 1035 i32 0; service call SurfaceFlinger 1036 i32 0; service call SurfaceFlinger 1037 i32 0; service call SurfaceFlinger 1034 i32 0; setprop debug.sf.fps_override ''; setprop debug.sf.latch_unsignaled ''; setprop debug.sf.enable_gl_backpressure ''; setprop debug.sf.early_phase_offset_ns ''; setprop debug.sf.early_app_phase_offset_ns ''; setprop debug.sf.early_sf_phase_offset_ns ''; setprop debug.sf.high_fps_early_phase_offset_ns ''; setprop debug.sf.high_fps_early_app_phase_offset_ns ''; setprop debug.sf.high_fps_early_sf_phase_offset_ns ''; setprop debug.hwui.renderer ''; setprop debug.egl.force_msaa 0; setprop debug.angle.backend ''; setprop persist.sys.touch.rate ''; setprop view.touch_slop ''; setprop debug.touch.pressure.scale ''; setprop sys.bypass.charging 0; setprop persist.sys.darlink.mode 0; setprop persist.sys.phx.fps ''; setprop persist.sys.game.fps ''; setprop sys.infinix.fps ''; setprop sys.tecno.fps ''; setprop sys.oem.fps_limit ''; setprop sys.gos.fps_limit ''; setprop persist.sys.power.fps ''; setprop persist.vendor.power.dfps ''; setprop persist.sys.joyose.fps ''; setprop sys.thermal.mode 0; setprop sys.asus.gaming.mode 0; setprop persist.sys.asus.hz ''; setprop sys.asus.fps ''; setprop sys.nubia.game.mode 0; setprop persist.sys.nubia.hz ''; setprop sys.nubia.fps ''; setprop persist.sys.hw.fps ''; setprop sys.perf.game 0; wm density reset; wm size reset; am force-stop com.gamebooster.app; echo '✅ [SUCCESS] 100% Full System, Display, OEM Tweaks & Network Reset Complete!'"
```

---

## 💻 PC ADB Terminal Version (Single Copy-Paste)

If running from a Windows CMD, PowerShell, or macOS/Linux terminal with your device connected via USB/Wi-Fi ADB:

```bash
adb shell 'settings delete system peak_refresh_rate; settings delete system min_refresh_rate; settings delete system user_refresh_rate; settings delete global peak_refresh_rate; settings delete global min_refresh_rate; settings delete global user_refresh_rate; settings delete global display_downscale_disable; settings delete global updatable_driver_production_opt_in_apps; settings delete global game_driver_all_apps; settings put global private_dns_mode opportunistic; settings delete global private_dns_specifier; settings delete secure refresh_rate_mode; settings delete secure match_content_frame_rate; cmd connectivity set-private-dns-mode opportunistic; cmd game mode standard global; cmd window reset-app-refresh-rate global; cmd thermalservice override-status -1; service call SurfaceFlinger 1035 i32 0; service call SurfaceFlinger 1036 i32 0; service call SurfaceFlinger 1037 i32 0; setprop debug.sf.fps_override ""; setprop debug.hwui.renderer ""; setprop sys.bypass.charging 0; setprop persist.sys.darlink.mode 0; setprop persist.sys.joyose.fps ""; setprop sys.asus.gaming.mode 0; setprop sys.nubia.game.mode 0; wm density reset; wm size reset; am force-stop com.gamebooster.app; echo "✅ Reset Done!"'
```

---

## 📑 Detailed Breakdown by Module

### 1. ⚡ Refresh Rate & Display Hz Reset
Reverts forced 90Hz / 120Hz / 144Hz / 165Hz overrides and restores standard dynamic refresh rate scaling across AOSP and OEM layers:
```bash
# System, Global & Secure Settings
settings delete system peak_refresh_rate
settings delete system min_refresh_rate
settings delete system user_refresh_rate
settings delete system default_refresh_rate
settings delete global peak_refresh_rate
settings delete global min_refresh_rate
settings delete global user_refresh_rate
settings delete global default_refresh_rate
settings delete global display_downscale_disable
settings delete global mode_fps_override
settings delete global sf_max_fps
settings delete secure user_refresh_rate
settings delete secure peak_refresh_rate
settings delete secure min_refresh_rate
settings delete secure refresh_rate_mode
settings delete secure match_content_frame_rate

# SurfaceFlinger Display Mode Binder Reset
service call SurfaceFlinger 1035 i32 0
service call SurfaceFlinger 1036 i32 0
service call SurfaceFlinger 1037 i32 0
service call SurfaceFlinger 1034 i32 0

# SurfaceFlinger Frame-Pacing Properties
setprop debug.sf.fps_override ""
setprop debug.sf.latch_unsignaled ""
setprop debug.sf.enable_gl_backpressure ""
setprop debug.sf.early_phase_offset_ns ""
setprop debug.sf.early_app_phase_offset_ns ""
setprop debug.sf.early_sf_phase_offset_ns ""
setprop debug.sf.high_fps_early_phase_offset_ns ""
setprop debug.sf.high_fps_early_app_phase_offset_ns ""
setprop debug.sf.high_fps_early_sf_phase_offset_ns ""
```

---

### 2. 🎮 ANGLE & Game Driver Graphics Reset
Resets ANGLE (OpenGL ES to Vulkan translation layer) and System Updatable Game Driver assignments back to stock:
```bash
settings delete global angle_gl_driver_all_angle
settings delete global angle_gl_driver_selection_pkgs
settings delete global angle_gl_driver_selection_values
settings delete global angle_enabled_pkgs
settings delete global game_driver_all_apps
settings delete global updatable_driver_all_apps
settings delete global updatable_driver_production_opt_in_apps
settings delete global game_driver_opt_in_apps
setprop debug.angle.backend ""
setprop debug.hwui.renderer ""
setprop debug.egl.force_msaa 0
```

---

### 3. 🎯 Touch Latency & Pointer Response Reset
Restores touch prediction latency, touch polling rate, and pointer speed to standard defaults:
```bash
settings delete system touch_prediction_latency
settings put system pointer_speed 5
settings delete secure long_press_timeout
setprop persist.sys.touch.rate ""
setprop view.touch_slop ""
setprop debug.touch.pressure.scale ""
```

---

### 4. 🏢 OEM Hardware Matrix Reset (Infinix, Tecno, Samsung, Xiaomi, Vivo, Oppo, ASUS, REDMAGIC, Honor)
Reverts vendor-specific game mode flags and thermal overrides:
```bash
# Infinix & Tecno (Transsion HiOS / XOS)
setprop sys.bypass.charging 0
setprop persist.sys.darlink.mode 0
setprop persist.sys.phx.fps ""
setprop persist.sys.game.fps ""
setprop sys.infinix.fps ""
setprop sys.tecno.fps ""
setprop sys.oem.fps_limit ""
settings delete system infinix_refresh_rate_mode
settings delete system xos_display_refresh_rate
settings delete system tecno_refresh_rate_mode
settings delete system hios_display_refresh_rate
settings delete system transsion_refresh_rate_mode

# Samsung One UI (GOS Bypass)
settings delete global game_auto_temperature_control
settings delete secure game_performance_mode
setprop sys.gos.fps_limit ""

# Xiaomi / POCO / Redmi (HyperOS / MIUI / Joyose)
setprop persist.sys.power.fps ""
setprop persist.vendor.power.dfps ""
setprop persist.sys.joyose.fps ""
setprop sys.thermal.mode 0
settings delete system miui_refresh_rate
settings delete system joyose_game_fps

# OPPO / Realme / OnePlus (ColorOS / OxygenOS / HyperBoost)
settings delete system oplus_customize_screen_refresh_rate
settings delete system oppo_screen_refresh_rate
settings delete system oppo_display_refresh_rate
settings delete secure oplus_customize_display_level

# Vivo / iQOO (OriginOS / FuntouchOS)
settings delete system vivo_screen_refresh_rate
settings delete system iqoo_game_fps_target
settings delete secure high_refresh_rate_apps_list

# ASUS ROG (X-Mode)
setprop sys.asus.gaming.mode 0
setprop persist.sys.asus.hz ""
setprop sys.asus.fps ""

# Nubia / REDMAGIC (Game Space)
setprop sys.nubia.game.mode 0
setprop persist.sys.nubia.hz ""
setprop sys.nubia.fps ""
settings delete system nubia_refresh_rate
settings delete system redmagic_game_mode

# Honor & Huawei (MagicOS / EMUI)
settings delete system honor_screen_refresh_rate
settings delete system hw_display_refresh_rate
setprop persist.sys.hw.fps ""
setprop sys.perf.game 0
```

---

### 5. 🌐 Gaming Private DNS & Network Reset
Restores Private DNS mode back to standard `opportunistic` default:
```bash
settings put global private_dns_mode opportunistic
settings delete global private_dns_specifier
cmd connectivity set-private-dns-mode opportunistic
```

---

### 6. 📱 Window Resolution & DPI Scale Reset
Restores native display resolution and factory stock density:
```bash
wm density reset
wm size reset
```

---

### 7. 🛑 Stop Game Booster Background Services
Gracefully terminates all background monitoring daemons, Hz locks, and floating overlay windows:
```bash
am force-stop com.gamebooster.app
```
