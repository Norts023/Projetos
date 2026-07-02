-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep class com.skytycoon.app.data.local.entity.** { *; }
-keep class com.google.gson.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
