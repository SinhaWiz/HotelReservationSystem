-- ======================================================
-- Hotel Reservation System - Sample Data (Safe for Multiple Runs)
-- File: 04_sample_data.sql
-- Purpose: Insert sample data for testing and demonstration
-- Note: Safe to run multiple times - checks for existing data
-- ======================================================

-- ======================================================
-- ROOM TYPESA
-- ======================================================
INSERT INTO room_types(type_id, type_name, base_price, max_occupancy, amenities, description)
SELECT room_type_seq.NEXTVAL, 'STANDARD', 100.00, 2, 'WiFi,TV,Air Conditioning', 'Standard room with basic amenities'
FROM dual WHERE NOT EXISTS (SELECT 1 FROM room_types WHERE type_name = 'STANDARD');

INSERT INTO room_types(type_id, type_name, base_price, max_occupancy, amenities, description)
SELECT room_type_seq.NEXTVAL, 'DELUXE', 180.00, 3, 'WiFi,TV,Air Conditioning,Mini Bar,Room Service', 'Deluxe room with enhanced amenities'
FROM dual WHERE NOT EXISTS (SELECT 1 FROM room_types WHERE type_name = 'DELUXE');

INSERT INTO room_types(type_id, type_name, base_price, max_occupancy, amenities, description)
SELECT room_type_seq.NEXTVAL, 'SUITE', 300.00, 4, 'WiFi,TV,Air Conditioning,Mini Bar,Room Service,Balcony,Jacuzzi', 'Luxury suite with premium amenities'
FROM dual WHERE NOT EXISTS (SELECT 1 FROM room_types WHERE type_name = 'SUITE');

INSERT INTO room_types(type_id, type_name, base_price, max_occupancy, amenities, description)
SELECT room_type_seq.NEXTVAL, 'PRESIDENTIAL', 500.00, 6, 'WiFi,TV,Air Conditioning,Mini Bar,Room Service,Balcony,Jacuzzi,Butler Service,Kitchen', 'Presidential suite with all luxury amenities'
FROM dual WHERE NOT EXISTS (SELECT 1 FROM room_types WHERE type_name = 'PRESIDENTIAL');

-- ======================================================
-- ROOMS
-- ======================================================
-- Standard rooms (Floor 1-2)
INSERT INTO rooms(room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '101', (SELECT type_id FROM room_types WHERE type_name = 'STANDARD'), 1, 'AVAILABLE', 100.00
FROM dual WHERE NOT EXISTS (SELECT 1 FROM rooms WHERE room_number = '101');

INSERT INTO rooms(room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '102', (SELECT type_id FROM room_types WHERE type_name = 'STANDARD'), 1, 'AVAILABLE', 100.00
FROM dual WHERE NOT EXISTS (SELECT 1 FROM rooms WHERE room_number = '102');

INSERT INTO rooms(room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '103', (SELECT type_id FROM room_types WHERE type_name = 'STANDARD'), 1, 'MAINTENANCE', 100.00
FROM dual WHERE NOT EXISTS (SELECT 1 FROM rooms WHERE room_number = '103');

INSERT INTO rooms(room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '201', (SELECT type_id FROM room_types WHERE type_name = 'STANDARD'), 2, 'AVAILABLE', 100.00
FROM dual WHERE NOT EXISTS (SELECT 1 FROM rooms WHERE room_number = '201');

INSERT INTO rooms(room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '202', (SELECT type_id FROM room_types WHERE type_name = 'STANDARD'), 2, 'AVAILABLE', 100.00
FROM dual WHERE NOT EXISTS (SELECT 1 FROM rooms WHERE room_number = '202');

-- Deluxe rooms (Floor 3-4)
INSERT INTO rooms(room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '301', (SELECT type_id FROM room_types WHERE type_name = 'DELUXE'), 3, 'AVAILABLE', 180.00
FROM dual WHERE NOT EXISTS (SELECT 1 FROM rooms WHERE room_number = '301');

INSERT INTO rooms(room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '302', (SELECT type_id FROM room_types WHERE type_name = 'DELUXE'), 3, 'AVAILABLE', 180.00
FROM dual WHERE NOT EXISTS (SELECT 1 FROM rooms WHERE room_number = '302');

INSERT INTO rooms(room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '401', (SELECT type_id FROM room_types WHERE type_name = 'DELUXE'), 4, 'AVAILABLE', 180.00
FROM dual WHERE NOT EXISTS (SELECT 1 FROM rooms WHERE room_number = '401');

INSERT INTO rooms(room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '402', (SELECT type_id FROM room_types WHERE type_name = 'DELUXE'), 4, 'OCCUPIED', 180.00
FROM dual WHERE NOT EXISTS (SELECT 1 FROM rooms WHERE room_number = '402');

-- Suites (Floor 5)
INSERT INTO rooms(room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '501', (SELECT type_id FROM room_types WHERE type_name = 'SUITE'), 5, 'AVAILABLE', 300.00
FROM dual WHERE NOT EXISTS (SELECT 1 FROM rooms WHERE room_number = '501');

INSERT INTO rooms(room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '502', (SELECT type_id FROM room_types WHERE type_name = 'SUITE'), 5, 'AVAILABLE', 300.00
FROM dual WHERE NOT EXISTS (SELECT 1 FROM rooms WHERE room_number = '502');

-- Presidential Suite (Floor 6)
INSERT INTO rooms(room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '601', (SELECT type_id FROM room_types WHERE type_name = 'PRESIDENTIAL'), 6, 'AVAILABLE', 500.00
FROM dual WHERE NOT EXISTS (SELECT 1 FROM rooms WHERE room_number = '601');

-- ======================================================
-- CUSTOMERS
-- ======================================================
INSERT INTO customers(customer_id, first_name, last_name, email, phone, address, total_spent, loyalty_points)
SELECT customer_seq.NEXTVAL, 'John', 'Doe', 'john.doe@email.com', '+1-555-0101', '123 Main St, New York, NY 10001', 0, 0
FROM dual WHERE NOT EXISTS (SELECT 1 FROM customers WHERE email = 'john.doe@email.com');

INSERT INTO customers(customer_id, first_name, last_name, email, phone, address, total_spent, loyalty_points)
SELECT customer_seq.NEXTVAL, 'Jane', 'Smith', 'jane.smith@email.com', '+1-555-0102', '456 Oak Ave, Los Angeles, CA 90210', 8500, 850
FROM dual WHERE NOT EXISTS (SELECT 1 FROM customers WHERE email = 'jane.smith@email.com');

INSERT INTO customers(customer_id, first_name, last_name, email, phone, address, total_spent, loyalty_points)
SELECT customer_seq.NEXTVAL, 'Michael', 'Johnson', 'michael.johnson@email.com', '+1-555-0103', '789 Pine St, Chicago, IL 60601', 15000, 1500
FROM dual WHERE NOT EXISTS (SELECT 1 FROM customers WHERE email = 'michael.johnson@email.com');

INSERT INTO customers(customer_id, first_name, last_name, email, phone, address, total_spent, loyalty_points)
SELECT customer_seq.NEXTVAL, 'Emily', 'Davis', 'emily.davis@email.com', '+1-555-0104', '321 Elm St, Miami, FL 33101', 25000, 2500
FROM dual WHERE NOT EXISTS (SELECT 1 FROM customers WHERE email = 'emily.davis@email.com');

INSERT INTO customers(customer_id, first_name, last_name, email, phone, address, total_spent, loyalty_points)
SELECT customer_seq.NEXTVAL, 'Robert', 'Wilson', 'robert.wilson@email.com', '+1-555-0105', '654 Maple Dr, Seattle, WA 98101', 3200, 320
FROM dual WHERE NOT EXISTS (SELECT 1 FROM customers WHERE email = 'robert.wilson@email.com');

-- ======================================================
-- VIP MEMBERS (Based on spending levels)
-- ======================================================
-- Gold VIP (Jane Smith - $8,500 spent)
INSERT INTO vip_members(vip_id, customer_id, membership_level, discount_percentage, benefits, is_active)
SELECT vip_seq.NEXTVAL,
       (SELECT customer_id FROM customers WHERE email = 'jane.smith@email.com'),
       'GOLD', 10, 'Priority Booking, Late Checkout, Room Upgrades', 'Y'
FROM dual WHERE NOT EXISTS (
    SELECT 1 FROM vip_members vm
    JOIN customers c ON vm.customer_id = c.customer_id
    WHERE c.email = 'jane.smith@email.com'
);

-- Platinum VIP (Michael Johnson - $15,000 spent)
INSERT INTO vip_members(vip_id, customer_id, membership_level, discount_percentage, benefits, is_active)
SELECT vip_seq.NEXTVAL,
       (SELECT customer_id FROM customers WHERE email = 'michael.johnson@email.com'),
       'PLATINUM', 15, 'Priority Booking, Late Checkout, Room Upgrades, Complimentary Breakfast', 'Y'
FROM dual WHERE NOT EXISTS (
    SELECT 1 FROM vip_members vm
    JOIN customers c ON vm.customer_id = c.customer_id
    WHERE c.email = 'michael.johnson@email.com'
);

-- Diamond VIP (Emily Davis - $25,000 spent)
INSERT INTO vip_members(vip_id, customer_id, membership_level, discount_percentage, benefits, is_active)
SELECT vip_seq.NEXTVAL,
       (SELECT customer_id FROM customers WHERE email = 'emily.davis@email.com'),
       'DIAMOND', 20, 'Priority Booking, Late Checkout, Room Upgrades, Complimentary Breakfast, Butler Service', 'Y'
FROM dual WHERE NOT EXISTS (
    SELECT 1 FROM vip_members vm
    JOIN customers c ON vm.customer_id = c.customer_id
    WHERE c.email = 'emily.davis@email.com'
);

-- ======================================================
-- ROOM SERVICES
-- ======================================================
INSERT INTO room_services(service_id, service_name, service_description, service_category, base_price, is_active)
SELECT room_service_seq.NEXTVAL, 'Room Cleaning', 'Daily housekeeping service', 'HOUSEKEEPING', 25.00, 'Y'
FROM dual WHERE NOT EXISTS (SELECT 1 FROM room_services WHERE service_name = 'Room Cleaning');

INSERT INTO room_services(service_id, service_name, service_description, service_category, base_price, is_active)
SELECT room_service_seq.NEXTVAL, 'Laundry Service', 'Wash and fold laundry service', 'HOUSEKEEPING', 35.00, 'Y'
FROM dual WHERE NOT EXISTS (SELECT 1 FROM room_services WHERE service_name = 'Laundry Service');

INSERT INTO room_services(service_id, service_name, service_description, service_category, base_price, is_active)
SELECT room_service_seq.NEXTVAL, 'Continental Breakfast', 'Continental breakfast in room', 'FOOD', 25.00, 'Y'
FROM dual WHERE NOT EXISTS (SELECT 1 FROM room_services WHERE service_name = 'Continental Breakfast');

INSERT INTO room_services(service_id, service_name, service_description, service_category, base_price, is_active)
SELECT room_service_seq.NEXTVAL, 'Gourmet Dinner', 'Multi-course dinner service', 'FOOD', 85.00, 'Y'
FROM dual WHERE NOT EXISTS (SELECT 1 FROM room_services WHERE service_name = 'Gourmet Dinner');

INSERT INTO room_services(service_id, service_name, service_description, service_category, base_price, is_active)
SELECT room_service_seq.NEXTVAL, 'Spa Treatment', 'In-room massage and spa service', 'WELLNESS', 120.00, 'Y'
FROM dual WHERE NOT EXISTS (SELECT 1 FROM room_services WHERE service_name = 'Spa Treatment');

INSERT INTO room_services(service_id, service_name, service_description, service_category, base_price, is_active)
SELECT room_service_seq.NEXTVAL, 'Late Checkout', 'Checkout after standard time', 'ACCOMMODATION', 50.00, 'Y'
FROM dual WHERE NOT EXISTS (SELECT 1 FROM room_services WHERE service_name = 'Late Checkout');

INSERT INTO room_services(service_id, service_name, service_description, service_category, base_price, is_active)
SELECT room_service_seq.NEXTVAL, 'Airport Transfer', 'Luxury car transfer service', 'TRANSPORTATION', 75.00, 'Y'
FROM dual WHERE NOT EXISTS (SELECT 1 FROM room_services WHERE service_name = 'Airport Transfer');

INSERT INTO room_services(service_id, service_name, service_description, service_category, base_price, is_active)
SELECT room_service_seq.NEXTVAL, 'Mini Bar Refill', 'Premium mini bar restocking', 'FOOD', 45.00, 'Y'
FROM dual WHERE NOT EXISTS (SELECT 1 FROM room_services WHERE service_name = 'Mini Bar Refill');

-- ======================================================
-- ROOM SERVICE ASSIGNMENTS
-- ======================================================
-- Standard rooms - basic services
INSERT INTO room_service_assignments(assignment_id, room_type_id, service_id, is_complimentary)
SELECT assignment_seq.NEXTVAL,
       (SELECT type_id FROM room_types WHERE type_name = 'STANDARD'),
       (SELECT service_id FROM room_services WHERE service_name = 'Room Cleaning'),
       'Y'
FROM dual WHERE NOT EXISTS (
    SELECT 1 FROM room_service_assignments rsa
    JOIN room_types rt ON rsa.room_type_id = rt.type_id
    JOIN room_services rs ON rsa.service_id = rs.service_id
    WHERE rt.type_name = 'STANDARD' AND rs.service_name = 'Room Cleaning'
);

INSERT INTO room_service_assignments(assignment_id, room_type_id, service_id, is_complimentary)
SELECT assignment_seq.NEXTVAL,
       (SELECT type_id FROM room_types WHERE type_name = 'STANDARD'),
       (SELECT service_id FROM room_services WHERE service_name = 'Laundry Service'),
       'N'
FROM dual WHERE NOT EXISTS (
    SELECT 1 FROM room_service_assignments rsa
    JOIN room_types rt ON rsa.room_type_id = rt.type_id
    JOIN room_services rs ON rsa.service_id = rs.service_id
    WHERE rt.type_name = 'STANDARD' AND rs.service_name = 'Laundry Service'
);

INSERT INTO room_service_assignments(assignment_id, room_type_id, service_id, is_complimentary)
SELECT assignment_seq.NEXTVAL,
       (SELECT type_id FROM room_types WHERE type_name = 'STANDARD'),
       (SELECT service_id FROM room_services WHERE service_name = 'Continental Breakfast'),
       'N'
FROM dual WHERE NOT EXISTS (
    SELECT 1 FROM room_service_assignments rsa
    JOIN room_types rt ON rsa.room_type_id = rt.type_id
    JOIN room_services rs ON rsa.service_id = rs.service_id
    WHERE rt.type_name = 'STANDARD' AND rs.service_name = 'Continental Breakfast'
);

INSERT INTO room_service_assignments(assignment_id, room_type_id, service_id, is_complimentary)
SELECT assignment_seq.NEXTVAL,
       (SELECT type_id FROM room_types WHERE type_name = 'STANDARD'),
       (SELECT service_id FROM room_services WHERE service_name = 'Late Checkout'),
       'N'
FROM dual WHERE NOT EXISTS (
    SELECT 1 FROM room_service_assignments rsa
    JOIN room_types rt ON rsa.room_type_id = rt.type_id
    JOIN room_services rs ON rsa.service_id = rs.service_id
    WHERE rt.type_name = 'STANDARD' AND rs.service_name = 'Late Checkout'
);

-- Deluxe rooms - enhanced services (basic set for brevity)
INSERT INTO room_service_assignments(assignment_id, room_type_id, service_id, is_complimentary)
SELECT assignment_seq.NEXTVAL,
       (SELECT type_id FROM room_types WHERE type_name = 'DELUXE'),
       (SELECT service_id FROM room_services WHERE service_name = 'Room Cleaning'),
       'Y'
FROM dual WHERE NOT EXISTS (
    SELECT 1 FROM room_service_assignments rsa
    JOIN room_types rt ON rsa.room_type_id = rt.type_id
    JOIN room_services rs ON rsa.service_id = rs.service_id
    WHERE rt.type_name = 'DELUXE' AND rs.service_name = 'Room Cleaning'
);

INSERT INTO room_service_assignments(assignment_id, room_type_id, service_id, is_complimentary)
SELECT assignment_seq.NEXTVAL,
       (SELECT type_id FROM room_types WHERE type_name = 'DELUXE'),
       (SELECT service_id FROM room_services WHERE service_name = 'Continental Breakfast'),
       'Y'
FROM dual WHERE NOT EXISTS (
    SELECT 1 FROM room_service_assignments rsa
    JOIN room_types rt ON rsa.room_type_id = rt.type_id
    JOIN room_services rs ON rsa.service_id = rs.service_id
    WHERE rt.type_name = 'DELUXE' AND rs.service_name = 'Continental Breakfast'
);

-- ======================================================
-- SAMPLE BOOKINGS (Simplified for demonstration)
-- ======================================================
-- Future confirmed booking (John Doe) - only if no bookings exist for this customer
INSERT INTO bookings(booking_id, customer_id, room_id, check_in_date, check_out_date, total_amount, booking_status, payment_status)
SELECT booking_seq.NEXTVAL,
       (SELECT customer_id FROM customers WHERE email = 'john.doe@email.com'),
       (SELECT room_id FROM rooms WHERE room_number = '101'),
       SYSDATE + 7, SYSDATE + 10, 300.00, 'CONFIRMED', 'PENDING'
FROM dual WHERE NOT EXISTS (
    SELECT 1 FROM bookings b
    JOIN customers c ON b.customer_id = c.customer_id
    WHERE c.email = 'john.doe@email.com'
) AND EXISTS (SELECT 1 FROM customers WHERE email = 'john.doe@email.com')
  AND EXISTS (SELECT 1 FROM rooms WHERE room_number = '101');

COMMIT;

-- Display summary of inserted data
PROMPT ======================================================
PROMPT Sample Data Insertion Summary
PROMPT ======================================================
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
SELECT 'Service Assignments: ' || COUNT(*) FROM room_service_assignments
UNION ALL
SELECT 'Bookings: ' || COUNT(*) FROM bookings
UNION ALL
SELECT 'Service Usage Records: ' || COUNT(*) FROM customer_service_usage;

PROMPT ======================================================
PROMPT Sample data script completed. Safe to run multiple times.
PROMPT ======================================================
