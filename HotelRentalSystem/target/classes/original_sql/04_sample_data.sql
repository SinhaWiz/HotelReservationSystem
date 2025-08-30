-- ======================================================
-- Hotel Reservation System - Sample Data
-- File: 04_sample_data.sql
-- Purpose: Insert sample data for testing and demonstration
-- ======================================================

-- ======================================================
-- ROOM TYPES
-- ======================================================
INSERT INTO room_types(type_id, type_name, base_price, max_occupancy, amenities, description)
VALUES (room_type_seq.NEXTVAL, 'STANDARD', 100.00, 2, 'WiFi,TV,Air Conditioning', 'Standard room with basic amenities');

INSERT INTO room_types(type_id, type_name, base_price, max_occupancy, amenities, description)
VALUES (room_type_seq.NEXTVAL, 'DELUXE', 180.00, 3, 'WiFi,TV,Air Conditioning,Mini Bar,Room Service', 'Deluxe room with enhanced amenities');

INSERT INTO room_types(type_id, type_name, base_price, max_occupancy, amenities, description)
VALUES (room_type_seq.NEXTVAL, 'SUITE', 300.00, 4, 'WiFi,TV,Air Conditioning,Mini Bar,Room Service,Balcony,Jacuzzi', 'Luxury suite with premium amenities');

INSERT INTO room_types(type_id, type_name, base_price, max_occupancy, amenities, description)
VALUES (room_type_seq.NEXTVAL, 'PRESIDENTIAL', 500.00, 6, 'WiFi,TV,Air Conditioning,Mini Bar,Room Service,Balcony,Jacuzzi,Butler Service,Kitchen', 'Presidential suite with all luxury amenities');

-- ======================================================
-- ROOMS
-- ======================================================
-- Standard rooms (Floor 1-2)
INSERT ALL
  INTO rooms(room_id, room_number, type_id, floor_number, status, base_price)
    VALUES (room_seq.NEXTVAL, '101', 1, 1, 'AVAILABLE', 100.00)
  INTO rooms(room_id, room_number, type_id, floor_number, status, base_price)
    VALUES (room_seq.NEXTVAL, '102', 1, 1, 'AVAILABLE', 100.00)
  INTO rooms(room_id, room_number, type_id, floor_number, status, base_price)
    VALUES (room_seq.NEXTVAL, '103', 1, 1, 'MAINTENANCE', 100.00)
  INTO rooms(room_id, room_number, type_id, floor_number, status, base_price)
    VALUES (room_seq.NEXTVAL, '201', 1, 2, 'AVAILABLE', 100.00)
  INTO rooms(room_id, room_number, type_id, floor_number, status, base_price)
    VALUES (room_seq.NEXTVAL, '202', 1, 2, 'AVAILABLE', 100.00)
SELECT * FROM dual;

-- Deluxe rooms (Floor 3-4)
INSERT ALL
  INTO rooms(room_id, room_number, type_id, floor_number, status, base_price)
    VALUES (room_seq.NEXTVAL, '301', 2, 3, 'AVAILABLE', 180.00)
  INTO rooms(room_id, room_number, type_id, floor_number, status, base_price)
    VALUES (room_seq.NEXTVAL, '302', 2, 3, 'AVAILABLE', 180.00)
  INTO rooms(room_id, room_number, type_id, floor_number, status, base_price)
    VALUES (room_seq.NEXTVAL, '401', 2, 4, 'AVAILABLE', 180.00)
  INTO rooms(room_id, room_number, type_id, floor_number, status, base_price)
    VALUES (room_seq.NEXTVAL, '402', 2, 4, 'OCCUPIED', 180.00)
SELECT * FROM dual;

-- Suites (Floor 5)
INSERT ALL
  INTO rooms(room_id, room_number, type_id, floor_number, status, base_price)
    VALUES (room_seq.NEXTVAL, '501', 3, 5, 'AVAILABLE', 300.00)
  INTO rooms(room_id, room_number, type_id, floor_number, status, base_price)
    VALUES (room_seq.NEXTVAL, '502', 3, 5, 'AVAILABLE', 300.00)
SELECT * FROM dual;

-- Presidential Suite (Floor 6)
INSERT INTO rooms(room_id, room_number, type_id, floor_number, status, base_price)
VALUES (room_seq.NEXTVAL, '601', 4, 6, 'AVAILABLE', 500.00);

-- ======================================================
-- CUSTOMERS
-- ======================================================
INSERT ALL
  INTO customers(customer_id, first_name, last_name, email, phone, address, total_spent, loyalty_points)
    VALUES (customer_seq.NEXTVAL, 'John', 'Doe', 'john.doe@email.com', '+1-555-0101', '123 Main St, New York, NY 10001', 0, 0)
  INTO customers(customer_id, first_name, last_name, email, phone, address, total_spent, loyalty_points)
    VALUES (customer_seq.NEXTVAL, 'Jane', 'Smith', 'jane.smith@email.com', '+1-555-0102', '456 Oak Ave, Los Angeles, CA 90210', 8500, 850)
  INTO customers(customer_id, first_name, last_name, email, phone, address, total_spent, loyalty_points)
    VALUES (customer_seq.NEXTVAL, 'Michael', 'Johnson', 'michael.johnson@email.com', '+1-555-0103', '789 Pine St, Chicago, IL 60601', 15000, 1500)
  INTO customers(customer_id, first_name, last_name, email, phone, address, total_spent, loyalty_points)
    VALUES (customer_seq.NEXTVAL, 'Emily', 'Davis', 'emily.davis@email.com', '+1-555-0104', '321 Elm St, Miami, FL 33101', 25000, 2500)
  INTO customers(customer_id, first_name, last_name, email, phone, address, total_spent, loyalty_points)
    VALUES (customer_seq.NEXTVAL, 'Robert', 'Wilson', 'robert.wilson@email.com', '+1-555-0105', '654 Maple Dr, Seattle, WA 98101', 3200, 320)
SELECT * FROM dual;

-- ======================================================
-- VIP MEMBERS (Based on spending levels)
-- ======================================================
-- Gold VIP (Jane Smith - $8,500 spent)
INSERT INTO vip_members(vip_id, customer_id, membership_level, discount_percentage, benefits, is_active)
VALUES (vip_seq.NEXTVAL, 2, 'GOLD', 10, 'Priority Booking, Late Checkout, Room Upgrades', 'Y');

-- Platinum VIP (Michael Johnson - $15,000 spent)
INSERT INTO vip_members(vip_id, customer_id, membership_level, discount_percentage, benefits, is_active)
VALUES (vip_seq.NEXTVAL, 3, 'PLATINUM', 15, 'Priority Booking, Late Checkout, Room Upgrades, Complimentary Breakfast', 'Y');

-- Diamond VIP (Emily Davis - $25,000 spent)
INSERT INTO vip_members(vip_id, customer_id, membership_level, discount_percentage, benefits, is_active)
VALUES (vip_seq.NEXTVAL, 4, 'DIAMOND', 20, 'Priority Booking, Late Checkout, Room Upgrades, Complimentary Breakfast, Butler Service', 'Y');

-- ======================================================
-- ROOM SERVICES
-- ======================================================
INSERT ALL
  INTO room_services(service_id, service_name, service_description, service_category, base_price, is_active)
    VALUES (room_service_seq.NEXTVAL, 'Room Cleaning', 'Daily housekeeping service', 'HOUSEKEEPING', 25.00, 'Y')
  INTO room_services(service_id, service_name, service_description, service_category, base_price, is_active)
    VALUES (room_service_seq.NEXTVAL, 'Laundry Service', 'Wash and fold laundry service', 'HOUSEKEEPING', 35.00, 'Y')
  INTO room_services(service_id, service_name, service_description, service_category, base_price, is_active)
    VALUES (room_service_seq.NEXTVAL, 'Continental Breakfast', 'Continental breakfast in room', 'FOOD', 25.00, 'Y')
  INTO room_services(service_id, service_name, service_description, service_category, base_price, is_active)
    VALUES (room_service_seq.NEXTVAL, 'Gourmet Dinner', 'Multi-course dinner service', 'FOOD', 85.00, 'Y')
  INTO room_services(service_id, service_name, service_description, service_category, base_price, is_active)
    VALUES (room_service_seq.NEXTVAL, 'Spa Treatment', 'In-room massage and spa service', 'WELLNESS', 120.00, 'Y')
  INTO room_services(service_id, service_name, service_description, service_category, base_price, is_active)
    VALUES (room_service_seq.NEXTVAL, 'Late Checkout', 'Checkout after standard time', 'ACCOMMODATION', 50.00, 'Y')
  INTO room_services(service_id, service_name, service_description, service_category, base_price, is_active)
    VALUES (room_service_seq.NEXTVAL, 'Airport Transfer', 'Luxury car transfer service', 'TRANSPORTATION', 75.00, 'Y')
  INTO room_services(service_id, service_name, service_description, service_category, base_price, is_active)
    VALUES (room_service_seq.NEXTVAL, 'Mini Bar Refill', 'Premium mini bar restocking', 'FOOD', 45.00, 'Y')
SELECT * FROM dual;

-- ======================================================
-- ROOM SERVICE ASSIGNMENTS
-- ======================================================
-- Standard rooms - basic services
INSERT ALL
  INTO room_service_assignments(assignment_id, room_type_id, service_id, is_complimentary)
    VALUES (assignment_seq.NEXTVAL, 1, 1, 'Y')  -- Room Cleaning
  INTO room_service_assignments(assignment_id, room_type_id, service_id, is_complimentary)
    VALUES (assignment_seq.NEXTVAL, 1, 2, 'N')  -- Laundry Service
  INTO room_service_assignments(assignment_id, room_type_id, service_id, is_complimentary)
    VALUES (assignment_seq.NEXTVAL, 1, 3, 'N')  -- Continental Breakfast
  INTO room_service_assignments(assignment_id, room_type_id, service_id, is_complimentary)
    VALUES (assignment_seq.NEXTVAL, 1, 6, 'N')  -- Late Checkout
SELECT * FROM dual;

-- Deluxe rooms - enhanced services
INSERT ALL
  INTO room_service_assignments(assignment_id, room_type_id, service_id, is_complimentary)
    VALUES (assignment_seq.NEXTVAL, 2, 1, 'Y')  -- Room Cleaning
  INTO room_service_assignments(assignment_id, room_type_id, service_id, is_complimentary)
    VALUES (assignment_seq.NEXTVAL, 2, 2, 'N')  -- Laundry Service
  INTO room_service_assignments(assignment_id, room_type_id, service_id, is_complimentary)
    VALUES (assignment_seq.NEXTVAL, 2, 3, 'Y')  -- Continental Breakfast
  INTO room_service_assignments(assignment_id, room_type_id, service_id, is_complimentary)
    VALUES (assignment_seq.NEXTVAL, 2, 4, 'N')  -- Gourmet Dinner
  INTO room_service_assignments(assignment_id, room_type_id, service_id, is_complimentary)
    VALUES (assignment_seq.NEXTVAL, 2, 6, 'N')  -- Late Checkout
  INTO room_service_assignments(assignment_id, room_type_id, service_id, is_complimentary)
    VALUES (assignment_seq.NEXTVAL, 2, 8, 'N')  -- Mini Bar Refill
SELECT * FROM dual;

-- Suites - premium services
INSERT ALL
  INTO room_service_assignments(assignment_id, room_type_id, service_id, is_complimentary)
    VALUES (assignment_seq.NEXTVAL, 3, 1, 'Y')  -- Room Cleaning
  INTO room_service_assignments(assignment_id, room_type_id, service_id, is_complimentary)
    VALUES (assignment_seq.NEXTVAL, 3, 2, 'Y')  -- Laundry Service
  INTO room_service_assignments(assignment_id, room_type_id, service_id, is_complimentary)
    VALUES (assignment_seq.NEXTVAL, 3, 3, 'Y')  -- Continental Breakfast
  INTO room_service_assignments(assignment_id, room_type_id, service_id, is_complimentary)
    VALUES (assignment_seq.NEXTVAL, 3, 4, 'N')  -- Gourmet Dinner
  INTO room_service_assignments(assignment_id, room_type_id, service_id, is_complimentary)
    VALUES (assignment_seq.NEXTVAL, 3, 5, 'N')  -- Spa Treatment
  INTO room_service_assignments(assignment_id, room_type_id, service_id, is_complimentary)
    VALUES (assignment_seq.NEXTVAL, 3, 6, 'Y')  -- Late Checkout
  INTO room_service_assignments(assignment_id, room_type_id, service_id, is_complimentary)
    VALUES (assignment_seq.NEXTVAL, 3, 7, 'N')  -- Airport Transfer
  INTO room_service_assignments(assignment_id, room_type_id, service_id, is_complimentary)
    VALUES (assignment_seq.NEXTVAL, 3, 8, 'Y')  -- Mini Bar Refill
SELECT * FROM dual;

-- Presidential Suite - all services
INSERT ALL
  INTO room_service_assignments(assignment_id, room_type_id, service_id, is_complimentary)
    VALUES (assignment_seq.NEXTVAL, 4, 1, 'Y')  -- Room Cleaning
  INTO room_service_assignments(assignment_id, room_type_id, service_id, is_complimentary)
    VALUES (assignment_seq.NEXTVAL, 4, 2, 'Y')  -- Laundry Service
  INTO room_service_assignments(assignment_id, room_type_id, service_id, is_complimentary)
    VALUES (assignment_seq.NEXTVAL, 4, 3, 'Y')  -- Continental Breakfast
  INTO room_service_assignments(assignment_id, room_type_id, service_id, is_complimentary)
    VALUES (assignment_seq.NEXTVAL, 4, 4, 'Y')  -- Gourmet Dinner
  INTO room_service_assignments(assignment_id, room_type_id, service_id, is_complimentary)
    VALUES (assignment_seq.NEXTVAL, 4, 5, 'Y')  -- Spa Treatment
  INTO room_service_assignments(assignment_id, room_type_id, service_id, is_complimentary)
    VALUES (assignment_seq.NEXTVAL, 4, 6, 'Y')  -- Late Checkout
  INTO room_service_assignments(assignment_id, room_type_id, service_id, is_complimentary)
    VALUES (assignment_seq.NEXTVAL, 4, 7, 'Y')  -- Airport Transfer
  INTO room_service_assignments(assignment_id, room_type_id, service_id, is_complimentary)
    VALUES (assignment_seq.NEXTVAL, 4, 8, 'Y')  -- Mini Bar Refill
SELECT * FROM dual;

-- ======================================================
-- SAMPLE BOOKINGS (Various statuses for testing)
-- ======================================================
-- Future confirmed booking (John Doe)
INSERT INTO bookings(booking_id, customer_id, room_id, check_in_date, check_out_date, total_amount, booking_status, payment_status)
VALUES (booking_seq.NEXTVAL, 1, 1, SYSDATE + 7, SYSDATE + 10, 300.00, 'CONFIRMED', 'PENDING');

-- Current checked-in booking (Jane Smith - VIP Gold)
INSERT INTO bookings(booking_id, customer_id, room_id, check_in_date, check_out_date, actual_check_in, total_amount, discount_applied, booking_status, payment_status)
VALUES (booking_seq.NEXTVAL, 2, 8, SYSDATE - 1, SYSDATE + 2, SYSDATE - 1, 540.00, 54.00, 'CHECKED_IN', 'PENDING');

-- Completed booking (Michael Johnson - VIP Platinum)
INSERT INTO bookings(booking_id, customer_id, room_id, check_in_date, check_out_date, actual_check_in, actual_check_out, total_amount, discount_applied, booking_status, payment_status)
VALUES (booking_seq.NEXTVAL, 3, 9, SYSDATE - 14, SYSDATE - 11, SYSDATE - 14, SYSDATE - 11, 900.00, 135.00, 'CHECKED_OUT', 'PAID');

-- Future suite booking (Emily Davis - VIP Diamond)
INSERT INTO bookings(booking_id, customer_id, room_id, check_in_date, check_out_date, total_amount, discount_applied, booking_status, payment_status, special_requests)
VALUES (booking_seq.NEXTVAL, 4, 10, SYSDATE + 3, SYSDATE + 6, 900.00, 180.00, 'CONFIRMED', 'PENDING', 'Extra pillows, late checkout');

-- ======================================================
-- SERVICE USAGE EXAMPLES
-- ======================================================
-- Service usage for checked-in customer (Jane Smith)
INSERT ALL
  INTO customer_service_usage(usage_id, booking_id, customer_id, service_id, usage_date, quantity, unit_price, total_cost, is_complimentary)
    VALUES (usage_seq.NEXTVAL, 2, 2, 1, SYSDATE - 1, 1, 25.00, 25.00, 'Y')  -- Room Cleaning
  INTO customer_service_usage(usage_id, booking_id, customer_id, service_id, usage_date, quantity, unit_price, total_cost, is_complimentary)
    VALUES (usage_seq.NEXTVAL, 2, 2, 3, SYSDATE - 1, 1, 25.00, 25.00, 'Y')  -- Continental Breakfast
  INTO customer_service_usage(usage_id, booking_id, customer_id, service_id, usage_date, quantity, unit_price, total_cost, is_complimentary)
    VALUES (usage_seq.NEXTVAL, 2, 2, 4, SYSDATE, 1, 85.00, 85.00, 'N')      -- Gourmet Dinner
SELECT * FROM dual;

-- Service usage for completed booking (Michael Johnson)
INSERT ALL
  INTO customer_service_usage(usage_id, booking_id, customer_id, service_id, usage_date, quantity, unit_price, total_cost, is_complimentary)
    VALUES (usage_seq.NEXTVAL, 3, 3, 1, SYSDATE - 14, 3, 25.00, 75.00, 'Y')  -- Room Cleaning (3 days)
  INTO customer_service_usage(usage_id, booking_id, customer_id, service_id, usage_date, quantity, unit_price, total_cost, is_complimentary)
    VALUES (usage_seq.NEXTVAL, 3, 3, 5, SYSDATE - 12, 1, 120.00, 120.00, 'N') -- Spa Treatment
  INTO customer_service_usage(usage_id, booking_id, customer_id, service_id, usage_date, quantity, unit_price, total_cost, is_complimentary)
    VALUES (usage_seq.NEXTVAL, 3, 3, 7, SYSDATE - 11, 1, 75.00, 75.00, 'N')   -- Airport Transfer
SELECT * FROM dual;

COMMIT;

-- Display summary of inserted data
SELECT 'Data insertion completed successfully!' as status FROM dual;
SELECT 'Room Types: ' || COUNT(*) as summary FROM room_types
UNION ALL
SELECT 'Rooms: ' || COUNT(*) FROM rooms
UNION ALL
SELECT 'Customers: ' || COUNT(*) FROM customers
UNION ALL
SELECT 'VIP Members: ' || COUNT(*) FROM vip_members
UNION ALL
SELECT 'Services: ' || COUNT(*) FROM room_services
UNION ALL
SELECT 'Bookings: ' || COUNT(*) FROM bookings;
