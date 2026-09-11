# ============================================================================
#  Game Booster PRO — Comprehensive ProGuard & R8 Optimization / Keep Rules
#  Ensures 100% functional integrity across all subsystems in Release APKs.
# ============================================================================

# ----------------------------------------------------------------------------
# 1. GENERAL OPTIMIZATIONS & COMPILER SETTINGS
# ----------------------------------------------------------------------------
-repackageclasses ''
-allowaccessmodification
-dontusemixedcaseclassnames
-verbose

# Suppress warnings from hidden Android framework APIs and build tools
-dontwarn android.os.SystemProperties
-dontwarn android.view.IWindowManager
-dontwarn android.app.IActivityManager
-dontwarn android.content.pm.IPackageManager
-dontwarn sun.misc.Unsafe

# ----------------------------------------------------------------------------
# 2. ANDROID CORE COMPONENTS & APPLICATION LIFECYCLE
# ----------------------------------------------------------------------------
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends androidx.fragment.app.Fragment

# Keep Parcelable & Serializable implementations
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
    <fields>;
    <methods>;
}
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep Enums values & valueOf methods
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ----------------------------------------------------------------------------
# 3. C++ NATIVE JNI METHODS & BINDINGS (cpp / libgamebooster_native.so)
# ----------------------------------------------------------------------------
-keepclasseswithmembernames class * {
    native <methods>;
}
-keep class com.gamebooster.app.config.NativeConfigInjector {
    public static native <methods>;
    public static *;
}
-keepclassmembers class * {
    @androidx.annotation.Keep <methods>;
    @androidx.annotation.Keep <fields>;
}

# ----------------------------------------------------------------------------
# 4. SHIZUKU API, BINDER IPC & AIDL USER SERVICES (rikka / moe / shizuku)
# ----------------------------------------------------------------------------
-dontwarn rikka.shizuku.**
-keep class rikka.shizuku.** { *; }
-keep interface rikka.shizuku.** { *; }
-keep class moe.shizuku.** { *; }
-keep interface moe.shizuku.** { *; }
-keep class com.gamebooster.app.shizuku.** { *; }
-keep interface com.gamebooster.app.shizuku.** { *; }

# Keep AIDL interfaces and stubs
-keep interface com.gamebooster.app.shizuku.IUserService { *; }
-keep class com.gamebooster.app.shizuku.IUserService$Stub { *; }
-keep class com.gamebooster.app.shizuku.IUserService$Stub$Proxy { *; }
-keep class com.gamebooster.app.shizuku.UserService { *; }

# ----------------------------------------------------------------------------
# 5. HARDWARE DEVICE IDENTITY SPOOFER & REFLECTION RULES
# ----------------------------------------------------------------------------
-keep class com.gamebooster.app.spoofer.** { *; }
-keep interface com.gamebooster.app.spoofer.** { *; }

# Preserve android.os.Build fields for HardwareMaskEngine reflection spoofing
-keepclassmembers class android.os.Build {
    public static java.lang.String MODEL;
    public static java.lang.String BRAND;
    public static java.lang.String MANUFACTURER;
    public static java.lang.String DEVICE;
    public static java.lang.String PRODUCT;
    public static java.lang.String BOARD;
    public static java.lang.String HARDWARE;
    public static java.lang.String FINGERPRINT;
    public static java.lang.String DISPLAY;
    public static java.lang.String ID;
    public static java.lang.String TAGS;
    public static java.lang.String TYPE;
    public static java.lang.String HOST;
    public static java.lang.String USER;
}
-keepclassmembers class android.os.Build$VERSION {
    public static java.lang.String INCREMENTAL;
    public static java.lang.String RELEASE;
    public static int SDK_INT;
}

# ----------------------------------------------------------------------------
# 6. ALL INTERNAL SUBSYSTEMS & MODULES (com.gamebooster.app.**)
# ----------------------------------------------------------------------------
-keep class com.gamebooster.app.booster.** { *; }
-keep class com.gamebooster.app.config.** { *; }
-keep class com.gamebooster.app.core.** { *; }
-keep class com.gamebooster.app.device.** { *; }
-keep class com.gamebooster.app.diagnostics.** { *; }
-keep class com.gamebooster.app.engine.** { *; }
-keep class com.gamebooster.app.focus.** { *; }
-keep class com.gamebooster.app.gamemanager.** { *; }
-keep class com.gamebooster.app.games.** { *; }
-keep class com.gamebooster.app.gamespace.** { *; }
-keep class com.gamebooster.app.overlay.** { *; }
-keep class com.gamebooster.app.search.** { *; }
-keep class com.gamebooster.app.services.** { *; }
-keep class com.gamebooster.app.terminal.** { *; }
-keep class com.gamebooster.app.tweaks.** { *; }
-keep class com.gamebooster.app.ui.** { *; }
-keep class com.gamebooster.app.utils.** { *; }

# ----------------------------------------------------------------------------
# 7. UI CUSTOM VIEWS, XML INFLATION & LAYOUT ANIMATIONS
# ----------------------------------------------------------------------------
-keep class com.gamebooster.app.ui.views.** { *; }

# Keep View constructors for XML layout inflation
-keepclasseswithmembers class * {
    public <init>(android.content.Context);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int, int);
}

# Keep onClick callback methods in Activities
-keepclassmembers class * extends android.app.Activity {
    public void *(android.view.View);
}

# ----------------------------------------------------------------------------
# 8. ANDROIDX & THIRD-PARTY LIBRARIES
# ----------------------------------------------------------------------------
# FileProvider for diagnostics share/export intents
-keep class androidx.core.content.FileProvider { *; }

# AndroidX Lifecycle & ViewModel
-keep class androidx.lifecycle.** { *; }
-dontwarn androidx.lifecycle.**

# Google Material Components
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**

# Glide Image & GIF Loading
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep class com.bumptech.glide.** { *; }
-dontwarn com.bumptech.glide.**
-keep public class * extends com.bumptech.glide.annotation.compiler.Index
-keepclassmembers class * {
    @com.bumptech.glide.annotation.GlideOption <methods>;
    @com.bumptech.glide.annotation.GlideExtension <methods>;
}
