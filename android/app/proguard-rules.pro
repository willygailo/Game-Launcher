# WebView exposes only explicitly annotated methods. Preserve that public bridge
# when R8 optimizes the release APK.
-keepclassmembers class com.gamebooster.app.core.GameBoosterJsInterface {
    @android.webkit.JavascriptInterface <methods>;
}
