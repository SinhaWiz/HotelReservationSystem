@echo off
echo Starting Hotel Rental System (Simple Mode)...
echo.

REM Check if classes are compiled
if not exist "target\classes\com\hotel\HotelRentalSystem.class" (
    echo Classes not found. Please run build.bat first.
    pause
    exit /b 1
)

REM Run the application with compiled classes and Oracle JDBC driver
java -cp "target\classes;lib\ojdbc8-21.9.0.0.jar" com.hotel.HotelRentalSystem

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Application failed to start. Please check:
    echo 1. Java is installed and in PATH
    echo 2. Oracle JDBC driver is in lib folder
    echo 3. Database connection is configured correctly
)

pause 