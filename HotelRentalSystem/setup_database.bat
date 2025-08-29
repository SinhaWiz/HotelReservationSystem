@echo off
echo Hotel Management DB setup (using 00_master_install.sql)
echo.
echo Enter application schema username (e.g. CATWOMAN):
set /p APP_USER=
echo Enter password for %APP_USER%:
set /p APP_PASS=
echo Enter connect descriptor (default //localhost:1521/orcl3):
set /p APP_TNS=
if "%APP_TNS%"=="" set APP_TNS=//localhost:1521/orcl3
echo.
echo Running master install as %APP_USER%@%APP_TNS% ...
sqlplus -L %APP_USER%/%APP_PASS%@%APP_TNS% @src/main/resources/00_master_install.sql
if %ERRORLEVEL% NEQ 0 (
  echo Master install failed. Check credentials / Oracle status.
  exit /b 1
)
echo Done.
exit /b 0
