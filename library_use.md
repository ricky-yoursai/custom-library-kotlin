<!-- JIT_PACK@ 使用指南说明：本文件包含 JitPack 与 RN 接入步骤。 -->
# Library 使用指南（JitPack + React Native）

## 1. JitPack 发布准备
1. 打开 `gradle.properties`，设置以下配置：
   - `jitpack.group=com.github.ricky-yoursai`
   - `jitpack.version=1.0.0`（本地构建兜底，JitPack 会使用 git tag 作为版本）
   - `reactNativeVersion=0.76.0`（与项目一致）
2. 推送代码到 GitHub 仓库，并创建 git tag（例如 `v1.0.0`）。

## 2. React Native 项目接入（Android）
### 2.1 添加 JitPack 仓库
在 RN 项目 `android/settings.gradle` 或 `android/build.gradle` 的仓库配置中添加：

```kotlin
// settings.gradle (Gradle 7+)
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") } // 必须添加
    }
}
```

### 2.2 添加依赖
在 RN 项目 `android/app/build.gradle` 中添加依赖：

```kotlin
dependencies {
    // 注意：请确保仓库名与 GitHub 上的项目名一致
    implementation("com.github.ricky-yoursai:custom-library-kotlin:v1.0.0")
}
```

#### 2.2.1 同步与下载
添加完依赖后，必须执行以下操作以确保包被正确下载：
1. **点击 Android Studio 顶部的 "Sync Project with Gradle Files" 按钮（象鼻图标）。**
2. 或者在终端执行：
   - **macOS / Linux**:
     ```bash
     cd android && ./gradlew assembleDebug
     ```
   - **Windows**:
     ```cmd
     cd android && gradlew.bat assembleDebug
     ```
   这会强制 Gradle 去 JitPack 下载 AAR 包。如果下载失败，请检查 [JitPack 编译日志](https://jitpack.io/com/github/ricky-yoursai/custom-library-kotlin)。

### 2.3 注册 ReactPackage（手动）
一旦同步成功，你就可以在代码中引用 `LiquidWidgetPackage` 了：

```kotlin
// android/app/src/main/java/.../MainApplication.kt
import com.yoursai.library.rn.LiquidWidgetPackage

override fun getPackages(): List<ReactPackage> {
    return PackageList(this).packages.apply {
        add(LiquidWidgetPackage()) // 手动添加
    }
}
```

## 3. RN 侧使用示例
... (后续代码保持不变)
