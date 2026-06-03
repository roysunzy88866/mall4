// 顶层构建脚本:插件版本集中声明。
// 版本基线沿用本机已验证可用的组合(Gradle 8.7 + AGP 8.5.2 + Kotlin 2.0.20)。
plugins {
    id("com.android.application") version "8.5.2" apply false
    id("org.jetbrains.kotlin.android") version "2.0.20" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.20" apply false
}
