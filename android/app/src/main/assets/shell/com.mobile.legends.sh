#!/system/bin/sh
# ─────────────────────────────────────────────────────────────────────────────
# com.mobile.legends.sh — Mobile Legends: Bang Bang tuning
# Covers: com.mobile.legends, com.vng.mlbbvn, com.mobilelegends.mi
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

# Hz lock for MLBB match (avoid OEM reverting mid-match)
settings put system peak_refresh_rate {TARGET_HZ}.0 2>/dev/null
settings put system min_refresh_rate {TARGET_HZ}.0 2>/dev/null
setprop debug.sf.fps_limit {TARGET_HZ}

# Drop caches before first frame
echo 1 > /proc/sys/vm/drop_caches 2>/dev/null
