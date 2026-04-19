@echo off
echo Dang kiem tra emulator...
"%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" devices

echo.
echo Dang cai dat app...
"%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" install -r "app\build\outputs\apk\debug\app-debug.apk"

echo.
echo Hoan thanh!
pause
