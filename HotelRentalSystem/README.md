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

- Java 11 or higher
- Oracle Database 19c or higher (or Oracle Express Edition)
- Maven (for building the project)

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

### Using Maven
```bash
mvn clean package
java -cp "target/HotelRentalSystem-1.0-SNAPSHOT-jar-with-dependencies.jar;lib/ojdbc8-21.9.0.0.jar" com.hotel.HotelRentalSystem
```

### Using Batch Files
```bash
run.bat
```

### Testing Database Connection
```bash
test_connection.bat
```

## Troubleshooting

### Oracle Driver Not Found
If you encounter the "Oracle Driver not found" error:

1. Make sure the Oracle JDBC driver JAR file is in the `lib` directory
2. The driver class name should be: `oracle.jdbc.driver.OracleDriver`
3. Check that the Oracle JDBC driver version is compatible with your Oracle database version

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

## License

This project is for educational purposes. 