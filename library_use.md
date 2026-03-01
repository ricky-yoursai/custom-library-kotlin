<!-- JIT_PACK@ 使用指南说明：本文件包含 JitPack 与 RN 接入步骤。 -->
# Library 使用指南（JitPack + React Native）

## 1. JitPack 发布准备
1. 打开 `gradle.properties`，设置以下配置：
   - `jitpack.group=com.github.<你的GitHub用户名或组织>`
   - `jitpack.version=0.1.0`（本地构建兜底，JitPack 会使用 git tag 作为版本）
   - `reactNativeVersion=<你的 RN 版本>`（与项目一致）
2. 推送代码到 GitHub 仓库，并创建 git tag（例如 `v0.1.0`）。

## 2. React Native 项目接入（Android）
### 2.1 添加 JitPack 仓库
在 RN 项目 `android/settings.gradle` 或 `android/build.gradle` 的仓库配置中添加：

```kotlin
// settings.gradle (Gradle 7+)
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

### 2.2 添加依赖
在 RN 项目 `android/app/build.gradle` 中添加依赖（示例）：

```kotlin
dependencies {
    implementation("com.github.<你的GitHub用户名或组织>:<你的仓库名>:v0.1.0")
}
```

### 2.3 注册 ReactPackage（手动）
因为通过 Maven 方式集成 AAR，需手动注册包：

```kotlin
// android/app/src/main/java/.../MainApplication.kt
import com.yoursai.library.rn.LiquidWidgetPackage

override fun getPackages(): List<ReactPackage> {
    return PackageList(this).packages.apply {
        add(LiquidWidgetPackage())
    }
}
```

## 3. RN 侧使用示例
### 3.1 LiquidTabBar
```tsx
import React from "react";
import { requireNativeComponent, View } from "react-native";

const LiquidTabBar = requireNativeComponent("LiquidTabBar");

export default function Demo() {
  return (
    <View style={{ flex: 1 }}>
      <LiquidTabBar
        style={{ height: 64 }}
        items={[
          { icon: "ic_home", title: "Home" },
          { icon: "ic_search", title: "Search" },
          { icon: "ic_user", title: "Me" },
        ]}
        selectedIndex={0}
        selectedColor="#ff3b30"
        unselectedColor="#8e8e93"
        blurRadius={12}
        dispersion={0.5}
        onTabSelected={(e: any) => {
          const index = e.nativeEvent.index;
          console.log("tab selected:", index);
        }}
      />
    </View>
  );
}
```

说明：
- `items[].icon` 为 Android 资源名，需放在 `android/app/src/main/res/drawable` 或 `mipmap`。
- `onTabSelected` 回调事件字段为 `nativeEvent.index`。

### 3.2 LiquidGlassView（可选）
```tsx
import React from "react";
import { requireNativeComponent, View } from "react-native";

const LiquidGlassView = requireNativeComponent("LiquidGlassView");

export default function GlassDemo() {
  return (
    <View style={{ flex: 1 }}>
      <View style={{ flex: 1, backgroundColor: "#1e1e1e" }} />
      <LiquidGlassView
        style={{ position: "absolute", left: 24, top: 100, width: 140, height: 80 }}
        bindToDefaultBackground={true}
        blurRadius={16}
        cornerRadius={20}
        tintColor="rgba(255,255,255,0.25)"
      />
    </View>
  );
}
```

说明：
- `bindToDefaultBackground` 会尝试绑定到同级中靠前的可见 View 作为采样背景。
