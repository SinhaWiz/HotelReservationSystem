@echo off
echo Testing database connection...
java -cp "lib/mysql-connector-j-8.0.33.jar;src/main/java" com.hotel.util.TestDatabaseConnection
pause 