@echo off
echo Building Hotel Rental System...
echo.

REM Create output directory
if not exist "target" mkdir target
if not exist "target\classes" mkdir target\classes

REM Compile Java files
echo Compiling Java source files...
javac -cp "lib\ojdbc8-21.9.0.0.jar" -d target\classes src\main\java\com\hotel\*.java src\main\java\com\hotel\dao\*.java src\main\java\com\hotel\model\*.java src\main\java\com\hotel\util\*.java src\main\java\com\hotel\view\*.java

if %ERRORLEVEL% EQU 0 (
    echo Compilation successful!
    echo.
    echo To run the application, use: run.bat
) else (
    echo Compilation failed! Please check your Java installation.
    echo Make sure Java is installed and added to your system PATH.
)

pause 