# ============================================================================
#  Game Booster PRO — Comprehensive ProGuard & R8 Optimization / Keep Rules
#  Ensures 100% functional integrity across all subsystems in Release APKs.
# ============================================================================

# ----------------------------------------------------------------------------
# 1. ANDROID CORE COMPONENTS & APPLICATION LIFECYCLE
# ----------------------------------------------------------------------------
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends androidx.fragment.app.Fragment

# ----------------------------------------------------------------------------
# 2. C++ NATIVE JNI METHODS & BINDINGS (cpp / libgamebooster_native.so)
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
# 3. GAME CONFIGURATION & ENGINE PATCHERS (com.gamebooster.app.config.**)
# ----------------------------------------------------------------------------
-keep class com.gamebooster.app.config.** { *; }
-keep interface com.gamebooster.app.config.** { *; }

# ----------------------------------------------------------------------------
# 4. HARDWARE & PERFORMANCE BOOSTERS (com.gamebooster.app.booster.**)
# ----------------------------------------------------------------------------
-keep class com.gamebooster.app.booster.** { *; }
-keep interface com.gamebooster.app.booster.** { *; }

# ----------------------------------------------------------------------------
# 5. STORAGE & JUNK CLEANER ENGINE (com.gamebooster.app.cleaner.**)
# ----------------------------------------------------------------------------
-keep class com.gamebooster.app.cleaner.** { *; }
-keep interface com.gamebooster.app.cleaner.** { *; }

# ----------------------------------------------------------------------------
# 6. SYSTEM OPTIMIZATION ENGINES & COMMAND EXECUTORS (com.gamebooster.app.engine.**)
# ----------------------------------------------------------------------------
-keep class com.gamebooster.app.engine.** { *; }
-keep interface com.gamebooster.app.engine.** { *; }

# ----------------------------------------------------------------------------
# 7. DIAGNOSTICS & CRASHLOG TELEMETRY (com.gamebooster.app.diagnostics.**)
# ----------------------------------------------------------------------------
-keep class com.gamebooster.app.diagnostics.** { *; }
-keep interface com.gamebooster.app.diagnostics.** { *; }

# ----------------------------------------------------------------------------
# 8. SHIZUKU API, BINDER IPC & AIDL USER SERVICES (rikka / moe / shizuku)
# ----------------------------------------------------------------------------
-dontwarn rikka.shizuku.**
-keep class rikka.shizuku.** { *; }
-keep interface rikka.shizuku.** { *; }
-keep class moe.shizuku.** { *; }
-keep interface moe.shizuku.** { *; }
-keep class com.gamebooster.app.shizuku.** { *; }
-keep interface com.gamebooster.app.shizuku.** { *; }

# ----------------------------------------------------------------------------
# 9. HARDWARE DEVICE IDENTITY SPOOFER (com.gamebooster.app.spoofer.**)
# ----------------------------------------------------------------------------
-keep class com.gamebooster.app.spoofer.** { *; }
-keep interface com.gamebooster.app.spoofer.** { *; }

# ----------------------------------------------------------------------------
# 10. GAME MANAGER, GAMESPACE & SESSION ENGINES
# ----------------------------------------------------------------------------
-keep class com.gamebooster.app.gamemanager.** { *; }
-keep class com.gamebooster.app.gamespace.** { *; }
-keep class com.gamebooster.app.games.** { *; }

# ----------------------------------------------------------------------------
# 11. OVERLAY HUD & REAL-TIME FPS MONITORS (com.gamebooster.app.overlay.**)
# ----------------------------------------------------------------------------
-keep class com.gamebooster.app.overlay.** { *; }
-keep interface com.gamebooster.app.overlay.** { *; }

# ----------------------------------------------------------------------------
# 12. CYBER TERMINAL & TWEAKS CONSOLE (com.gamebooster.app.terminal.**)
# ----------------------------------------------------------------------------
-keep class com.gamebooster.app.terminal.** { *; }
-keep class com.gamebooster.app.tweaks.** { *; }

# ----------------------------------------------------------------------------
# 13. DEVICE HARDWARE, DISPLAY & CORE SYSTEM MANAGERS
# ----------------------------------------------------------------------------
-keep class com.gamebooster.app.device.** { *; }
-keep class com.gamebooster.app.core.** { *; }
-keep class com.gamebooster.app.services.** { *; }
-keep class com.gamebooster.app.search.** { *; }
-keep class com.gamebooster.app.utils.** { *; }

# ----------------------------------------------------------------------------
# 14. UI COMPONENTS, ADAPTERS, DIALOGS, FRAGMENTS & CUSTOM VIEWS
# ----------------------------------------------------------------------------
-keep class com.gamebooster.app.ui.** { *; }
-keep class com.gamebooster.app.ui.views.** { *; }

# Keep View constructors for XML layout inflation
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# ----------------------------------------------------------------------------
# 15. THIRD-PARTY LIBRARIES & FILE PROVIDER
# ----------------------------------------------------------------------------
# FileProvider for diagnostics share/export intents
-keep class androidx.core.content.FileProvider { *; }

# Glide Image & GIF Loading
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep class com.bumptech.glide.** { *; }
-dontwarn com.bumptech.glide.**

# AndroidX Lifecycle & ViewModel
-keep class androidx.lifecycle.** { *; }
-dontwarn androidx.lifecycle.**
