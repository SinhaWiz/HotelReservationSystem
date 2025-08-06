@echo off
echo Downloading Oracle JDBC Driver...
echo.

REM Create lib directory if it doesn't exist
if not exist "lib" mkdir lib

REM Download Oracle JDBC Driver (ojdbc8.jar)
echo Downloading Oracle JDBC Driver (ojdbc8-21.9.0.0.jar)...
powershell -Command "& {Invoke-WebRequest -Uri 'https://download.oracle.com/otn-pub/otn_software/jdbc/ojdbc8.jar' -OutFile 'lib\ojdbc8-21.9.0.0.jar'}"

if %ERRORLEVEL% EQU 0 (
    echo Oracle JDBC Driver downloaded successfully!
    echo.
    echo You can now:
    echo 1. Run build.bat to compile the project
    echo 2. Run run_simple.bat to start the application
) else (
    echo Failed to download Oracle JDBC Driver.
    echo.
    echo Manual download instructions:
    echo 1. Go to: https://www.oracle.com/database/technologies/jdbc-drivers-12c-downloads.html
    echo 2. Download ojdbc8.jar
    echo 3. Rename it to ojdbc8-21.9.0.0.jar
    echo 4. Place it in the lib folder
)

pause 