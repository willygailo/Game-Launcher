# ====================================================================
# Game Launcher PRO — Full R8 / ProGuard Optimization Rules
# ====================================================================

# Keep JNI native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# WebView JS bridge: 12 @JavascriptInterface methods in GameBoosterJsInterface
# are invoked from JavaScript (invisible to R8) — keep every annotated method.
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Shizuku API: ShizukuExecutor resolves Shizuku.newProcess(..) reflectively
# to spawn the native Shizuku command pipeline.
-keep class dev.rikka.shizuku.** { *; }
-dontwarn dev.rikka.shizuku.**

# LSPosed / Xposed module — entry loaded by the framework from assets/xposed_init.
# R8 must never strip, rename, or inline the module entry + hooks.
-keep class com.gamebooster.app.spoofer.lsposed.** { *; }
-keep class de.robv.android.xposed.** { *; }
-keepclassmembers class * implements de.robv.android.xposed.IXposedHookLoadPackage {
    public void handleLoadPackage(de.robv.android.xposed.callbacks.XC_LoadPackage$LoadPackageParam);
}
-dontwarn de.robv.android.xposed.**

# Glide image loading optimization & keep rules
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
    <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}
-dontwarn com.bumptech.glide.**

# Optimization & byte-code tuning
-repackageclasses 'com.gamebooster.app.opt'
-allowaccessmodification
-mergeinterfacesaggressively
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,SourceFile,LineNumberTable