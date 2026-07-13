@echo off
cd /d "%~dp0"
javac -encoding UTF-8 -d bin src/com/hasidicmaze/*.java src/com/hasidicmaze/*/*.java
if errorlevel 1 (
  echo Compile failed.
  pause
  exit /b 1
)
start "" javaw -cp bin com.hasidicmaze.App
exit /b 0
