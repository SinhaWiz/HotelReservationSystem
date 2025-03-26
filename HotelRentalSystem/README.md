# Hotel Rental System

A Java Swing application for managing hotel rentals, with MySQL database integration.

## Features

- **Property Management**: Add, edit, delete, and view properties
- **Booking Management**: Create, modify, and cancel bookings
- **User Management**: Manage hosts and renters
- **Reports**: Generate various reports including booking summaries, revenue by property, top-rated properties, etc.
- **Data Export**: Export reports to CSV files

## Prerequisites

- Java 11 or higher
- MySQL 8.0 or higher

## Quick Setup

1. **Set up the database**:
   - Run the `setup_database.bat` script to create the database and tables
   - Enter your MySQL root password when prompted

2. **Test the database connection**:
   - Run the `test_connection.bat` script to verify that the application can connect to the database

3. **Run the application**:
   - Run the `run.bat` script to start the application

## Manual Setup

### Database Setup

1. Install MySQL if you haven't already
2. Create a database using the provided SQL script in `src/main/resources/database_setup.sql`
3. You can run the script manually using the MySQL command line:
   ```
   mysql -u root -p < src/main/resources/database_setup.sql
   ```

### Configuration

The database connection settings are in `src/main/java/com/hotel/util/DatabaseConnection.java`:

```java
private static final String URL = "jdbc:mysql://localhost:3306/rentalplatform";
private static final String USER = "root";
private static final String PASSWORD = "your_mysql_password"; // Update this with your password
```

Update the `PASSWORD` field with your MySQL root password.

### Running the Application

To run the application, use the provided batch file:
```
run.bat
```

Or run it manually:
```
java -cp "target/HotelRentalSystem-1.0-SNAPSHOT-jar-with-dependencies.jar;lib/mysql-connector-j-8.0.33.jar" com.hotel.HotelRentalSystem
```

## Troubleshooting

### MySQL Driver Not Found

If you encounter the "MySQL Driver not found" error:

1. Make sure the MySQL Connector JAR file is in the `lib` directory
2. Verify that the JAR file is included in the classpath when running the application
3. Try both driver class names:
   - `com.mysql.jdbc.Driver` (older versions)
   - `com.mysql.cj.jdbc.Driver` (newer versions)

The application is configured to try both driver class names automatically.

### Database Connection Issues

If you have issues connecting to the database:

1. Verify that MySQL is running
2. Check that the database name, username, and password are correct in `DatabaseConnection.java`
3. Ensure the database and tables exist by running the setup script
4. Run the test connection script to diagnose the issue

## Project Structure

- `src/main/java/com/hotel/model/` - Model classes representing database entities
- `src/main/java/com/hotel/dao/` - Data Access Objects for database operations
- `src/main/java/com/hotel/util/` - Utility classes
- `src/main/java/com/hotel/view/` - UI components
  - `src/main/java/com/hotel/view/panels/` - Panel components for different sections
  - `src/main/java/com/hotel/view/dialogs/` - Dialog components for user interactions
- `src/main/java/com/hotel/HotelRentalSystem.java` - Main application class

## Login Credentials

The database setup script creates the following sample users:

- **Admin**: 
  - Username: admin
  - Password: admin123

- **Property Owner**:
  - Username: owner1
  - Password: owner123

- **Customer**:
  - Username: customer1
  - Password: customer123

## License

This project is licensed under the MIT License - see the LICENSE file for details. 