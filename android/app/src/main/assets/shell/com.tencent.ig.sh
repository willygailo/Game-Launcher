#!/system/bin/sh
# ─────────────────────────────────────────────────────────────────────────────
# com.tencent.ig.sh — PUBG Mobile tuning
# Target: com.tencent.ig (GLOBAL ONLY)
# Updated: Sep 30 2026 — 185fps unlock + AC telemetry null-route + global-only
# ─────────────────────────────────────────────────────────────────────────────

# PUBG uses Unreal Engine 4 — Vulkan renderer preferred (UE4 >= 4.25)
setprop debug.hwui.renderer vulkan
setprop debug.vulkan.enable_validation_layers 0
setprop debug.egl.swapinterval -1
setprop debug.egl.buffcount 3

# Unreal Engine threading — ensure game threads hit big cores (policy4=mid, policy7=prime)
for p in /sys/devices/system/cpu/cpufreq/policy4 /sys/devices/system/cpu/cpufreq/policy7; do
  echo performance > "$p/scaling_governor" 2>/dev/null
  cat "$p/cpuinfo_max_freq" > "$p/scaling_min_freq" 2>/dev/null
done

# ── 185fps Force Unlock (PUBGM — UE4 t.MaxFPS bypass via SurfaceFlinger + SwappyGL) ──
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
# Kernel-level display nodes (tries all chipset vendors)
echo 185 > /sys/devices/virtual/graphics/fb0/dynamic_fps 2>/dev/null
echo 185 > /sys/class/graphics/fb0/dynamic_fps 2>/dev/null
echo 185 > /sys/devices/platform/mtk_disp_mgr.0/refresh_rate 2>/dev/null
echo 185 > /proc/mtk_display/fps 2>/dev/null
echo 185 > /sys/devices/platform/exynos-drm/drm/card0/card0-DSI-1/max_fps 2>/dev/null

# PUBG network — anti jitter
sysctl -w net.ipv4.tcp_congestion_control=bbr 2>/dev/null
sysctl -w net.ipv4.tcp_notsent_lowat=16384 2>/dev/null
sysctl -w net.ipv4.tcp_mtu_probing=1 2>/dev/null

# GPU — PUBG UE4 benefits from buffer preload
setprop debug.adreno.turbo 1
setprop vendor.gpu.power_mode 1

# Memory — kill background aggressively for UE4
echo 1 > /proc/sys/vm/drop_caches 2>/dev/null
setprop ro.lmk.kill_heaviest_task true

# ── AC Telemetry Null-Route (Tencent ACE + crash endpoints only) ──
for HOST in bugly.qq.com crash.tencent.com androidsdk.bugly.qq.com ac.tosshub.com; do
    iptables -A OUTPUT -d "$HOST" -j DROP 2>/dev/null
    ip6tables -A OUTPUT -d "$HOST" -j DROP 2>/dev/null
done

# GameMode API clamp removal for PUBGM
cmd game set --mode 2 --user 0 com.tencent.ig 2>/dev/null
