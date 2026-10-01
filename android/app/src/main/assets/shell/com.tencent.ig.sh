#!/system/bin/sh
# ─────────────────────────────────────────────────────────────────────────────
# com.tencent.ig.sh — PUBG Mobile Ultra Tuning (Fix Loading Screen Hang)
# Target: com.tencent.ig (GLOBAL ONLY)
# ─────────────────────────────────────────────────────────────────────────────

# Restore EGL, Swappy, and SurfaceFlinger defaults to prevent UE4 loading deadlock
setprop debug.egl.swapinterval "" 2>/dev/null
setprop debug.swappy.swap_interval "" 2>/dev/null
setprop swappy.disable 0 2>/dev/null
setprop debug.sf.latch_unsignaled "" 2>/dev/null
setprop debug.sf.disable_backpressure "" 2>/dev/null
setprop debug.hwui.renderer "" 2>/dev/null

# CPU Governor — Lock all CPU policy clusters to performance
for p in /sys/devices/system/cpu/cpufreq/policy*/scaling_governor; do
  echo performance > "$p" 2>/dev/null
done

# ── 185fps Refresh Rate Unlock (PUBGM — UE4 FPS unlock) ──
settings put system peak_refresh_rate 185.0 2>/dev/null
settings put system min_refresh_rate 185.0 2>/dev/null
settings put global game_mode_config 0 2>/dev/null
setprop debug.sf.fps_limit 185 2>/dev/null
setprop persist.sys.NV_FPSLIMIT 185 2>/dev/null
setprop persist.game_mode.performance.fps 185 2>/dev/null
setprop persist.sys.game.fps 185 2>/dev/null

# Kernel display nodes (chipset vendors)
echo 185 > /sys/devices/virtual/graphics/fb0/dynamic_fps 2>/dev/null
echo 185 > /sys/class/graphics/fb0/dynamic_fps 2>/dev/null
echo 185 > /sys/devices/platform/mtk_disp_mgr.0/refresh_rate 2>/dev/null
echo 185 > /proc/mtk_display/fps 2>/dev/null

# GPU — Maximum clock level
setprop debug.adreno.turbo 1 2>/dev/null
setprop vendor.gpu.power_mode 1 2>/dev/null

# Network — Anti-jitter & low latency TCP
sysctl -w net.ipv4.tcp_congestion_control=bbr 2>/dev/null
sysctl -w net.ipv4.tcp_notsent_lowat=16384 2>/dev/null
sysctl -w net.ipv4.tcp_mtu_probing=1 2>/dev/null

# Non-Blocking Network Handshake Pass-Through (Fixes PUBGM GCloud Login Loading)
iptables -F OUTPUT 2>/dev/null
ip6tables -F OUTPUT 2>/dev/null
ndc resolver flushdefaultif 2>/dev/null

# GameMode API clamp removal for PUBGM
cmd game set --mode 2 --user 0 com.tencent.ig 2>/dev/null

