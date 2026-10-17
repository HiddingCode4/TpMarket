@echo off
cd /d "%~dp0"
call gradlew.bat build
if errorlevel 1 (echo Compilation echouee. & pause & exit /b 1)
echo Mod disponible dans build\libs
explorer build\libs
pause
