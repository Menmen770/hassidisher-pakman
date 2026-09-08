@echo off
setlocal EnableDelayedExpansion
cd /d "%~dp0"

echo === Building Hassidisher Pakman release ===

if exist dist rmdir /s /q dist
mkdir dist\input
mkdir dist\stage

REM Compile
if exist bin\com rmdir /s /q bin\com
set JAVA_FILES=
for /r src %%f in (*.java) do set JAVA_FILES=!JAVA_FILES! "%%f"
set CP=bin
for %%j in (lib\*.jar) do set CP=!CP!;%%j
javac -encoding UTF-8 -cp "!CP!" -d bin !JAVA_FILES!
if errorlevel 1 (
  echo Compile failed.
  exit /b 1
)

REM Main game jar
jar --create --file dist\input\hassidisher-pakman.jar -C bin .

REM Sound libs next to main jar (jpackage puts all input files in app/)
copy /y lib\*.jar dist\input\ >nul

REM Manifest classpath jar launcher is awkward with jpackage; use module-path style via -cp in java-options
REM Build a thin launcher jar with Main-Class and Class-Path
> dist\input\manifest.mf echo Main-Class: com.hasidicmaze.App
>> dist\input\manifest.mf echo Class-Path: jl1.0.1.jar mp3spi-1.9.5.4.jar tritonus-share-0.3.7.4.jar
>> dist\input\manifest.mf echo.
jar --create --file dist\input\hassidisher-pakman.jar --manifest dist\input\manifest.mf -C bin .
del dist\input\manifest.mf

echo Running jpackage...
jpackage ^
  --type app-image ^
  --name "Hassidisher Pakman" ^
  --app-version 2.0.0 ^
  --input dist\input ^
  --main-jar hassidisher-pakman.jar ^
  --main-class com.hasidicmaze.App ^
  --icon "Hassidisher Pakman.ico" ^
  --dest dist\stage ^
  --java-options "-Dapp.home=$ROOTDIR" ^
  --java-options "-Dfile.encoding=UTF-8"

if errorlevel 1 (
  echo jpackage failed.
  exit /b 1
)

REM Assets + scores next to the .exe
xcopy /e /i /y assets "dist\stage\Hassidisher Pakman\assets\" >nul
if not exist "dist\stage\Hassidisher Pakman\data" mkdir "dist\stage\Hassidisher Pakman\data"
copy /y data\scores.txt "dist\stage\Hassidisher Pakman\data\scores.txt" >nul
if exist "docs\קרא-לפני-הפעלה.txt" copy /y "docs\קרא-לפני-הפעלה.txt" "dist\stage\Hassidisher Pakman\קרא-לפני-הפעלה.txt" >nul

REM Zip the whole folder (not loose files) so Extract All creates one clear directory
powershell -NoProfile -Command "Compress-Archive -Path 'dist\stage\Hassidisher Pakman' -DestinationPath 'dist\Hassidisher-Pakman-Windows.zip' -Force"

echo.
echo DONE.
echo Play folder: dist\stage\Hassidisher Pakman\
echo Download ZIP: dist\Hassidisher-Pakman-Windows.zip
echo Open the folder and double-click "Hassidisher Pakman.exe"
exit /b 0
