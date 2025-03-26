@echo off
echo Starting Hotel Rental System...

REM Run with both the compiled classes and the MySQL Connector JAR in the classpath
java -cp "src/main/java;lib/mysql-connector-j-8.0.33.jar" com.hotel.HotelRentalSystem

pause 