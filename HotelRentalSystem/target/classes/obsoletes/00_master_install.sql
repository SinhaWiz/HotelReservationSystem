-- Master Installation Script for Hotel Management System
-- This script will execute all database setup files in the correct order

-- Start transaction
SET AUTOCOMMIT OFF;

PROMPT Installing Hotel Management System Database...
PROMPT ================================================

PROMPT 1. Creating tables and sequences...
@@01_create_tables.sql

PROMPT 2. Creating stored procedures...
@@02_procedures.sql

PROMPT 3. Creating functions and triggers...
@@03_triggers_and_functions.sql

PROMPT 4. Inserting sample data...
@@04_sample_data.sql

PROMPT Installation completed successfully!
PROMPT
PROMPT Database Statistics:
PROMPT - Room Types: 8
PROMPT - Rooms: 50 (across 5 floors)
PROMPT - Customers: 25+
PROMPT - VIP Members: Based on customer spending
PROMPT - Room Services: 10
PROMPT - Sample Bookings: Historical, Current, and Future
PROMPT
PROMPT You can now start the Hotel Management Application.

COMMIT;
SET AUTOCOMMIT ON;
