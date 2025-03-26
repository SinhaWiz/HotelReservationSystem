@echo off
echo Setting up database...
echo Please enter your MySQL root password when prompted

mysql -u root -p < src/main/resources/database_setup.sql

if %ERRORLEVEL% EQU 0 (
    echo Database setup completed successfully!
) else (
    echo Failed to set up database. Please check your MySQL installation and credentials.
)

pause 