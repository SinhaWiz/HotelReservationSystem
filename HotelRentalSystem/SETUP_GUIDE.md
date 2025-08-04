# Hotel Rental System - Setup Guide

This guide will help you set up and run the Hotel Rental System without requiring Maven or advanced Java knowledge.

## Prerequisites

### 1. Install Java (Required)
1. Download Java 11 or higher from: https://adoptium.net/
2. Run the installer and follow the setup wizard
3. Make sure to check "Add to PATH" during installation
4. Restart your command prompt after installation

### 2. Install Oracle Database (Required)
1. Download Oracle Database Express Edition (XE) from: https://www.oracle.com/database/technologies/xe-downloads.html
2. Install Oracle Database following the installation wizard
3. Note down the password you set for the SYSTEM user

## Quick Setup (Without Maven)

### Step 1: Download Oracle JDBC Driver
Run the provided script to download the Oracle JDBC driver:
```bash
download_oracle_driver.bat
```

If the script fails, manually download:
1. Go to: https://www.oracle.com/database/technologies/jdbc-drivers-12c-downloads.html
2. Download `ojdbc8.jar`
3. Rename it to `ojdbc8-21.9.0.0.jar`
4. Place it in the `lib` folder

### Step 2: Set Up Oracle Database
1. Open SQL*Plus as SYSTEM user:
   ```bash
   sqlplus system/your_password@//localhost:1521/XE
   ```

2. Create the application user:
   ```sql
   CREATE USER rentalplatform IDENTIFIED BY rentalplatform123;
   GRANT CONNECT, RESOURCE, CREATE VIEW, CREATE SEQUENCE TO rentalplatform;
   GRANT UNLIMITED TABLESPACE TO rentalplatform;
   EXIT;
   ```

3. Run the database setup script:
   ```bash
   setup_database.bat
   ```

### Step 3: Build and Run
1. Compile the project:
   ```bash
   build.bat
   ```

2. Test the database connection:
   ```bash
   test_connection.bat
   ```

3. Run the application:
   ```bash
   run_simple.bat
   ```

## Alternative Setup (With Maven)

If you prefer to use Maven:

### Install Maven
1. Download Maven from: https://maven.apache.org/download.cgi
2. Extract to a folder (e.g., `C:\Program Files\Apache\maven`)
3. Add Maven to your system PATH

### Build and Run with Maven
```bash
mvn clean package
java -cp "target/HotelRentalSystem-1.0-SNAPSHOT-jar-with-dependencies.jar;lib/ojdbc8-21.9.0.0.jar" com.hotel.HotelRentalSystem
```

## Troubleshooting

### Java Not Found
- **Error**: `'java' is not recognized`
- **Solution**: Install Java and add it to your system PATH

### Maven Not Found
- **Error**: `'mvn' is not recognized`
- **Solution**: Use the provided batch files instead of Maven

### Oracle Driver Not Found
- **Error**: `Oracle JDBC Driver not found`
- **Solution**: Run `download_oracle_driver.bat` or manually download the driver

### Database Connection Issues
- **Error**: `ORA-12541: TNS:no listener`
- **Solution**: Make sure Oracle Database is running
- **Error**: `ORA-01017: invalid username/password`
- **Solution**: Check the credentials in `DatabaseConnection.java`

### Compilation Errors
- **Error**: `javac is not recognized`
- **Solution**: Install Java Development Kit (JDK), not just Java Runtime Environment (JRE)

## Sample Login Credentials

After running the setup script, you can use these sample accounts:

- **Admin**: username: `admin`, password: `admin123`
- **Owner**: username: `owner1`, password: `owner123`
- **Customer**: username: `customer1`, password: `customer123`

## File Structure

```
HotelRentalSystem/
├── src/main/java/com/hotel/     # Java source code
├── lib/                         # JDBC drivers
├── target/                      # Compiled classes (created by build.bat)
├── build.bat                    # Compile without Maven
├── run_simple.bat               # Run without Maven
├── setup_database.bat           # Set up Oracle database
├── test_connection.bat          # Test database connection
└── download_oracle_driver.bat   # Download Oracle JDBC driver
```

## Support

If you encounter any issues:
1. Check that Java is properly installed: `java -version`
2. Verify Oracle Database is running
3. Ensure the Oracle JDBC driver is in the `lib` folder
4. Check the database connection settings in `DatabaseConnection.java` 