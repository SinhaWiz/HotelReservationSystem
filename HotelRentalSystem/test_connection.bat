@echo off
echo Testing Oracle database connection...
java -cp "lib/ojdbc8-21.9.0.0.jar;src/main/java" com.hotel.util.TestDatabaseConnection
pause 