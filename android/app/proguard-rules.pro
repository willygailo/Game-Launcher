# ====================================================================
# Game Launcher PRO — R8 / ProGuard keep rules
# ====================================================================

# WebView JS bridge: 12 @JavascriptInterface methods in GameBoosterJsInterface
# are invoked from JavaScript (invisible to R8) — keep every annotated method.
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Shizuku API: ShizukuExecutor resolves Shizuku.newProcess(..) reflectively
# to spawn the native Shizuku command pipeline.
-keep class dev.rikka.shizuku.** { *; }