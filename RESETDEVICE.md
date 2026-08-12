# 🔄 Android System & Hardware Engine Device Reset Guide

Complete reference of all ADB, Shizuku, and Shell commands to reset all tweaked system settings, display refresh rates, ANGLE graphics drivers, GPU renderers, touch parameters, OEM hardware profiles, CPU governors, and spoof profiles back to stock Android default.

---

## 1. ⚡ Refresh Rate & Display Hz Reset

Reverts forced 120Hz / 144Hz / 165Hz overrides and restores Android dynamic refresh rate scaling.

```bash
# AOSP System, Global & Secure Refresh Rate Reset
settings delete system peak_refresh_rate
settings delete system min_refresh_rate
settings delete system user_refresh_rate
settings delete global peak_refresh_rate
settings delete global min_refresh_rate
settings delete global user_refresh_rate
settings delete global display_downscale_disable
settings delete secure user_refresh_rate
settings delete secure peak_refresh_rate
settings delete secure min_refresh_rate

# OEM-Specific Display Refresh Rate Overrides Reset
settings delete system oplus_customize_screen_refresh_rate
settings delete system vivo_screen_refresh_rate

# SurfaceFlinger Direct Binder Reset (Auto-mode = 0)
service call SurfaceFlinger 1035 i32 0
service call SurfaceFlinger 1036 i32 0
service call SurfaceFlinger 1034 i32 0
service call SurfaceFlinger 1037 i32 0

# Android Game Mode & Window Manager Reset
cmd game mode standard global
cmd window reset-app-refresh-rate global
device_config delete game_overlay global

# Runtime FPS Limit Props Reset
setprop debug.sf.fps_limit ""
setprop persist.sys.NV_FPSLIMIT ""
setprop persist.vendor.dfps.level ""
```

---

## 2. 🎮 ANGLE & Game Driver Graphics Reset

Resets ANGLE (OpenGL ES to Vulkan translation layer) and System Game Driver forced assignments back to default system selection.

```bash
# ANGLE Driver Global & Per-App Reset
settings delete global angle_gl_driver_all_angle
settings delete global angle_gl_driver_selection_pkgs
settings delete global angle_gl_driver_selection_values
settings delete global angle_enabled_pkgs
setprop debug.angle.backend ""

# System Game Driver & ANGLE Opt-In Reset
settings delete global game_driver_all_apps
settings delete global updatable_driver_all_apps
settings delete global updatable_driver_production_opt_in_apps
settings delete global game_driver_opt_in_apps
```

---

## 3. 🎨 GPU Renderer & Hardware Acceleration Reset

Restores standard HWUI rendering and EGL swap interval settings.

```bash
# GPU Render Backend Reset (Skia default)
setprop debug.hwui.renderer ""
setprop debug.renderengine.backend ""
setprop debug.egl.force_msaa 0
setprop debug.sf.hw 1

# Frame Pacing & Phase Offset Reset
setprop debug.gr.swapinterval ""
setprop debug.egl.swapinterval ""
setprop debug.sf.latch_unsignaled ""
setprop debug.sf.disable_backpressure ""
setprop debug.sf.early_app_phase_offset_ns ""
setprop debug.sf.early_sf_phase_offset_ns ""
setprop persist.sys.sf.native_mode ""
setprop vendor.display.enable_default_color_mode ""
```

---

## 4. 🎯 Touch Latency & Pointer Response Reset

Resets touch sampling rate, pointer speed, touch prediction latency, and long-press timeouts to Android defaults.

```bash
settings delete system touch_response_boost
settings delete system touch_prediction_latency
settings delete secure long_press_timeout
settings put system pointer_speed 5
setprop persist.sys.touch_boost ""
setprop persist.sys.touch.rate ""
setprop touch.pressure.scale ""
setprop ro.surface_flinger.set_idle_timer_ms ""
setprop ro.surface_flinger.set_touch_timer_ms ""
```

---

## 5. ⚡ OEM Hardware Matrix & Thermal Engine Reset

Restores OEM hardware features (Infinix/Tecno bypass charging, Samsung GOS auto thermal control, Xiaomi Joyose FPS target) and enables default thermal mitigation.

```bash
# Infinix / Tecno (Transsion) Reset
setprop sys.bypass.charging 0
setprop persist.sys.darlink.mode 0

# Samsung GOS & Performance Mode Reset
settings delete global game_auto_temperature_control
settings delete secure game_performance_mode

# Xiaomi / POCO Thermal Power Reset
setprop persist.sys.power.fps ""
setprop persist.vendor.power.dfps ""

# CPU Governor & Thermal Protection Reset
setprop persist.sys.thermal.mitigation 1
cmd thermalservice reset
cmd power reset-hints
cmd thermalservice override-status 0
```

---

## 6. 🎭 Hardware Device Spoofing Reset

Restores stock device model, manufacturer, brand, and build fingerprints.

```bash
# Reset system property overrides
setprop reset:ro.product.model
setprop reset:ro.product.brand
setprop reset:ro.product.manufacturer
setprop reset:ro.product.device
setprop reset:ro.product.board
setprop reset:ro.hardware
```

---

## 7. 📱 Display Density & DPI Scale Reset

Resets custom ADB display density (`wm density`) overrides back to device factory stock DPI and restores native window scaling.

```bash
# Reset system display density back to stock factory default
wm density reset

# Reset display resolution & size overrides
wm size reset
```

---

## 🚀 1-Tap Complete Device Reset Script

Execute this single command in ADB Shell or Shizuku Terminal to instantly reset all tweaked settings, refresh rates, graphics drivers, touch parameters, OEM hardware flags, and display density at once:

```bash
sh -c "settings delete system peak_refresh_rate; settings delete system min_refresh_rate; settings delete system user_refresh_rate; settings delete global peak_refresh_rate; settings delete global min_refresh_rate; settings delete global user_refresh_rate; settings delete global display_downscale_disable; settings delete secure user_refresh_rate; settings delete system oplus_customize_screen_refresh_rate; settings delete system vivo_screen_refresh_rate; settings delete global angle_gl_driver_all_angle; settings delete global game_driver_all_apps; settings delete global updatable_driver_production_opt_in_apps; settings delete system touch_prediction_latency; settings delete secure long_press_timeout; settings delete global game_auto_temperature_control; setprop sys.bypass.charging 0; setprop persist.sys.power.fps ''; setprop persist.vendor.power.dfps ''; setprop persist.sys.touch.rate ''; setprop debug.hwui.renderer ''; setprop debug.egl.force_msaa 0; setprop debug.angle.backend ''; setprop debug.sf.fps_limit ''; service call SurfaceFlinger 1035 i32 0; wm density reset; wm size reset; echo '✅ Complete device hardware & display density reset finished!'"
```
