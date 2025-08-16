-- Hotel Management System - Sample Data
-- Oracle Database Sample Data Insertion Script

-- Insert Room Types
INSERT INTO room_types (type_id, type_name, base_price, max_occupancy, amenities, description) VALUES 
(1, 'Standard Single', 100.00, 1, 'WiFi, TV, Air Conditioning, Private Bathroom', 'Comfortable single room with essential amenities');

INSERT INTO room_types (type_id, type_name, base_price, max_occupancy, amenities, description) VALUES 
(2, 'Standard Double', 150.00, 2, 'WiFi, TV, Air Conditioning, Private Bathroom, Mini Fridge', 'Spacious double room perfect for couples or business travelers');

INSERT INTO room_types (type_id, type_name, base_price, max_occupancy, amenities, description) VALUES 
(3, 'Deluxe Suite', 300.00, 4, 'WiFi, TV, Air Conditioning, Private Bathroom, Mini Fridge, Balcony, Room Service', 'Luxurious suite with premium amenities and balcony view');

INSERT INTO room_types (type_id, type_name, base_price, max_occupancy, amenities, description) VALUES 
(4, 'Presidential Suite', 500.00, 6, 'WiFi, TV, Air Conditioning, Private Bathroom, Mini Fridge, Balcony, Room Service, Jacuzzi, Butler Service', 'Ultimate luxury experience with butler service and jacuzzi');

INSERT INTO room_types (type_id, type_name, base_price, max_occupancy, amenities, description) VALUES 
(5, 'Economy Room', 75.00, 1, 'WiFi, TV, Air Conditioning, Shared Bathroom', 'Budget-friendly room with shared facilities');

-- Insert Rooms
-- Floor 1 - Standard Rooms
INSERT INTO rooms (room_id, room_number, type_id, floor_number, amenities, notes, description, base_price) VALUES 
(room_seq.NEXTVAL, '101', 1, 1, 'WiFi, TV, Air Conditioning', 'Recently renovated', 'Standard single room on first floor', 100.00);
INSERT INTO rooms (room_id, room_number, type_id, floor_number, amenities, notes, description, base_price) VALUES 
(room_seq.NEXTVAL, '102', 2, 1, 'WiFi, TV, Air Conditioning, Mini Fridge', 'Quiet room', 'Standard double room on first floor', 150.00);
INSERT INTO rooms (room_id, room_number, type_id, floor_number, amenities, notes, description, base_price) VALUES 
(room_seq.NEXTVAL, '103', 1, 1, 'WiFi, TV, Air Conditioning', 'Corner room', 'Standard single room on first floor', 100.00);
INSERT INTO rooms (room_id, room_number, type_id, floor_number, amenities, notes, description, base_price) VALUES 
(room_seq.NEXTVAL, '104', 2, 1, 'WiFi, TV, Air Conditioning, Mini Fridge', 'Near elevator', 'Standard double room on first floor', 150.00);
INSERT INTO rooms (room_id, room_number, type_id, floor_number, amenities, notes, description, base_price) VALUES 
(room_seq.NEXTVAL, '105', 5, 1, 'WiFi, TV, Air Conditioning', 'Shared bathroom', 'Economy room on first floor', 75.00);

-- Floor 2 - Standard and Deluxe Rooms
INSERT INTO rooms (room_id, room_number, type_id, floor_number, amenities, notes, description, base_price) VALUES 
(room_seq.NEXTVAL, '201', 2, 2, 'WiFi, TV, Air Conditioning, Mini Fridge', 'City view', 'Standard double room on second floor', 150.00);
INSERT INTO rooms (room_id, room_number, type_id, floor_number, amenities, notes, description, base_price) VALUES 
(room_seq.NEXTVAL, '202', 2, 2, 'WiFi, TV, Air Conditioning, Mini Fridge', 'Garden view', 'Standard double room on second floor', 150.00);
INSERT INTO rooms (room_id, room_number, type_id, floor_number, amenities, notes, description, base_price) VALUES 
(room_seq.NEXTVAL, '203', 3, 2, 'WiFi, TV, Air Conditioning, Mini Fridge, Balcony', 'Premium suite', 'Deluxe suite on second floor', 300.00);
INSERT INTO rooms (room_id, room_number, type_id, floor_number, amenities, notes, description, base_price) VALUES 
(room_seq.NEXTVAL, '204', 3, 2, 'WiFi, TV, Air Conditioning, Mini Fridge, Balcony', 'Corner suite', 'Deluxe suite on second floor', 300.00);
INSERT INTO rooms (room_id, room_number, type_id, floor_number, amenities, notes, description, base_price) VALUES 
(room_seq.NEXTVAL, '205', 1, 2, 'WiFi, TV, Air Conditioning', 'Quiet room', 'Standard single room on second floor', 100.00);

-- Floor 3 - Premium Rooms
INSERT INTO rooms (room_id, room_number, type_id, floor_number, amenities, notes, description, base_price) VALUES 
(room_seq.NEXTVAL, '301', 3, 3, 'WiFi, TV, Air Conditioning, Mini Fridge, Balcony', 'Premium suite', 'Deluxe suite on third floor', 300.00);
INSERT INTO rooms (room_id, room_number, type_id, floor_number, amenities, notes, description, base_price) VALUES 
(room_seq.NEXTVAL, '302', 3, 3, 'WiFi, TV, Air Conditioning, Mini Fridge, Balcony', 'City view suite', 'Deluxe suite on third floor', 300.00);
INSERT INTO rooms (room_id, room_number, type_id, floor_number, amenities, notes, description, base_price) VALUES 
(room_seq.NEXTVAL, '303', 4, 3, 'WiFi, TV, Air Conditioning, Mini Fridge, Balcony, Jacuzzi', 'Presidential suite', 'Presidential suite on third floor', 500.00);
INSERT INTO rooms (room_id, room_number, type_id, floor_number, amenities, notes, description, base_price) VALUES 
(room_seq.NEXTVAL, '304', 2, 3, 'WiFi, TV, Air Conditioning, Mini Fridge', 'Premium double', 'Standard double room on third floor', 150.00);
INSERT INTO rooms (room_id, room_number, type_id, floor_number, amenities, notes, description, base_price) VALUES 
(room_seq.NEXTVAL, '305', 2, 3, 'WiFi, TV, Air Conditioning, Mini Fridge', 'Premium double', 'Standard double room on third floor', 150.00);

-- Floor 4 - Luxury Suites
INSERT INTO rooms (room_id, room_number, type_id, floor_number, amenities, notes, description, base_price) VALUES 
(room_seq.NEXTVAL, '401', 4, 4, 'WiFi, TV, Air Conditioning, Mini Fridge, Balcony, Jacuzzi', 'Top floor suite', 'Presidential suite on fourth floor', 500.00);
INSERT INTO rooms (room_id, room_number, type_id, floor_number, amenities, notes, description, base_price) VALUES 
(room_seq.NEXTVAL, '402', 4, 4, 'WiFi, TV, Air Conditioning, Mini Fridge, Balcony, Jacuzzi', 'Corner presidential', 'Presidential suite on fourth floor', 500.00);
INSERT INTO rooms (room_id, room_number, type_id, floor_number, amenities, notes, description, base_price) VALUES 
(room_seq.NEXTVAL, '403', 3, 4, 'WiFi, TV, Air Conditioning, Mini Fridge, Balcony', 'Premium suite', 'Deluxe suite on fourth floor', 300.00);
INSERT INTO rooms (room_id, room_number, type_id, floor_number, amenities, notes, description, base_price) VALUES 
(room_seq.NEXTVAL, '404', 3, 4, 'WiFi, TV, Air Conditioning, Mini Fridge, Balcony', 'Premium suite', 'Deluxe suite on fourth floor', 300.00);
INSERT INTO rooms (room_id, room_number, type_id, floor_number, amenities, notes, description, base_price) VALUES 
(room_seq.NEXTVAL, '405', 3, 4, 'WiFi, TV, Air Conditioning, Mini Fridge, Balcony', 'Premium suite', 'Deluxe suite on fourth floor', 300.00);

-- Insert Sample Customers
INSERT INTO customers (customer_id, first_name, last_name, email, phone, address, date_of_birth, total_spent, blacklist_status, vip_promotion_eligible) VALUES 
(customer_seq.NEXTVAL, 'John', 'Smith', 'john.smith@email.com', '+1-555-0101', '123 Main St, New York, NY 10001', DATE '1985-03-15', 2500.00, 'N', 'Y');

INSERT INTO customers (customer_id, first_name, last_name, email, phone, address, date_of_birth, total_spent, blacklist_status, vip_promotion_eligible) VALUES 
(customer_seq.NEXTVAL, 'Sarah', 'Johnson', 'sarah.johnson@email.com', '+1-555-0102', '456 Oak Ave, Los Angeles, CA 90210', DATE '1990-07-22', 5000.00, 'N', 'Y');

INSERT INTO customers (customer_id, first_name, last_name, email, phone, address, date_of_birth, total_spent, blacklist_status, vip_promotion_eligible) VALUES 
(customer_seq.NEXTVAL, 'Michael', 'Brown', 'michael.brown@email.com', '+1-555-0103', '789 Pine St, Chicago, IL 60601', DATE '1978-11-08', 1200.00, 'N', 'N');

INSERT INTO customers (customer_id, first_name, last_name, email, phone, address, date_of_birth, total_spent, blacklist_status, vip_promotion_eligible) VALUES 
(customer_seq.NEXTVAL, 'Emily', 'Davis', 'emily.davis@email.com', '+1-555-0104', '321 Elm St, Miami, FL 33101', DATE '1992-05-30', 8000.00, 'N', 'Y');

INSERT INTO customers (customer_id, first_name, last_name, email, phone, address, date_of_birth, total_spent, blacklist_status, vip_promotion_eligible) VALUES 
(customer_seq.NEXTVAL, 'Robert', 'Wilson', 'robert.wilson@email.com', '+1-555-0105', '654 Maple Dr, Seattle, WA 98101', DATE '1975-09-12', 15000.00, 'N', 'Y');

-- Insert VIP Members (customers who have spent threshold amount)
INSERT INTO vip_members (vip_id, customer_id, membership_level, discount_percentage, benefits, booking_count) VALUES 
(vip_seq.NEXTVAL, 2, 'GOLD', 10.00, 'Free WiFi, Late Checkout, Priority Booking', 15);

INSERT INTO vip_members (vip_id, customer_id, membership_level, discount_percentage, benefits, booking_count) VALUES 
(vip_seq.NEXTVAL, 4, 'PLATINUM', 15.00, 'Free WiFi, Late Checkout, Priority Booking, Complimentary Breakfast', 25);

INSERT INTO vip_members (vip_id, customer_id, membership_level, discount_percentage, benefits, booking_count) VALUES 
(vip_seq.NEXTVAL, 5, 'DIAMOND', 20.00, 'Free WiFi, Late Checkout, Priority Booking, Complimentary Breakfast, Room Upgrade, Concierge Service', 40);

-- Insert Sample Bookings
INSERT INTO bookings (booking_id, customer_id, room_id, check_in_date, check_out_date, total_amount, booking_status, services_total) VALUES 
(booking_seq.NEXTVAL, 1, 1, DATE '2025-01-15', DATE '2025-01-18', 300.00, 'CONFIRMED', 0.00);

INSERT INTO bookings (booking_id, customer_id, room_id, check_in_date, check_out_date, actual_check_in, actual_check_out, total_amount, booking_status, services_total) VALUES 
(booking_seq.NEXTVAL, 2, 3, DATE '2025-01-10', DATE '2025-01-12', DATE '2025-01-10', DATE '2025-01-12', 600.00, 'CHECKED_OUT', 50.00);

INSERT INTO bookings (booking_id, customer_id, room_id, check_in_date, check_out_date, actual_check_in, total_amount, booking_status, services_total) VALUES 
(booking_seq.NEXTVAL, 3, 5, DATE '2025-01-20', DATE '2025-01-25', DATE '2025-01-20', 375.00, 'CHECKED_IN', 25.00);

COMMIT;

-- Display table creation summary
SELECT 'Tables created successfully' AS status FROM dual;
SELECT table_name FROM user_tables WHERE table_name IN ('ROOM_TYPES', 'ROOMS', 'CUSTOMERS', 'VIP_MEMBERS', 'BOOKINGS', 'BOOKING_ARCHIVE', 'AUDIT_LOG');

