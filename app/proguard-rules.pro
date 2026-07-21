# 学校受控浏览器 ProGuard 规则

# ==================== Kotlin 序列化 (完整保留) ====================
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# kotlinx.serialization 核心框架
-keep,includedescriptorclasses class kotlinx.serialization.**$$serializer { *; }
-keepclassmembers class kotlinx.serialization.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# 保留所有 @Serializable 注解的类及其伴生对象
-keep @kotlinx.serialization.Serializable class * {
    <init>(...);
}
-keepclassmembers @kotlinx.serialization.Serializable class * {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}

# 保留生成的 $serializer 内部类
-keep,includedescriptorclasses class **$$serializer { *; }
-keepclassmembers class ** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}

# 序列化枚举
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    <fields>;
}

# 保留 RemoteType 枚举
-keep enum com.school.browser.policy.model.RemoteType { *; }

# 保留所有数据模型类
-keep class com.school.browser.policy.model.** { *; }
-keep class com.school.browser.App { *; }

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