#!/system/bin/sh
# ─────────────────────────────────────────────────────────────────────────────
# com.activision.callofduty.shooter.sh — Call of Duty Mobile tuning
# Target: com.activision.callofduty.shooter (GLOBAL ONLY)
# Updated: Sep 30 2026 — 185fps unlock + AC telemetry null-route + global-only
# ─────────────────────────────────────────────────────────────────────────────

# IW8 (COD) engine — Vulkan preferred renderer
setprop debug.hwui.renderer vulkan
setprop debug.vulkan.enable_validation_layers 0
setprop debug.egl.swapinterval -1

# Force max CPU on all policies — IW8 is multi-threaded heavily
for p in /sys/devices/system/cpu/cpufreq/policy*; do
  echo performance > "$p/scaling_governor" 2>/dev/null
  cat "$p/cpuinfo_max_freq" > "$p/scaling_min_freq" 2>/dev/null
done

# ── 185fps Force Unlock (CODM — IW8 engine ignores SwappyGL, use direct UE path) ──
settings put system peak_refresh_rate 185.0 2>/dev/null
settings put system min_refresh_rate 185.0 2>/dev/null
settings put global game_mode_config 0 2>/dev/null
setprop debug.sf.fps_limit 185
setprop persist.sys.NV_FPSLIMIT 185
setprop persist.game_mode.performance.fps 185
setprop swappy.disable 1
setprop debug.swappy.swap_interval 0
setprop debug.egl.swapinterval 0
setprop debug.sf.disable_backpressure 1
setprop debug.sf.latch_unsignaled 1
service call SurfaceFlinger 1034 i32 185 2>/dev/null
service call SurfaceFlinger 1035 i32 185 2>/dev/null
# Kernel-level display nodes
echo 185 > /sys/devices/virtual/graphics/fb0/dynamic_fps 2>/dev/null
echo 185 > /sys/class/graphics/fb0/dynamic_fps 2>/dev/null
echo 185 > /sys/devices/platform/mtk_disp_mgr.0/refresh_rate 2>/dev/null
echo 185 > /proc/mtk_display/fps 2>/dev/null
echo 185 > /sys/devices/platform/exynos-drm/drm/card0/card0-DSI-1/max_fps 2>/dev/null

# COD network — latency sensitive
sysctl -w net.ipv4.tcp_congestion_control=bbr 2>/dev/null
sysctl -w net.ipv4.tcp_quickack=1 2>/dev/null
sysctl -w net.ipv4.tcp_nodelay=1 2>/dev/null

# Thermal off — COD runs hot on heavy maps
stop thermal-engine 2>/dev/null
stop vendor.thermal-engine 2>/dev/null
for z in /sys/class/thermal/thermal_zone*; do
  echo disabled > "$z/mode" 2>/dev/null
done

# Drop caches — COD map loads need max contiguous RAM
echo 3 > /proc/sys/vm/drop_caches 2>/dev/null

# ── AC Telemetry Null-Route (crash/bug reports only — Tencent ACE endpoints) ──
for HOST in bugly.qq.com crash.tencent.com androidsdk.bugly.qq.com ac.tosshub.com; do
    iptables -A OUTPUT -d "$HOST" -j DROP 2>/dev/null
    ip6tables -A OUTPUT -d "$HOST" -j DROP 2>/dev/null
done

# GameMode API clamp removal for CODM
cmd game set --mode 2 --user 0 com.activision.callofduty.shooter 2>/dev/null
