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

set DEVICE_IP=10.228.69.63:5555

adb connect %DEVICE_IP%
adb -s %DEVICE_IP% install -r -t "app\build\outputs\apk\debug\app-debug.apk"
if %ERRORLEVEL% EQU 0 (
    adb -s %DEVICE_IP% shell am force-stop com.kanhaji.upastithi
    adb -s %DEVICE_IP% shell am start -n com.kanhaji.upastithi/.MainActivity
    echo ===================================================
    echo [COMPLETE] App updated and launched on %DEVICE_IP%!
    echo ===================================================
) else (
    echo [ERROR] Failed to install APK on device %DEVICE_IP%!
)
