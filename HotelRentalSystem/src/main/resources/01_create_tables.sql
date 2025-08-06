-- Hotel Management System - Database Schema
-- Oracle Database DDL Script

-- Drop tables if they exist (for clean installation)
DROP TABLE booking_archive CASCADE CONSTRAINTS;
DROP TABLE audit_log CASCADE CONSTRAINTS;
DROP TABLE bookings CASCADE CONSTRAINTS;
DROP TABLE vip_members CASCADE CONSTRAINTS;
DROP TABLE customers CASCADE CONSTRAINTS;
DROP TABLE rooms CASCADE CONSTRAINTS;
DROP TABLE room_types CASCADE CONSTRAINTS;

-- Drop sequences if they exist
DROP SEQUENCE customer_seq;
DROP SEQUENCE room_seq;
DROP SEQUENCE booking_seq;
DROP SEQUENCE vip_seq;
DROP SEQUENCE audit_seq;
DROP SEQUENCE archive_seq;

-- Create Room Types table
CREATE TABLE room_types (
    type_id NUMBER(10) PRIMARY KEY,
    type_name VARCHAR2(50) NOT NULL UNIQUE,
    base_price NUMBER(10,2) NOT NULL,
    max_occupancy NUMBER(2) NOT NULL,
    amenities VARCHAR2(500),
    created_date DATE DEFAULT SYSDATE
);

-- Create Rooms table
CREATE TABLE rooms (
    room_id NUMBER(10) PRIMARY KEY,
    room_number VARCHAR2(10) NOT NULL UNIQUE,
    type_id NUMBER(10) NOT NULL,
    floor_number NUMBER(3),
    status VARCHAR2(20) DEFAULT 'AVAILABLE' CHECK (status IN ('AVAILABLE', 'OCCUPIED', 'MAINTENANCE', 'RESERVED')),
    last_maintenance DATE,
    created_date DATE DEFAULT SYSDATE,
    CONSTRAINT fk_room_type FOREIGN KEY (type_id) REFERENCES room_types(type_id)
);

-- Create Customers table
CREATE TABLE customers (
    customer_id NUMBER(10) PRIMARY KEY,
    first_name VARCHAR2(50) NOT NULL,
    last_name VARCHAR2(50) NOT NULL,
    email VARCHAR2(100) UNIQUE,
    phone VARCHAR2(20),
    address VARCHAR2(200),
    date_of_birth DATE,
    total_spent NUMBER(12,2) DEFAULT 0,
    registration_date DATE DEFAULT SYSDATE,
    is_active CHAR(1) DEFAULT 'Y' CHECK (is_active IN ('Y', 'N')),
    loyalty_points NUMBER(10) DEFAULT 0
);

-- Create VIP Members table
CREATE TABLE vip_members (
    vip_id NUMBER(10) PRIMARY KEY,
    customer_id NUMBER(10) NOT NULL,
    membership_level VARCHAR2(20) DEFAULT 'GOLD' CHECK (membership_level IN ('GOLD', 'PLATINUM', 'DIAMOND')),
    discount_percentage NUMBER(5,2) DEFAULT 10.00,
    membership_start_date DATE DEFAULT SYSDATE,
    membership_end_date DATE,
    benefits VARCHAR2(500),
    is_active CHAR(1) DEFAULT 'Y' CHECK (is_active IN ('Y', 'N')),
    CONSTRAINT fk_vip_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
    CONSTRAINT uk_vip_customer UNIQUE (customer_id)
);

-- Create Bookings table
CREATE TABLE bookings (
    booking_id NUMBER(10) PRIMARY KEY,
    customer_id NUMBER(10) NOT NULL,
    room_id NUMBER(10) NOT NULL,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    actual_check_in DATE,
    actual_check_out DATE,
    booking_date DATE DEFAULT SYSDATE,
    total_amount NUMBER(12,2) NOT NULL,
    discount_applied NUMBER(12,2) DEFAULT 0,
    extra_charges NUMBER(12,2) DEFAULT 0,
    payment_status VARCHAR2(20) DEFAULT 'PENDING' CHECK (payment_status IN ('PENDING', 'PAID', 'CANCELLED', 'REFUNDED')),
    booking_status VARCHAR2(20) DEFAULT 'CONFIRMED' CHECK (booking_status IN ('CONFIRMED', 'CHECKED_IN', 'CHECKED_OUT', 'CANCELLED', 'NO_SHOW')),
    special_requests VARCHAR2(500),
    created_by VARCHAR2(50) DEFAULT USER,
    CONSTRAINT fk_booking_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
    CONSTRAINT fk_booking_room FOREIGN KEY (room_id) REFERENCES rooms(room_id),
    CONSTRAINT chk_dates CHECK (check_out_date > check_in_date)
);

-- Create Booking Archive table (for data older than 1 month)
CREATE TABLE booking_archive (
    archive_id NUMBER(10) PRIMARY KEY,
    booking_id NUMBER(10),
    customer_id NUMBER(10),
    room_id NUMBER(10),
    check_in_date DATE,
    check_out_date DATE,
    actual_check_in DATE,
    actual_check_out DATE,
    booking_date DATE,
    total_amount NUMBER(12,2),
    discount_applied NUMBER(12,2),
    extra_charges NUMBER(12,2),
    payment_status VARCHAR2(20),
    booking_status VARCHAR2(20),
    special_requests VARCHAR2(500),
    archived_date DATE DEFAULT SYSDATE,
    archived_by VARCHAR2(50) DEFAULT USER
);

-- Create Audit Log table
CREATE TABLE audit_log (
    log_id NUMBER(10) PRIMARY KEY,
    table_name VARCHAR2(50) NOT NULL,
    operation VARCHAR2(10) NOT NULL CHECK (operation IN ('INSERT', 'UPDATE', 'DELETE')),
    record_id NUMBER(10),
    old_values CLOB,
    new_values CLOB,
    changed_by VARCHAR2(50) DEFAULT USER,
    change_date DATE DEFAULT SYSDATE,
    ip_address VARCHAR2(45),
    session_id VARCHAR2(100)
);

-- Create sequences for primary keys
CREATE SEQUENCE customer_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE room_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE booking_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE vip_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE audit_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE archive_seq START WITH 1 INCREMENT BY 1 NOCACHE;

-- Create indexes for performance optimization
CREATE INDEX idx_customer_email ON customers(email);
CREATE INDEX idx_customer_phone ON customers(phone);
CREATE INDEX idx_customer_total_spent ON customers(total_spent);
CREATE INDEX idx_room_status ON rooms(status);
CREATE INDEX idx_room_type ON rooms(type_id);
CREATE INDEX idx_booking_dates ON bookings(check_in_date, check_out_date);
CREATE INDEX idx_booking_customer ON bookings(customer_id);
CREATE INDEX idx_booking_room ON bookings(room_id);
CREATE INDEX idx_booking_status ON bookings(booking_status);
CREATE INDEX idx_vip_customer ON vip_members(customer_id);
CREATE INDEX idx_vip_level ON vip_members(membership_level);
CREATE INDEX idx_audit_table ON audit_log(table_name);
CREATE INDEX idx_audit_date ON audit_log(change_date);

COMMIT;

