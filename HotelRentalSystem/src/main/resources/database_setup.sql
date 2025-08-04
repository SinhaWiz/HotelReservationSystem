-- Oracle SQL script for Hotel Rental System
-- Run this script as the rentalplatform user

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
    name VARCHAR2(100) NOT NULL,
    user_type VARCHAR2(10) CHECK (user_type IN ('host', 'renter')) NOT NULL,
    phone_number VARCHAR2(20),
    address VARCHAR2(255),
    date_of_registration TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create Properties table
CREATE TABLE properties (
    property_id NUMBER DEFAULT properties_seq.NEXTVAL PRIMARY KEY,
    host_id NUMBER NOT NULL,
    title VARCHAR2(100) NOT NULL,
    description CLOB,
    property_type VARCHAR2(10) CHECK (property_type IN ('apartment', 'house', 'villa')) NOT NULL,
    location VARCHAR2(255) NOT NULL,
    city VARCHAR2(50) NOT NULL,
    state VARCHAR2(50),
    country VARCHAR2(50) NOT NULL,
    price_per_night NUMBER(10, 2) NOT NULL,
    bedrooms NUMBER NOT NULL,
    bathrooms NUMBER NOT NULL,
    max_guests NUMBER NOT NULL,
    amenities CLOB,
    availability_status NUMBER(1) DEFAULT 1 CHECK (availability_status IN (0, 1)),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_properties_host FOREIGN KEY (host_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Create Bookings table
CREATE TABLE bookings (
    booking_id NUMBER DEFAULT bookings_seq.NEXTVAL PRIMARY KEY,
    property_id NUMBER NOT NULL,
    renter_id NUMBER NOT NULL,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    total_price NUMBER(10, 2) NOT NULL,
    booking_status VARCHAR2(10) CHECK (booking_status IN ('confirmed', 'pending', 'cancelled')) DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_bookings_property FOREIGN KEY (property_id) REFERENCES properties(property_id) ON DELETE CASCADE,
    CONSTRAINT fk_bookings_renter FOREIGN KEY (renter_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Create Payments table
CREATE TABLE payments (
    payment_id NUMBER DEFAULT payments_seq.NEXTVAL PRIMARY KEY,
    booking_id NUMBER NOT NULL,
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    payment_method VARCHAR2(15) CHECK (payment_method IN ('credit card', 'PayPal', 'bank transfer')) NOT NULL,
    amount NUMBER(10, 2) NOT NULL,
    CONSTRAINT fk_payments_booking FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE CASCADE
);

-- Create Reviews table
CREATE TABLE reviews (
    review_id NUMBER DEFAULT reviews_seq.NEXTVAL PRIMARY KEY,
    property_id NUMBER NOT NULL,
    user_id NUMBER NOT NULL,
    rating NUMBER CHECK (rating BETWEEN 1 AND 5) NOT NULL,
    comment CLOB,
    date_posted TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reviews_property FOREIGN KEY (property_id) REFERENCES properties(property_id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
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

-- Insert sample data
INSERT INTO users (username, password, email, name, user_type, phone_number, address) VALUES
('john_host', 'password123', 'john@example.com', 'John Smith', 'host', '555-0101', '123 Main St, City'),
('jane_renter', 'password123', 'jane@example.com', 'Jane Doe', 'renter', '555-0102', '456 Oak Ave, Town'),
('mike_host', 'password123', 'mike@example.com', 'Mike Johnson', 'host', '555-0103', '789 Pine Rd, Village');

INSERT INTO properties (host_id, title, description, property_type, location, city, state, country, price_per_night, bedrooms, bathrooms, max_guests, amenities, availability_status) VALUES
(1, 'Cozy Apartment', 'Beautiful apartment in the heart of the city', 'apartment', 'Downtown Area', 'New York', 'NY', 'USA', 150.00, 2, 1, 4, 'WiFi, Kitchen, TV', 1),
(1, 'Luxury Villa', 'Spacious villa with ocean view', 'villa', 'Beachfront', 'Miami', 'FL', 'USA', 300.00, 4, 3, 8, 'Pool, WiFi, Kitchen, TV, Parking', 1),
(3, 'Family House', 'Perfect for families', 'house', 'Suburban Area', 'Chicago', 'IL', 'USA', 200.00, 3, 2, 6, 'WiFi, Kitchen, TV, Garden', 1);

INSERT INTO bookings (property_id, renter_id, check_in_date, check_out_date, total_price, booking_status) VALUES
(1, 2, DATE '2024-01-15', DATE '2024-01-20', 750.00, 'confirmed'),
(2, 2, DATE '2024-02-01', DATE '2024-02-05', 1200.00, 'pending');

INSERT INTO payments (booking_id, payment_date, payment_method, amount) VALUES
(1, TIMESTAMP '2024-01-10 10:00:00', 'credit card', 750.00);

INSERT INTO reviews (property_id, user_id, rating, comment) VALUES
(1, 2, 5, 'Excellent stay! The apartment was clean and well-located.'),
(2, 2, 4, 'Great villa with amazing views. Highly recommended!'); 