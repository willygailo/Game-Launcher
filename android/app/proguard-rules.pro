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

# ====================================================================
# LSPosed / Xposed module — entry loaded by the framework from assets/xposed_init.
# R8 must never strip, rename, or inline the module entry + hooks.
# ====================================================================
-keep class com.gamebooster.app.spoofer.lsposed.** { *; }
-keep class de.robv.android.xposed.** { *; }
-keepclassmembers class * implements de.robv.android.xposed.IXposedHookLoadPackage {
    public void handleLoadPackage(de.robv.android.xposed.callbacks.XC_LoadPackage$LoadPackageParam);
}