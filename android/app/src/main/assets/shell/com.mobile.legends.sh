#!/system/bin/sh
# ─────────────────────────────────────────────────────────────────────────────
# com.mobile.legends.sh — Mobile Legends: Bang Bang tuning
# Target: com.mobile.legends (GLOBAL ONLY)
# Updated: Sep 30 2026 — 185fps unlock + AC telemetry null-route + global-only
# ─────────────────────────────────────────────────────────────────────────────

# Force ultra graphics path via Vulkan renderer
setprop debug.hwui.renderer vulkan
setprop debug.vulkan.layers ""
setprop debug.vulkan.enable_validation_layers 0

# Disable developer-mode jank detection that throttles rendering
setprop debug.hwui.show_overflow 0
setprop debug.hwui.profile false

# MLBB network — lower ping route
sysctl -w net.ipv4.tcp_keepalive_intvl=15 2>/dev/null
sysctl -w net.ipv4.tcp_keepalive_probes=5 2>/dev/null
sysctl -w net.ipv4.tcp_keepalive_time=60 2>/dev/null
sysctl -w net.ipv4.tcp_rmem="4096 87380 6291456" 2>/dev/null
sysctl -w net.ipv4.tcp_wmem="4096 16384 4194304" 2>/dev/null

# Adreno — MLBB uses OpenGL ES 3.2, push quality flags
setprop ro.hardware.egl adreno
setprop debug.egl.swapinterval -1
setprop debug.egl.buffcount 3

# ── 185fps Force Unlock (MLBB-specific Hz lock — resists OEM reverting mid-match) ──
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

# Drop caches before first frame
echo 1 > /proc/sys/vm/drop_caches 2>/dev/null

# ── MLBB bind-mount ResCheckConf as read-only (prevents Moonton bg integrity overwrite) ──
RES_DIR="/sdcard/Android/data/com.mobile.legends/files/dragon2017/res"
if [ -d "$RES_DIR" ]; then
    test -f "$RES_DIR/ResCheckConf.xml" && mount --bind "$RES_DIR/ResCheckConf.xml" "$RES_DIR/ResCheckConf.xml" 2>/dev/null && mount -o remount,ro "$RES_DIR/ResCheckConf.xml" 2>/dev/null
    test -f "$RES_DIR/res_skip_patch.xml" && chmod 444 "$RES_DIR/res_skip_patch.xml" 2>/dev/null
fi

# ── AC Telemetry Null-Route (crash/bug reports only — NOT heartbeat to avoid blackout flag) ──
for HOST in bugly.qq.com androidsdk.bugly.qq.com ac.tosshub.com; do
    iptables -A OUTPUT -d "$HOST" -j DROP 2>/dev/null
    ip6tables -A OUTPUT -d "$HOST" -j DROP 2>/dev/null
done

# GameMode API clamp removal for MLBB
cmd game set --mode 2 --user 0 com.mobile.legends 2>/dev/null
