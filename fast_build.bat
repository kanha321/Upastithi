@echo off
setlocal enabledelayedexpansion

echo ===================================================
echo [FAST DEBUG BUILD] Building Debug APK...
echo ===================================================

call .\gradlew.bat :app:assembleDebug --daemon --parallel --build-cache
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ===================================================
    echo [BUILD FAILED] Exit Code %ERRORLEVEL%!
    echo Install and Launch skipped.
    echo ===================================================
    exit /b %ERRORLEVEL%
)

echo.
echo ===================================================
echo [BUILD SUCCESS] Deploying Debug APK to device...
echo ===================================================

adb install -r -t "app\build\outputs\apk\debug\app-debug.apk"
if %ERRORLEVEL% EQU 0 (
    adb shell am force-stop com.kanhaji.upasthiti
    adb shell am start -n com.kanhaji.upasthiti/.MainActivity
    echo ===================================================
    echo [COMPLETE] App updated and launched!
    echo ===================================================
) else (
    echo [ERROR] Failed to install APK on device!
)
