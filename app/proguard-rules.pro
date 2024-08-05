# AppCompat
-keep class androidx.appcompat.** { *; }
-dontwarn androidx.appcompat.**

# Material Components
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**

# JUnit
-keep class org.junit.** { *; }
-dontwarn org.junit.**

# AndroidX Test (JUnit extension)
-keep class androidx.test.ext.junit.** { *; }
-dontwarn androidx.test.ext.junit.**

# Espresso Core
-keep class androidx.test.espresso.** { *; }
-dontwarn androidx.test.espresso.**

# Keep annotations
-keepattributes *Annotation*

# Keep class names for reflection
-keepnames class androidx.appcompat.** { *; }
-keepnames class com.google.android.material.** { *; }
-keepnames class org.junit.** { *; }
-keepnames class androidx.test.ext.junit.** { *; }
-keepnames class androidx.test.espresso.** { *; }

# If you are using any Parcelable classes
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
