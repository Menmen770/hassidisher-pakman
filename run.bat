@echo off
setlocal EnableDelayedExpansion
cd /d "%~dp0"
REM Always refresh compiled classes so map edits show up
if exist bin\com rmdir /s /q bin\com
if exist bin\src rmdir /s /q bin\src
set JAVA_FILES=
for /r src %%f in (*.java) do set JAVA_FILES=!JAVA_FILES! "%%f"
javac -encoding UTF-8 -d bin !JAVA_FILES!
if errorlevel 1 (
  echo Compile failed.
  pause
  exit /b 1
)
start "" javaw -cp bin com.hasidicmaze.App
exit /b 0
