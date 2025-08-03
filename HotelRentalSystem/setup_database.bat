@echo off
echo Setting up Oracle database for Hotel Rental System...
echo Please make sure Oracle is installed and SQL*Plus is available
echo.

REM Connect to Oracle and run the setup script
sqlplus rentalplatform/rentalplatform123@//localhost:1521/XE @src/main/resources/database_setup.sql

if %ERRORLEVEL% EQU 0 (
    echo Database setup completed successfully!
) else (
    echo Failed to set up database. Please check your Oracle installation and credentials.
    echo Make sure the rentalplatform user exists and has proper permissions.
) 