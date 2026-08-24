# ProGuard rules for Game Launcher PRO

# Keep JNI / Native method bindings
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep NativeConfigInjector and all JNI models
-keep class com.gamebooster.app.config.NativeConfigInjector { *; }
-keep class com.gamebooster.app.spoofer.** { *; }
-keep class com.gamebooster.app.spoofer.hook.** { *; }

# Keep Shizuku AIDL & IPC classes
-keep class moe.shizuku.** { *; }
-keep interface moe.shizuku.** { *; }
-keep class rikka.shizuku.** { *; }
-keep interface rikka.shizuku.** { *; }

# Keep Shizuku UserService and AIDL interface
-keep class com.gamebooster.app.shizuku.UserService {
    public <init>();
    public <init>(android.content.Context);
    *;
}
-keep class com.gamebooster.app.shizuku.IUserService { *; }
-keep class com.gamebooster.app.shizuku.IUserService$* { *; }
-keep class com.gamebooster.app.shizuku.** { *; }
-keep interface com.gamebooster.app.shizuku.** { *; }

# Keep reflection fields (for Build & SystemProperties mutation)
-keepclassmembers class android.os.Build { *; }
-keepclassmembers class android.os.Build$VERSION { *; }

# Keep Glide annotations and generated code
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
    <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}

# Keep Data & Configuration Models
-keep class com.gamebooster.app.cleaner.model.** { *; }
-keep class com.gamebooster.app.games.** { *; }
-keep class com.gamebooster.app.tweaks.** { *; }
-keep class com.gamebooster.app.diagnostics.** { *; }
-keep class com.gamebooster.app.config.** { *; }
-keep class com.gamebooster.app.device.** { *; }

# Don't warn about internal packages
-dontwarn moe.shizuku.**
-dontwarn rikka.shizuku.**
