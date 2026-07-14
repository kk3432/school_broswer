# 学校受控浏览器 ProGuard 规则

# 保留 Kotlin 序列化
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.school.browser.policy.model.**$$serializer { *; }
-keepclassmembers class com.school.browser.policy.model.** {
    *** Companion;
}
-keepclasseswithmembers class com.school.browser.policy.model.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }

# jcifs-ng (SMB)
-dontwarn jcifs.**
-keep class jcifs.** { *; }
-keep class eu.agno3.jcifs.** { *; }

# BCrypt
-keep class at.favre.lib.crypto.bcrypt.** { *; }

# ZXing
-keep class com.google.zxing.** { *; }

# AndroidX Security
-keep class androidx.security.crypto.** { *; }

# 保留 WebView 相关
-keep class android.webkit.** { *; }

# 保留我们的数据模型（反射使用）
-keep class com.school.browser.policy.model.** { *; }
-keep class com.school.browser.util.** { *; }