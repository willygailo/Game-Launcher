# ====================================================================
# Game Launcher PRO — Safe ProGuard & R8 Keep Rules
# ====================================================================

# Keep all project classes and members to prevent runtime ClassNotFoundException
-keep class com.gamebooster.app.** { *; }
-dontwarn com.gamebooster.app.**

# Keep all Android components (Activities, Services, Receivers, Fragments, Views)
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends androidx.fragment.app.Fragment {
    public <init>();
}
-keep public class * extends androidx.fragment.app.DialogFragment {
    public <init>();
}
-keep public class * extends androidx.recyclerview.widget.RecyclerView$ViewHolder {
    public <init>(android.view.View);
}
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Keep JNI native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# WebView JS bridge: @JavascriptInterface methods
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Shizuku API: Keep all Shizuku AIDL and internal classes
-keep class dev.rikka.shizuku.** { *; }
-dontwarn dev.rikka.shizuku.**
-keep class rikka.shizuku.** { *; }
-dontwarn rikka.shizuku.**

# Glide image loading
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
    <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}
-dontwarn com.bumptech.glide.**

# Retain annotations and stack traces for crash diagnostics
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,SourceFile,LineNumberTable