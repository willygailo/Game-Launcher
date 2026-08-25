# ProGuard & R8 Optimization and Keep Rules for Game Booster PRO

# Keep Android Application & Core Components
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends androidx.fragment.app.Fragment

# Keep Shizuku API, Binder & AIDL Interfaces
-dontwarn rikka.shizuku.**
-keep class rikka.shizuku.** { *; }
-keep interface rikka.shizuku.** { *; }
-keep class moe.shizuku.** { *; }
-keep interface moe.shizuku.** { *; }
-keep class com.gamebooster.app.shizuku.** { *; }
-keep interface com.gamebooster.app.shizuku.** { *; }

# Keep Diagnostics, CrashLog & System Telemetry Models
-keep class com.gamebooster.app.diagnostics.** { *; }
-keep class com.gamebooster.app.engine.** { *; }
-keep class com.gamebooster.app.device.** { *; }
-keep class com.gamebooster.app.tweaks.** { *; }
-keep class com.gamebooster.app.spoofer.** { *; }
-keep class com.gamebooster.app.gamemanager.** { *; }
-keep class com.gamebooster.app.games.** { *; }
-keep class com.gamebooster.app.config.** { *; }
-keep class com.gamebooster.app.overlay.** { *; }
-keep class com.gamebooster.app.terminal.** { *; }

# Keep native C++ JNI methods and bindings
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep FileProvider for share/export intents
-keep class androidx.core.content.FileProvider { *; }

# Keep Glide Models & Loaders
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-dontwarn com.bumptech.glide.**

# Keep View constructors for XML layout inflation
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Keep Custom UI Views
-keep class com.gamebooster.app.ui.views.** { *; }
