-- Oracle Database Setup Script for Hotel Rental System
-- Run this script as SYSTEM or SYS user

-- Create the application user
CREATE USER rentalplatform IDENTIFIED BY rentalplatform123;

-- Grant necessary privileges
GRANT CONNECT, RESOURCE TO rentalplatform;
GRANT CREATE VIEW, CREATE SEQUENCE TO rentalplatform;
GRANT UNLIMITED TABLESPACE TO rentalplatform;

-- Connect as the application user
CONNECT rentalplatform/rentalplatform123@//localhost:1521/XE

-- Create sequences for auto-incrementing IDs
CREATE SEQUENCE users_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE properties_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE bookings_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE payments_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE reviews_seq START WITH 1 INCREMENT BY 1;

-- Create Users table
CREATE TABLE users (
    user_id NUMBER DEFAULT users_seq.NEXTVAL PRIMARY KEY,
    username VARCHAR2(50) NOT NULL UNIQUE,
    password VARCHAR2(100) NOT NULL,
    email VARCHAR2(100) NOT NULL UNIQUE,
    full_name VARCHAR2(100) NOT NULL,
    user_type VARCHAR2(10) CHECK (user_type IN ('ADMIN', 'OWNER', 'CUSTOMER')) NOT NULL,
    phone VARCHAR2(20),
    address VARCHAR2(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create Properties table
CREATE TABLE properties (
    property_id NUMBER DEFAULT properties_seq.NEXTVAL PRIMARY KEY,
    owner_id NUMBER NOT NULL,
    title VARCHAR2(100) NOT NULL,
    description CLOB,
    property_type VARCHAR2(10) CHECK (property_type IN ('HOTEL', 'APARTMENT', 'VILLA', 'RESORT')) NOT NULL,
    address VARCHAR2(255) NOT NULL,
    city VARCHAR2(50) NOT NULL,
    state VARCHAR2(50),
    country VARCHAR2(50) NOT NULL,
    price_per_night NUMBER(10, 2) NOT NULL,
    bedrooms NUMBER NOT NULL,
    bathrooms NUMBER NOT NULL,
    max_guests NUMBER NOT NULL,
    amenities CLOB,
    status VARCHAR2(10) CHECK (status IN ('AVAILABLE', 'BOOKED', 'MAINTENANCE', 'INACTIVE')) DEFAULT 'AVAILABLE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_properties_owner FOREIGN KEY (owner_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Create Bookings table
CREATE TABLE bookings (
    booking_id NUMBER DEFAULT bookings_seq.NEXTVAL PRIMARY KEY,
    property_id NUMBER NOT NULL,
    customer_id NUMBER NOT NULL,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    total_price NUMBER(10, 2) NOT NULL,
    num_guests NUMBER NOT NULL,
    status VARCHAR2(10) CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED')) DEFAULT 'PENDING',
    special_requests CLOB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_bookings_property FOREIGN KEY (property_id) REFERENCES properties(property_id) ON DELETE CASCADE,
    CONSTRAINT fk_bookings_customer FOREIGN KEY (customer_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Create Payments table
CREATE TABLE payments (
    payment_id NUMBER DEFAULT payments_seq.NEXTVAL PRIMARY KEY,
    booking_id NUMBER NOT NULL,
    amount NUMBER(10, 2) NOT NULL,
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    payment_method VARCHAR2(15) CHECK (payment_method IN ('CREDIT_CARD', 'DEBIT_CARD', 'PAYPAL', 'BANK_TRANSFER')) NOT NULL,
    status VARCHAR2(10) CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED')) NOT NULL,
    transaction_id VARCHAR2(100),
    CONSTRAINT fk_payments_booking FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE CASCADE
);

-- Create Reviews table
CREATE TABLE reviews (
    review_id NUMBER DEFAULT reviews_seq.NEXTVAL PRIMARY KEY,
    booking_id NUMBER NOT NULL,
    property_id NUMBER NOT NULL,
    customer_id NUMBER NOT NULL,
    rating NUMBER CHECK (rating BETWEEN 1 AND 5) NOT NULL,
    comment CLOB,
    review_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reviews_booking FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_property FOREIGN KEY (property_id) REFERENCES properties(property_id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_customer FOREIGN KEY (customer_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Create triggers for updated_at timestamps
CREATE OR REPLACE TRIGGER users_update_trigger
    BEFORE UPDATE ON users
    FOR EACH ROW
BEGIN
    :NEW.updated_at := CURRENT_TIMESTAMP;
END;
/

CREATE OR REPLACE TRIGGER properties_update_trigger
    BEFORE UPDATE ON properties
    FOR EACH ROW
BEGIN
    :NEW.updated_at := CURRENT_TIMESTAMP;
END;
/

CREATE OR REPLACE TRIGGER bookings_update_trigger
    BEFORE UPDATE ON bookings
    FOR EACH ROW
BEGIN
    :NEW.updated_at := CURRENT_TIMESTAMP;
END;
/

-- Insert admin user
INSERT INTO users (username, password, email, full_name, user_type, phone, address)
VALUES ('admin', 'admin123', 'admin@hotel.com', 'System Administrator', 'ADMIN', '123-456-7890', 'Admin Office');

-- Insert sample owner
INSERT INTO users (username, password, email, full_name, user_type, phone, address)
VALUES ('owner1', 'owner123', 'owner1@hotel.com', 'Property Owner', 'OWNER', '234-567-8901', '123 Owner St');

-- Insert sample customer
INSERT INTO users (username, password, email, full_name, user_type, phone, address)
VALUES ('customer1', 'customer123', 'customer1@example.com', 'John Customer', 'CUSTOMER', '345-678-9012', '456 Customer Ave');

-- Commit the changes
COMMIT;

-- Display confirmation
PROMPT Database setup completed successfully!
PROMPT Sample users created:
PROMPT - Admin: admin/admin123
PROMPT - Owner: owner1/owner123
PROMPT - Customer: customer1/customer123 