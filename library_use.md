<!-- JIT_PACK@ 使用指南说明：本文件包含 JitPack 与 RN 接入步骤。 -->
# Library 使用指南（JitPack + React Native / Expo）

## 1. JitPack 发布准备
1. 打开 `gradle.properties`，设置以下配置：
   - `jitpack.group=com.github.ricky-yoursai`
   - `jitpack.version=1.0.1`（建议打新 Tag）
   - `reactNativeVersion=0.76.0`（与项目一致）
2. 推送代码到 GitHub 仓库，并创建 git tag（例如 `v1.0.1`）。

## 2. React Native / Expo 项目接入 (Android)
### 2.1 添加 JitPack 仓库
在项目 `android/settings.gradle` 中添加：
```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

### 2.2 添加依赖
在 `android/app/build.gradle` 中添加：
```kotlin
dependencies {
    implementation("com.github.ricky-yoursai.custom-library-kotlin:library:v1.0.1")
}
```

## 3. Expo 项目特别说明 (新架构支持)
本库已升级支持 **Expo Modules API**，在最新的 Expo 项目中：

### 3.1 无需手动注册
你 **不需要** 在 `MainApplication.kt` 中手动添加 `LiquidWidgetPackage`。Expo 会通过 `expo-module.config.json` 自动完成链接。

### 3.2 JS 侧使用建议
虽然 `requireNativeComponent` 仍然可用，但在 Expo 模块中，建议使用统一的导出方式：

```tsx
// components/LiquidTabBar.tsx
import { requireNativeViewManager } from 'expo-modules-core';
import React from 'react';

// 使用 Expo 提供的视图管理器
const NativeView = requireNativeViewManager('LiquidTabBar');

export default function LiquidTabBar(props: any) {
  return <NativeView {...props} />;
}
```

### 3.3 示例代码
```tsx
import LiquidTabBar from './components/LiquidTabBar';

export default function App() {
  return (
    <LiquidTabBar
      style={{ height: 64, width: '100%' }}
      items={[
        { icon: 'ic_home', title: 'Home' },
        { icon: 'ic_search', title: 'Search' }
      ]}
      selectedIndex={0}
      selectedColor="#ff3b30"
      onTabSelected={({ nativeEvent }: any) => {
        console.log('Selected:', nativeEvent.index);
      }}
    />
  );
}
```

## 4. 常见问题
- **View config not found**: 请确保已经执行了 `npx expo prebuild` 并且 Android 项目成功同步。
- **Tried to register two views**: 请检查是否在 `MainApplication.kt` 中重复手动注册了该包，Expo 模块会自动注册。
