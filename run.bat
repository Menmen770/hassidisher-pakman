@echo off
cd /d "%~dp0"
REM Always refresh compiled classes so map edits show up
if exist bin\com rmdir /s /q bin\com
if exist bin\src rmdir /s /q bin\src
javac -encoding UTF-8 -d bin src/com/hasidicmaze/*.java src/com/hasidicmaze/*/*.java
if errorlevel 1 (
  echo Compile failed.
  pause
  exit /b 1
)
start "" javaw -cp bin com.hasidicmaze.App
exit /b 0
