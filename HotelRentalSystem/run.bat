@echo off
echo Starting Hotel Rental System...
echo.

REM Run with both the compiled classes and the Oracle JDBC driver JAR in the classpath
java -cp "src/main/java;lib/ojdbc8-21.9.0.0.jar" com.hotel.HotelRentalSystem

pause 