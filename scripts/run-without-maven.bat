@echo off
REM Compile and run the project with the plain JDK (no Maven needed) - Windows version.
REM Usage: scripts\run-without-maven.bat [mode] [--xml file] [--xsd file]
cd /d "%~dp0\.."

echo [1/3] Compiling...
if not exist target\classes mkdir target\classes
dir /s /b src\main\java\*.java > target\sources.txt
javac -encoding UTF-8 -d target\classes @target\sources.txt || exit /b 1

echo [2/3] Copying resources...
xcopy /s /e /y /q src\main\resources\* target\classes\ >nul

echo [3/3] Running...
java -cp target\classes com.fss.soa.tp1.Main %*
