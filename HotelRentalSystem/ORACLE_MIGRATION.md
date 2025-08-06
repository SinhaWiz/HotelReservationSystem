# MySQL to Oracle Migration Guide

This document outlines the changes made to migrate the Hotel Rental System from MySQL to Oracle Database.

## Overview

The project has been successfully migrated from MySQL to Oracle Database. All database connections, SQL syntax, and data access patterns have been updated to work with Oracle.

## Key Changes Made

### 1. Dependencies (pom.xml)
- **Removed**: MySQL Connector JAR dependency
- **Added**: Oracle JDBC Driver (ojdbc8-21.9.0.0)

### 2. Database Connection (DatabaseConnection.java)
- **URL Format**: Changed from `jdbc:mysql://localhost:3306/rentalplatform` to `jdbc:oracle:thin:@localhost:1521:XE`
- **Driver Class**: Changed from `com.mysql.cj.jdbc.Driver` to `oracle.jdbc.driver.OracleDriver`
- **Credentials**: Updated to use Oracle user `rentalplatform` with password `rentalplatform123`

### 3. Database Schema (database_setup.sql)
- **Data Types**: 
  - `INT` → `NUMBER`
  - `VARCHAR` → `VARCHAR2`
  - `TEXT` → `CLOB`
  - `DECIMAL` → `NUMBER(10,2)`
  - `ENUM` → `VARCHAR2` with `CHECK` constraints
- **Auto-increment**: Replaced `AUTO_INCREMENT` with Oracle sequences
- **Timestamps**: Updated trigger syntax for `updated_at` fields
- **Constraints**: Added proper Oracle constraint naming

### 4. Model Classes

#### User.java
- **Fields Updated**:
  - `name` → `username` + `fullName`
  - `phoneNumber` → `phone`
  - `dateOfRegistration` → `createdAt` + `updatedAt`
  - Added `password` field
- **UserType Enum**: Updated from `HOST/RENTER` to `ADMIN/OWNER/CUSTOMER`

#### Property.java
- **Fields Updated**:
  - `hostId` → `ownerId`
  - `location` → `address`, `city`, `state`, `country`
  - `availabilityStatus` → `status` (enum)
  - Added `title`, `bedrooms`, `bathrooms`, `amenities`
- **PropertyType Enum**: Updated to include `HOTEL`, `APARTMENT`, `VILLA`, `RESORT`
- **New Status Enum**: `AVAILABLE`, `BOOKED`, `MAINTENANCE`, `INACTIVE`

### 5. Data Access Objects (DAO Classes)

#### UserDAO.java
- **Table Name**: `User` → `users`
- **Column Names**: Updated to match new schema
- **Generated Keys**: Changed from `Statement.RETURN_GENERATED_KEYS` to `new String[]{"user_id"}`
- **Methods**: Updated to use new field names and types

#### PropertyDAO.java
- **Table Name**: `Property` → `properties`
- **Column Names**: Updated to match new schema
- **Generated Keys**: Changed from `Statement.RETURN_GENERATED_KEYS` to `new String[]{"property_id"}`
- **Methods**: Updated to use new field names and types

### 6. Database Setup Scripts

#### oracle_setup.sql (New)
- Complete Oracle setup script including user creation
- Creates database user with proper permissions
- Sets up all tables, sequences, and triggers
- Inserts sample data

#### database_setup.sql (Updated)
- Converted from MySQL to Oracle syntax
- Updated data types and constraints
- Added Oracle-specific features

### 7. Batch Files

#### setup_database.bat
- **Command**: Changed from `mysql` to `sqlplus`
- **Script**: Updated to use `oracle_setup.sql`
- **Credentials**: Updated for Oracle SYSTEM user

#### test_connection.bat
- **JAR File**: Updated to use `ojdbc8-21.9.0.0.jar`
- **Driver**: Updated to test Oracle driver

#### run.bat
- **JAR File**: Updated to use `ojdbc8-21.9.0.0.jar`

### 8. Documentation (README.md)
- Updated all references from MySQL to Oracle
- Added Oracle-specific setup instructions
- Updated troubleshooting section with Oracle-specific issues
- Added Oracle database requirements

## Oracle-Specific Features Implemented

### 1. Sequences
- `users_seq`, `properties_seq`, `bookings_seq`, `payments_seq`, `reviews_seq`
- Used for auto-incrementing primary keys

### 2. Triggers
- `users_update_trigger`, `properties_update_trigger`, `bookings_update_trigger`
- Automatically update `updated_at` timestamps

### 3. Data Types
- `CLOB` for large text fields (descriptions, amenities, comments)
- `NUMBER` for numeric fields
- `VARCHAR2` with `CHECK` constraints for enums

### 4. Constraints
- Proper foreign key constraints with cascade delete
- Check constraints for enum values
- Unique constraints on username and email

## Setup Instructions

### Prerequisites
1. Oracle Database 19c or higher (or Oracle Express Edition)
2. Java 11 or higher
3. Maven (for building)

### Database Setup
1. Install Oracle Database
2. Create database user:
   ```sql
   CREATE USER rentalplatform IDENTIFIED BY rentalplatform123;
   GRANT CONNECT, RESOURCE, CREATE VIEW, CREATE SEQUENCE TO rentalplatform;
   GRANT UNLIMITED TABLESPACE TO rentalplatform;
   ```
3. Run setup script: `setup_database.bat`

### Application Setup
1. Build project: `mvn clean package`
2. Test connection: `test_connection.bat`
3. Run application: `run.bat`

## Troubleshooting

### Common Oracle Issues
- **ORA-12541**: Oracle listener not running
- **ORA-01017**: Invalid username/password
- **ORA-12514**: Service name not found in connection string

### Connection Issues
- Verify Oracle is running on port 1521
- Check service name in connection string
- Ensure database user has proper permissions

## Migration Benefits

1. **Enterprise Features**: Oracle provides advanced features like partitioning, advanced security, and high availability
2. **Performance**: Oracle's query optimizer and indexing capabilities
3. **Scalability**: Better support for large datasets and concurrent users
4. **Reliability**: ACID compliance and advanced backup/recovery features

## Notes

- All existing functionality has been preserved
- The application interface remains the same
- Sample data has been updated to match the new schema
- The migration is backward compatible in terms of application behavior 