#!/usr/bin/env sh
# ==============================================================================
# Game Launcher Pro - Device Gaming Environment Optimizer & Setup
# ==============================================================================

echo "=== [INITIALIZING GAMING ENVIRONMENT SETUP] ==="
MODEL=$(getprop ro.product.model 2>/dev/null)
SDK=$(getprop ro.build.version.sdk 2>/dev/null)
echo "Device: $MODEL (API $SDK)"

# 1. Performance and Display Refresh
settings put system peak_refresh_rate 185.0 2>/dev/null
settings put system min_refresh_rate 185.0 2>/dev/null
settings put global window_animation_scale 0.5 2>/dev/null
settings put global transition_animation_scale 0.5 2>/dev/null
settings put global animator_duration_scale 0.5 2>/dev/null

# 2. Game Driver & ANGLE
settings put global game_driver_all_apps 1 2>/dev/null
settings put global angle_gl_driver_all_angle 1 2>/dev/null

# 3. Hardware acceleration & rendering
setprop debug.egl.hw 1 2>/dev/null
setprop debug.sf.hw 1 2>/dev/null
setprop debug.hwui.renderer vulkan 2>/dev/null

echo "=== [DEVICE SETUP COMPLETED SUCCESSFULLY] ==="
