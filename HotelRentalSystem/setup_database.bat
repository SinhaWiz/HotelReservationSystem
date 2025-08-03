@echo off
echo Setting up Oracle database for Hotel Rental System...
echo Please make sure Oracle is installed and SQL*Plus is available
echo.

REM Connect to Oracle as SYSTEM and run the complete setup script
sqlplus system/oracle@//localhost:1521/XE @src/main/resources/oracle_setup.sql

if %ERRORLEVEL% EQU 0 (
    echo Database setup completed successfully!
    echo.
    echo Sample users created:
    echo - Admin: admin/admin123
    echo - Owner: owner1/owner123
    echo - Customer: customer1/customer123
) else (
    echo Failed to set up database. Please check your Oracle installation and credentials.
    echo Make sure Oracle is running and the SYSTEM password is correct.
    echo.
    echo Alternative setup method:
    echo 1. Connect to Oracle as SYSTEM: sqlplus system/oracle@//localhost:1521/XE
    echo 2. Run the setup script manually: @src/main/resources/oracle_setup.sql
) 