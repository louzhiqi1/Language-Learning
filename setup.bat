@echo off
REM === English Reader App - 环境搭建脚本 ===
REM 前置条件: 已安装 JDK 17, JAVA_HOME 已设置

echo === Step 1: 下载 Android SDK 命令行工具 ===
echo 请从以下地址下载 commandlinetools-win:
echo https://developer.android.com/studio#command-line-tools-only
echo 解压到 F:\android-sdk\cmdline-tools\latest\
echo.

echo === Step 2: 设置环境变量 ===
echo 请设置以下环境变量:
echo   ANDROID_HOME=F:\android-sdk
echo   PATH 中添加: F:\android-sdk\cmdline-tools\latest\bin
echo   PATH 中添加: F:\android-sdk\platform-tools
echo.

echo === Step 3: 安装 SDK 组件 ===
echo 运行以下命令:
echo   sdkmanager "platforms;android-35" "build-tools;35.0.0" "platform-tools"
echo.

echo === Step 4: 创建 local.properties ===
echo sdk.dir=F:\\android-sdk > "%~dp0local.properties"
echo 已创建 local.properties
echo.

echo === Step 5: 下载 Gradle Wrapper ===
echo 运行: gradle wrapper --gradle-version 8.9
echo 或者手动下载 gradle-wrapper.jar 放到 gradle\wrapper\ 目录
echo.

echo === Step 6: 编译项目 ===
echo 运行: gradlew.bat assembleDebug
echo.

echo === 注意事项 ===
echo - 首次编译会下载大量依赖(约2-3GB), 需要网络
echo - native 库(llama.cpp, MNN, Piper)需要单独编译或下载预编译版本
echo - 首次编译不含 native 库时, 可以先注释掉 inference 包中的 System.loadLibrary 调用
echo   来验证 Kotlin/Compose 层编译通过
echo.
pause
