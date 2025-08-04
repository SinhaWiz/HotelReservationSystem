# Hotel Rental System

A Java Swing application for managing hotel rentals, with Oracle database integration.

## Features

- User management (Admin, Owner, Customer)
- Property listing and management
- Booking system
- Payment processing
- Review and rating system
- Search and filter functionality

## Prerequisites

- Java 11 or higher (JDK, not just JRE)
- Oracle Database 19c or higher (or Oracle Express Edition)

## Quick Start (Without Maven)

If you don't have Maven installed, follow these steps:

### 1. Install Java
Download and install Java 11+ from: https://adoptium.net/

### 2. Download Oracle JDBC Driver
```bash
download_oracle_driver.bat
```

### 3. Set Up Database
```bash
setup_database.bat
```

### 4. Build and Run
```bash
build.bat
run_simple.bat
```

For detailed setup instructions, see [SETUP_GUIDE.md](SETUP_GUIDE.md).

## Database Setup

### Oracle Database Requirements
- Oracle Database 19c or higher
- Oracle Express Edition (XE) is supported
- A database user with appropriate permissions

### Setup Instructions

1. Install Oracle Database if you haven't already
2. Create a database user for the application:
   ```sql
   CREATE USER rentalplatform IDENTIFIED BY rentalplatform123;
   GRANT CONNECT, RESOURCE, CREATE VIEW, CREATE SEQUENCE TO rentalplatform;
   GRANT UNLIMITED TABLESPACE TO rentalplatform;
   ```

3. Run the database setup script:
   ```bash
   setup_database.bat
   ```

   You can also run the script manually using SQL*Plus:
   ```bash
   sqlplus rentalplatform/rentalplatform123@//localhost:1521/XE @src/main/resources/database_setup.sql
   ```

## Configuration

### Database Connection
The database connection is configured in `src/main/java/com/hotel/util/DatabaseConnection.java`:

```java
private static final String URL = "jdbc:oracle:thin:@localhost:1521:XE";
private static final String USER = "rentalplatform";
private static final String PASSWORD = "rentalplatform123";
```

Update the `USER` and `PASSWORD` fields with your Oracle database credentials.

## Building and Running

### Option 1: Without Maven (Recommended for beginners)
```bash
build.bat
run_simple.bat
```

### Option 2: Using Maven
```bash
mvn clean package
java -cp "target/HotelRentalSystem-1.0-SNAPSHOT-jar-with-dependencies.jar;lib/ojdbc8-21.9.0.0.jar" com.hotel.HotelRentalSystem
```

### Testing Database Connection
```bash
test_connection.bat
```

## Troubleshooting

### Java Not Found
- Install Java JDK (not just JRE) from https://adoptium.net/
- Make sure Java is added to your system PATH

### Oracle Driver Not Found
If you encounter the "Oracle Driver not found" error:

1. Run `download_oracle_driver.bat` to download the driver
2. Make sure the Oracle JDBC driver JAR file is in the `lib` directory
3. The driver class name should be: `oracle.jdbc.driver.OracleDriver`

### Database Connection Issues
1. Verify that Oracle is running
2. Check that the database service is accessible on the configured port (default: 1521)
3. Ensure the database user has proper permissions
4. Verify the connection string format: `jdbc:oracle:thin:@hostname:port:service_name`

### Common Oracle Issues
1. **ORA-12541: TNS:no listener**: Oracle listener is not running
2. **ORA-01017: invalid username/password**: Check credentials
3. **ORA-12514: TNS:listener does not currently know of service**: Check service name in connection string

## Project Structure

```
src/main/java/com/hotel/
├── dao/           # Data Access Objects
├── model/         # Entity classes
├── util/          # Utility classes
├── view/          # UI components
└── HotelRentalSystem.java  # Main application class
```

## Database Schema

The application uses the following Oracle tables:
- `users` - User accounts and profiles
- `properties` - Property listings
- `bookings` - Reservation records
- `payments` - Payment transactions
- `reviews` - User reviews and ratings

## Sample Login Credentials

After setup, you can use these sample accounts:
- **Admin**: username: `admin`, password: `admin123`
- **Owner**: username: `owner1`, password: `owner123`
- **Customer**: username: `customer1`, password: `customer123`

## Migration Information

This project was migrated from MySQL to Oracle. For detailed migration information, see [ORACLE_MIGRATION.md](ORACLE_MIGRATION.md).

## License

This project is for educational purposes. 