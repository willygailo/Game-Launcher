# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ComponentAwareGeneratorHolder
-keepclassmembers,allowobfuscation class * {
  @dagger.hilt.android.internal.lifecycle.HiltViewModelFactory.* <fields>;
}

# TensorFlow Lite
-keep class org.tensorflow.** { *; }
-dontwarn org.tensorflow.**
-keep class * extends org.tensorflow.lite.TensorFlowLite
-keepclassmembers class * {
  @org.tensorflow.lite.support.annotation.TensorInfo <fields>;
}

# Kotlin
-keep class kotlin.Metadata { *; }
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# Keep model classes
-keep class com.gamelauncher.data.model.** { *; }
-keep class com.gamelauncher.data.local.** { *; }

# Keep services
-keep class com.gamelauncher.services.** { *; }
-keep class com.gamelauncher.core.** { *; }

# Keep receivers
-keep class com.gamelauncher.receivers.** { *; }

# Keep widgets
-keep class com.gamelauncher.widgets.** { *; }

# Keep Hilt generated classes
-keep class * extends *GeneratedInjector
-keep class * implements *GeneratedInjector
-keep class * extends *EntryPointAccessors
