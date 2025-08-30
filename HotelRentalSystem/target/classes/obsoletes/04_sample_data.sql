-- Comprehensive Sample Data for Hotel Management System
-- Oracle Database

-- Insert Room Types
INSERT INTO room_types (type_id, type_name, base_price, max_occupancy, amenities, description)
VALUES (room_type_seq.NEXTVAL, 'Standard Single', 89.99, 1, 'WiFi;TV;Desk;Mini Fridge', 'Cozy single room perfect for business travelers');

INSERT INTO room_types (type_id, type_name, base_price, max_occupancy, amenities, description)
VALUES (room_type_seq.NEXTVAL, 'Standard Double', 119.99, 2, 'WiFi;TV;Desk;Mini Fridge;Coffee Maker', 'Comfortable double room with modern amenities');

INSERT INTO room_types (type_id, type_name, base_price, max_occupancy, amenities, description)
VALUES (room_type_seq.NEXTVAL, 'Standard Queen', 139.99, 2, 'WiFi;TV;Desk;Mini Fridge;Coffee Maker;Iron', 'Spacious queen room with premium bedding');

INSERT INTO room_types (type_id, type_name, base_price, max_occupancy, amenities, description)
VALUES (room_type_seq.NEXTVAL, 'Standard Twin', 129.99, 2, 'WiFi;TV;Desk;Mini Fridge;Coffee Maker', 'Twin bed room ideal for colleagues or friends');

INSERT INTO room_types (type_id, type_name, base_price, max_occupancy, amenities, description)
VALUES (room_type_seq.NEXTVAL, 'Deluxe King', 189.99, 2, 'WiFi;4K TV;Desk;Mini Bar;Coffee Machine;Sofa;Balcony', 'Luxurious king room with city view');

INSERT INTO room_types (type_id, type_name, base_price, max_occupancy, amenities, description)
VALUES (room_type_seq.NEXTVAL, 'Family Suite', 269.99, 4, 'WiFi;2 TVs;Kitchenette;Sofa Bed;Game Console;Balcony', 'Perfect family accommodation with entertainment');

INSERT INTO room_types (type_id, type_name, base_price, max_occupancy, amenities, description)
VALUES (room_type_seq.NEXTVAL, 'Business Suite', 299.99, 2, 'WiFi;4K TV;Office Desk;Mini Bar;Coffee Machine;Meeting Table;Printer', 'Executive suite for business professionals');

INSERT INTO room_types (type_id, type_name, base_price, max_occupancy, amenities, description)
VALUES (room_type_seq.NEXTVAL, 'Presidential Suite', 599.99, 4, 'Private Lounge;Butler Service;Spa Bath;Panoramic View;Full Kitchen;Dining Room', 'Ultimate luxury accommodation');

-- Insert Rooms (50 rooms across 5 floors)
-- Floor 1: Standard rooms (101-110)
INSERT INTO rooms (room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '101', rt.type_id, 1, 'AVAILABLE', rt.base_price
FROM room_types rt WHERE rt.type_name = 'Standard Single';

INSERT INTO rooms (room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '102', rt.type_id, 1, 'AVAILABLE', rt.base_price
FROM room_types rt WHERE rt.type_name = 'Standard Double';

INSERT INTO rooms (room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '103', rt.type_id, 1, 'AVAILABLE', rt.base_price
FROM room_types rt WHERE rt.type_name = 'Standard Queen';

INSERT INTO rooms (room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '104', rt.type_id, 1, 'AVAILABLE', rt.base_price
FROM room_types rt WHERE rt.type_name = 'Standard Twin';

INSERT INTO rooms (room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '105', rt.type_id, 1, 'AVAILABLE', rt.base_price
FROM room_types rt WHERE rt.type_name = 'Standard Single';

INSERT INTO rooms (room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '106', rt.type_id, 1, 'AVAILABLE', rt.base_price
FROM room_types rt WHERE rt.type_name = 'Standard Double';

INSERT INTO rooms (room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '107', rt.type_id, 1, 'AVAILABLE', rt.base_price
FROM room_types rt WHERE rt.type_name = 'Standard Queen';

INSERT INTO rooms (room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '108', rt.type_id, 1, 'AVAILABLE', rt.base_price
FROM room_types rt WHERE rt.type_name = 'Standard Twin';

INSERT INTO rooms (room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '109', rt.type_id, 1, 'MAINTENANCE', rt.base_price
FROM room_types rt WHERE rt.type_name = 'Standard Single';

INSERT INTO rooms (room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '110', rt.type_id, 1, 'AVAILABLE', rt.base_price
FROM room_types rt WHERE rt.type_name = 'Standard Double';

-- Floor 2: Standard and Deluxe rooms (201-210)
INSERT INTO rooms (room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '201', rt.type_id, 2, 'AVAILABLE', rt.base_price
FROM room_types rt WHERE rt.type_name = 'Standard Queen';

INSERT INTO rooms (room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '202', rt.type_id, 2, 'OCCUPIED', rt.base_price
FROM room_types rt WHERE rt.type_name = 'Deluxe King';

INSERT INTO rooms (room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '203', rt.type_id, 2, 'AVAILABLE', rt.base_price
FROM room_types rt WHERE rt.type_name = 'Standard Twin';

INSERT INTO rooms (room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '204', rt.type_id, 2, 'AVAILABLE', rt.base_price
FROM room_types rt WHERE rt.type_name = 'Deluxe King';

INSERT INTO rooms (room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '205', rt.type_id, 2, 'AVAILABLE', rt.base_price
FROM room_types rt WHERE rt.type_name = 'Standard Queen';

INSERT INTO rooms (room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '206', rt.type_id, 2, 'AVAILABLE', rt.base_price
FROM room_types rt WHERE rt.type_name = 'Deluxe King';

INSERT INTO rooms (room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '207', rt.type_id, 2, 'AVAILABLE', rt.base_price
FROM room_types rt WHERE rt.type_name = 'Standard Twin';

INSERT INTO rooms (room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '208', rt.type_id, 2, 'AVAILABLE', rt.base_price
FROM room_types rt WHERE rt.type_name = 'Deluxe King';

INSERT INTO rooms (room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '209', rt.type_id, 2, 'AVAILABLE', rt.base_price
FROM room_types rt WHERE rt.type_name = 'Standard Queen';

INSERT INTO rooms (room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '210', rt.type_id, 2, 'AVAILABLE', rt.base_price
FROM room_types rt WHERE rt.type_name = 'Deluxe King';

-- Floor 3: Deluxe and Family rooms (301-310)
INSERT INTO rooms (room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '301', rt.type_id, 3, 'OCCUPIED', rt.base_price
FROM room_types rt WHERE rt.type_name = 'Family Suite';

INSERT INTO rooms (room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '302', rt.type_id, 3, 'AVAILABLE', rt.base_price
FROM room_types rt WHERE rt.type_name = 'Deluxe King';

INSERT INTO rooms (room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '303', rt.type_id, 3, 'AVAILABLE', rt.base_price
FROM room_types rt WHERE rt.type_name = 'Family Suite';

INSERT INTO rooms (room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '304', rt.type_id, 3, 'AVAILABLE', rt.base_price
FROM room_types rt WHERE rt.type_name = 'Deluxe King';

INSERT INTO rooms (room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '305', rt.type_id, 3, 'OCCUPIED', rt.base_price
FROM room_types rt WHERE rt.type_name = 'Family Suite';

INSERT INTO rooms (room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '306', rt.type_id, 3, 'AVAILABLE', rt.base_price
FROM room_types rt WHERE rt.type_name = 'Deluxe King';

INSERT INTO rooms (room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '307', rt.type_id, 3, 'AVAILABLE', rt.base_price
FROM room_types rt WHERE rt.type_name = 'Family Suite';

INSERT INTO rooms (room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '308', rt.type_id, 3, 'AVAILABLE', rt.base_price
FROM room_types rt WHERE rt.type_name = 'Deluxe King';

INSERT INTO rooms (room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '309', rt.type_id, 3, 'AVAILABLE', rt.base_price
FROM room_types rt WHERE rt.type_name = 'Family Suite';

INSERT INTO rooms (room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '310', rt.type_id, 3, 'AVAILABLE', rt.base_price
FROM room_types rt WHERE rt.type_name = 'Deluxe King';

-- Floor 4: Business Suites (401-410)
INSERT INTO rooms (room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '401', rt.type_id, 4, 'AVAILABLE', rt.base_price
FROM room_types rt WHERE rt.type_name = 'Business Suite';

INSERT INTO rooms (room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '402', rt.type_id, 4, 'AVAILABLE', rt.base_price
FROM room_types rt WHERE rt.type_name = 'Business Suite';

INSERT INTO rooms (room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '403', rt.type_id, 4, 'AVAILABLE', rt.base_price
FROM room_types rt WHERE rt.type_name = 'Business Suite';

INSERT INTO rooms (room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '404', rt.type_id, 4, 'AVAILABLE', rt.base_price
FROM room_types rt WHERE rt.type_name = 'Business Suite';

INSERT INTO rooms (room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '405', rt.type_id, 4, 'AVAILABLE', rt.base_price
FROM room_types rt WHERE rt.type_name = 'Business Suite';

INSERT INTO rooms (room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '406', rt.type_id, 4, 'AVAILABLE', rt.base_price
FROM room_types rt WHERE rt.type_name = 'Business Suite';

INSERT INTO rooms (room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '407', rt.type_id, 4, 'AVAILABLE', rt.base_price
FROM room_types rt WHERE rt.type_name = 'Business Suite';

INSERT INTO rooms (room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '408', rt.type_id, 4, 'AVAILABLE', rt.base_price
FROM room_types rt WHERE rt.type_name = 'Business Suite';

INSERT INTO rooms (room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '409', rt.type_id, 4, 'AVAILABLE', rt.base_price
FROM room_types rt WHERE rt.type_name = 'Business Suite';

INSERT INTO rooms (room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '410', rt.type_id, 4, 'AVAILABLE', rt.base_price
FROM room_types rt WHERE rt.type_name = 'Business Suite';

-- Floor 5: Presidential and Premium rooms (501-510)
INSERT INTO rooms (room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '501', rt.type_id, 5, 'AVAILABLE', rt.base_price
FROM room_types rt WHERE rt.type_name = 'Presidential Suite';

INSERT INTO rooms (room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '502', rt.type_id, 5, 'AVAILABLE', rt.base_price
FROM room_types rt WHERE rt.type_name = 'Presidential Suite';

INSERT INTO rooms (room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '503', rt.type_id, 5, 'AVAILABLE', rt.base_price
FROM room_types rt WHERE rt.type_name = 'Business Suite';

INSERT INTO rooms (room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '504', rt.type_id, 5, 'AVAILABLE', rt.base_price
FROM room_types rt WHERE rt.type_name = 'Business Suite';

INSERT INTO rooms (room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '505', rt.type_id, 5, 'AVAILABLE', rt.base_price
FROM room_types rt WHERE rt.type_name = 'Family Suite';

INSERT INTO rooms (room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '506', rt.type_id, 5, 'AVAILABLE', rt.base_price
FROM room_types rt WHERE rt.type_name = 'Family Suite';

INSERT INTO rooms (room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '507', rt.type_id, 5, 'AVAILABLE', rt.base_price
FROM room_types rt WHERE rt.type_name = 'Deluxe King';

INSERT INTO rooms (room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '508', rt.type_id, 5, 'AVAILABLE', rt.base_price
FROM room_types rt WHERE rt.type_name = 'Deluxe King';

INSERT INTO rooms (room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '509', rt.type_id, 5, 'AVAILABLE', rt.base_price
FROM room_types rt WHERE rt.type_name = 'Business Suite';

INSERT INTO rooms (room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '510', rt.type_id, 5, 'AVAILABLE', rt.base_price
FROM room_types rt WHERE rt.type_name = 'Business Suite';

-- Insert Customers (50 customers)
INSERT INTO customers (customer_id, first_name, last_name, email, phone, address, date_of_birth, total_spent, loyalty_points)
VALUES (customer_seq.NEXTVAL, 'Alice', 'Morgan', 'alice.morgan@example.com', '+1-555-1001', '123 Maple St, New York, NY 10001', DATE '1990-05-15', 2450.00, 245);

INSERT INTO customers (customer_id, first_name, last_name, email, phone, address, date_of_birth, total_spent, loyalty_points)
VALUES (customer_seq.NEXTVAL, 'Brian', 'Chen', 'brian.chen@example.com', '+1-555-1002', '45 Oak Ave, Los Angeles, CA 90210', DATE '1985-09-22', 8750.00, 875);

INSERT INTO customers (customer_id, first_name, last_name, email, phone, address, date_of_birth, total_spent, loyalty_points)
VALUES (customer_seq.NEXTVAL, 'Carla', 'Diaz', 'carla.diaz@example.com', '+1-555-1003', '9 Pine Rd, Chicago, IL 60601', DATE '1992-11-08', 12500.00, 1250);

INSERT INTO customers (customer_id, first_name, last_name, email, phone, address, date_of_birth, total_spent, loyalty_points)
VALUES (customer_seq.NEXTVAL, 'David', 'Edwards', 'david.edwards@example.com', '+1-555-1004', '77 Birch Blvd, Miami, FL 33101', DATE '1978-03-12', 3200.00, 320);

INSERT INTO customers (customer_id, first_name, last_name, email, phone, address, date_of_birth, total_spent, loyalty_points)
VALUES (customer_seq.NEXTVAL, 'Emma', 'Foster', 'emma.foster@example.com', '+1-555-1005', '5 Cedar Ct, Seattle, WA 98101', DATE '1995-07-25', 1800.00, 180);

INSERT INTO customers (customer_id, first_name, last_name, email, phone, address, date_of_birth, total_spent, loyalty_points)
VALUES (customer_seq.NEXTVAL, 'Farid', 'Ghani', 'farid.ghani@example.com', '+1-555-1006', '10 Elm Way, Boston, MA 02101', DATE '1988-01-18', 22500.00, 2250);

INSERT INTO customers (customer_id, first_name, last_name, email, phone, address, date_of_birth, total_spent, loyalty_points)
VALUES (customer_seq.NEXTVAL, 'Grace', 'Hopper', 'grace.hopper@example.com', '+1-555-1007', '1 Innovation Dr, San Francisco, CA 94102', DATE '1980-12-09', 6400.00, 640);

INSERT INTO customers (customer_id, first_name, last_name, email, phone, address, date_of_birth, total_spent, loyalty_points)
VALUES (customer_seq.NEXTVAL, 'Henry', 'Iverson', 'henry.iverson@example.com', '+1-555-1008', '42 Logic Ln, Austin, TX 78701', DATE '1991-02-28', 950.00, 95);

INSERT INTO customers (customer_id, first_name, last_name, email, phone, address, date_of_birth, total_spent, loyalty_points)
VALUES (customer_seq.NEXTVAL, 'Isla', 'Jones', 'isla.jones@example.com', '+1-555-1009', '88 Harbor Dr, Portland, OR 97201', DATE '1993-04-14', 2100.00, 210);

INSERT INTO customers (customer_id, first_name, last_name, email, phone, address, date_of_birth, total_spent, loyalty_points)
VALUES (customer_seq.NEXTVAL, 'Jack', 'Kumar', 'jack.kumar@example.com', '+1-555-1010', '301 Sunset Strip, Denver, CO 80201', DATE '1986-08-30', 15800.00, 1580);

INSERT INTO customers (customer_id, first_name, last_name, email, phone, address, date_of_birth, total_spent, loyalty_points)
VALUES (customer_seq.NEXTVAL, 'Karen', 'Lopez', 'karen.lopez@example.com', '+1-555-1011', '27 River Rd, Phoenix, AZ 85001', DATE '1989-06-05', 7300.00, 730);

INSERT INTO customers (customer_id, first_name, last_name, email, phone, address, date_of_birth, total_spent, loyalty_points)
VALUES (customer_seq.NEXTVAL, 'Luis', 'Martinez', 'luis.martinez@example.com', '+1-555-1012', '64 Beach Blvd, San Diego, CA 92101', DATE '1975-12-20', 4900.00, 490);

INSERT INTO customers (customer_id, first_name, last_name, email, phone, address, date_of_birth, total_spent, loyalty_points)
VALUES (customer_seq.NEXTVAL, 'Maria', 'Nguyen', 'maria.nguyen@example.com', '+1-555-1013', '19 Valley View, Las Vegas, NV 89101', DATE '1982-10-11', 11200.00, 1120);

INSERT INTO customers (customer_id, first_name, last_name, email, phone, address, date_of_birth, total_spent, loyalty_points)
VALUES (customer_seq.NEXTVAL, 'Noah', 'Okafor', 'noah.okafor@example.com', '+1-555-1014', '33 Mountain Dr, Salt Lake City, UT 84101', DATE '1994-03-07', 680.00, 68);

INSERT INTO customers (customer_id, first_name, last_name, email, phone, address, date_of_birth, total_spent, loyalty_points)
VALUES (customer_seq.NEXTVAL, 'Olivia', 'Peterson', 'olivia.peterson@example.com', '+1-555-1015', '520 Lake Rd, Minneapolis, MN 55401', DATE '1987-07-16', 9700.00, 970);

INSERT INTO customers (customer_id, first_name, last_name, email, phone, address, date_of_birth, total_spent, loyalty_points)
VALUES (customer_seq.NEXTVAL, 'Peter', 'Quinn', 'peter.quinn@example.com', '+1-555-1016', '72 Forest Ave, Atlanta, GA 30301', DATE '1981-05-23', 550.00, 55);

INSERT INTO customers (customer_id, first_name, last_name, email, phone, address, date_of_birth, total_spent, loyalty_points)
VALUES (customer_seq.NEXTVAL, 'Qing', 'Ramirez', 'qing.ramirez@example.com', '+1-555-1017', '8 Desert Way, Albuquerque, NM 87101', DATE '1993-09-02', 18200.00, 1820);

INSERT INTO customers (customer_id, first_name, last_name, email, phone, address, date_of_birth, total_spent, loyalty_points)
VALUES (customer_seq.NEXTVAL, 'Raj', 'Singh', 'raj.singh@example.com', '+1-555-1018', '105 Summit Ln, Nashville, TN 37201', DATE '1979-02-19', 5600.00, 560);

INSERT INTO customers (customer_id, first_name, last_name, email, phone, address, date_of_birth, total_spent, loyalty_points)
VALUES (customer_seq.NEXTVAL, 'Sara', 'Thomas', 'sara.thomas@example.com', '+1-555-1019', '47 Meadow Rd, Charlotte, NC 28201', DATE '1990-11-27', 1200.00, 120);

INSERT INTO customers (customer_id, first_name, last_name, email, phone, address, date_of_birth, total_spent, loyalty_points)
VALUES (customer_seq.NEXTVAL, 'Tyler', 'Underwood', 'tyler.underwood@example.com', '+1-555-1020', '93 Canyon Way, Oklahoma City, OK 73101', DATE '1984-04-03', 14300.00, 1430);

-- Continue with more customers...
INSERT INTO customers (customer_id, first_name, last_name, email, phone, address, date_of_birth, total_spent, loyalty_points)
VALUES (customer_seq.NEXTVAL, 'Uma', 'Vargas', 'uma.vargas@example.com', '+1-555-1021', '16 Prairie Ln, Kansas City, MO 64101', DATE '1997-08-14', 320.00, 32);

INSERT INTO customers (customer_id, first_name, last_name, email, phone, address, date_of_birth, total_spent, loyalty_points)
VALUES (customer_seq.NEXTVAL, 'Victor', 'Wong', 'victor.wong@example.com', '+1-555-1022', '58 Hilltop Dr, Sacramento, CA 95814', DATE '1976-10-09', 27800.00, 2780);

INSERT INTO customers (customer_id, first_name, last_name, email, phone, address, date_of_birth, total_spent, loyalty_points)
VALUES (customer_seq.NEXTVAL, 'Wendy', 'Xu', 'wendy.xu@example.com', '+1-555-1023', '39 Seaside Ave, Portland, ME 04101', DATE '1988-05-21', 3800.00, 380);

INSERT INTO customers (customer_id, first_name, last_name, email, phone, address, date_of_birth, total_spent, loyalty_points)
VALUES (customer_seq.NEXTVAL, 'Xavier', 'Yamamoto', 'xavier.yamamoto@example.com', '+1-555-1024', '81 Cliffside Rd, Honolulu, HI 96801', DATE '1983-01-13', 6100.00, 610);

INSERT INTO customers (customer_id, first_name, last_name, email, phone, address, date_of_birth, total_spent, loyalty_points)
VALUES (customer_seq.NEXTVAL, 'Yasmin', 'Zhang', 'yasmin.zhang@example.com', '+1-555-1025', '22 Highland Blvd, Richmond, VA 23219', DATE '1991-12-08', 9300.00, 930);

-- Insert Room Services
INSERT INTO room_services (service_id, service_name, description, price, category)
VALUES (room_service_seq.NEXTVAL, 'Room Service Breakfast', 'Continental breakfast delivered to room', 25.99, 'FOOD');

INSERT INTO room_services (service_id, service_name, description, price, category)
VALUES (room_service_seq.NEXTVAL, 'Gourmet Dinner', 'Three-course dinner from our executive chef', 85.99, 'FOOD');

INSERT INTO room_services (service_id, service_name, description, price, category)
VALUES (room_service_seq.NEXTVAL, 'Wine & Cheese Platter', 'Selection of premium wines and artisanal cheeses', 45.99, 'BEVERAGE');

INSERT INTO room_services (service_id, service_name, description, price, category)
VALUES (room_service_seq.NEXTVAL, 'Premium Bar Setup', 'In-room bar with top-shelf liquors', 120.00, 'BEVERAGE');

INSERT INTO room_services (service_id, service_name, description, price, category)
VALUES (room_service_seq.NEXTVAL, 'Laundry Service', 'Same-day laundry and dry cleaning', 15.99, 'LAUNDRY');

INSERT INTO room_services (service_id, service_name, description, price, category)
VALUES (room_service_seq.NEXTVAL, 'Express Cleaning', 'Deep room cleaning service', 35.00, 'CLEANING');

INSERT INTO room_services (service_id, service_name, description, price, category)
VALUES (room_service_seq.NEXTVAL, 'Spa Treatment', 'In-room massage and spa services', 150.00, 'GENERAL');

INSERT INTO room_services (service_id, service_name, description, price, category)
VALUES (room_service_seq.NEXTVAL, 'Business Center Access', '24-hour business center with printing', 20.00, 'BUSINESS');

INSERT INTO room_services (service_id, service_name, description, price, category)
VALUES (room_service_seq.NEXTVAL, 'Movie Package', 'Premium movie channels and snacks', 30.00, 'ENTERTAINMENT');

INSERT INTO room_services (service_id, service_name, description, price, category)
VALUES (room_service_seq.NEXTVAL, 'Concierge Service', 'Personal concierge for reservations and tours', 75.00, 'GENERAL');

-- Insert VIP Members (customers with high spending)
INSERT INTO vip_members (vip_id, customer_id, membership_level, discount_percentage, benefits, annual_fee)
SELECT vip_member_seq.NEXTVAL, c.customer_id, 'PLATINUM', 20,
       'All Gold benefits, Personal butler, Spa access, Priority reservations, Free airport transfer', 1000
FROM customers c WHERE c.total_spent >= 20000;

INSERT INTO vip_members (vip_id, customer_id, membership_level, discount_percentage, benefits, annual_fee)
SELECT vip_member_seq.NEXTVAL, c.customer_id, 'GOLD', 15,
       'All Silver benefits, Free airport transfer, Concierge service, Spa access', 500
FROM customers c WHERE c.total_spent >= 10000 AND c.total_spent < 20000;

INSERT INTO vip_members (vip_id, customer_id, membership_level, discount_percentage, benefits, annual_fee)
SELECT vip_member_seq.NEXTVAL, c.customer_id, 'SILVER', 10,
       'All Bronze benefits, Room upgrade (subject to availability), Free breakfast, Late checkout', 250
FROM customers c WHERE c.total_spent >= 5000 AND c.total_spent < 10000;

INSERT INTO vip_members (vip_id, customer_id, membership_level, discount_percentage, benefits, annual_fee)
SELECT vip_member_seq.NEXTVAL, c.customer_id, 'BRONZE', 5,
       'Free WiFi, Late checkout until 2 PM, Welcome drink, Priority customer service', 100
FROM customers c WHERE c.total_spent >= 2000 AND c.total_spent < 5000;

-- Insert Historical Bookings (CHECKED_OUT)
INSERT INTO bookings (booking_id, customer_id, room_id, check_in_date, check_out_date, booking_status, payment_status, total_amount, discount_applied, special_requests)
SELECT booking_seq.NEXTVAL, c.customer_id, r.room_id, SYSDATE - 30, SYSDATE - 25, 'CHECKED_OUT', 'PAID', 699.95, 0, 'Late checkout requested'
FROM customers c, rooms r
WHERE c.email = 'alice.morgan@example.com' AND r.room_number = '101'
AND ROWNUM = 1;

INSERT INTO bookings (booking_id, customer_id, room_id, check_in_date, check_out_date, booking_status, payment_status, total_amount, discount_applied, special_requests)
SELECT booking_seq.NEXTVAL, c.customer_id, r.room_id, SYSDATE - 25, SYSDATE - 20, 'CHECKED_OUT', 'PAID', 949.95, 94.99, 'VIP member discount applied'
FROM customers c, rooms r
WHERE c.email = 'brian.chen@example.com' AND r.room_number = '204'
AND ROWNUM = 1;

INSERT INTO bookings (booking_id, customer_id, room_id, check_in_date, check_out_date, booking_status, payment_status, total_amount, discount_applied, special_requests)
SELECT booking_seq.NEXTVAL, c.customer_id, r.room_id, SYSDATE - 20, SYSDATE - 15, 'CHECKED_OUT', 'PAID', 1349.95, 134.99, 'Flowers for anniversary'
FROM customers c, rooms r
WHERE c.email = 'carla.diaz@example.com' AND r.room_number = '301'
AND ROWNUM = 1;

INSERT INTO bookings (booking_id, customer_id, room_id, check_in_date, check_out_date, booking_status, payment_status, total_amount, discount_applied, special_requests)
SELECT booking_seq.NEXTVAL, c.customer_id, r.room_id, SYSDATE - 15, SYSDATE - 12, 'CHECKED_OUT', 'PAID', 419.97, 0, 'Extra towels'
FROM customers c, rooms r
WHERE c.email = 'david.edwards@example.com' AND r.room_number = '102'
AND ROWNUM = 1;

INSERT INTO bookings (booking_id, customer_id, room_id, check_in_date, check_out_date, booking_status, payment_status, total_amount, discount_applied, special_requests)
SELECT booking_seq.NEXTVAL, c.customer_id, r.room_id, SYSDATE - 12, SYSDATE - 10, 'CHECKED_OUT', 'PAID', 279.98, 0, 'Quiet room please'
FROM customers c, rooms r
WHERE c.email = 'emma.foster@example.com' AND r.room_number = '103'
AND ROWNUM = 1;

-- Insert Current Bookings (CHECKED_IN)
INSERT INTO bookings (booking_id, customer_id, room_id, check_in_date, check_out_date, booking_status, payment_status, total_amount, discount_applied, actual_check_in, special_requests)
SELECT booking_seq.NEXTVAL, c.customer_id, r.room_id, SYSDATE - 2, SYSDATE + 3, 'CHECKED_IN', 'PAID', 949.95, 94.99, SYSTIMESTAMP - 2, 'Business traveler'
FROM customers c, rooms r
WHERE c.email = 'farid.ghani@example.com' AND r.room_number = '202'
AND ROWNUM = 1;

INSERT INTO bookings (booking_id, customer_id, room_id, check_in_date, check_out_date, booking_status, payment_status, total_amount, discount_applied, actual_check_in, special_requests)
SELECT booking_seq.NEXTVAL, c.customer_id, r.room_id, SYSDATE - 1, SYSDATE + 2, 'CHECKED_IN', 'PAID', 809.97, 0, SYSTIMESTAMP - 1, 'Tech conference attendee'
FROM customers c, rooms r
WHERE c.email = 'grace.hopper@example.com' AND r.room_number = '301'
AND ROWNUM = 1;

INSERT INTO bookings (booking_id, customer_id, room_id, check_in_date, check_out_date, booking_status, payment_status, total_amount, discount_applied, actual_check_in, special_requests)
SELECT booking_seq.NEXTVAL, c.customer_id, r.room_id, SYSDATE - 3, SYSDATE + 1, 'CHECKED_IN', 'PAID', 1079.96, 107.99, SYSTIMESTAMP - 3, 'Family vacation'
FROM customers c, rooms r
WHERE c.email = 'jack.kumar@example.com' AND r.room_number = '305'
AND ROWNUM = 1;

-- Insert Future Bookings (CONFIRMED)
INSERT INTO bookings (booking_id, customer_id, room_id, check_in_date, check_out_date, booking_status, payment_status, total_amount, discount_applied, special_requests)
SELECT booking_seq.NEXTVAL, c.customer_id, r.room_id, SYSDATE + 5, SYSDATE + 8, 'CONFIRMED', 'PENDING', 419.97, 0, 'Business meeting'
FROM customers c, rooms r
WHERE c.email = 'henry.iverson@example.com' AND r.room_number = '106'
AND ROWNUM = 1;

INSERT INTO bookings (booking_id, customer_id, room_id, check_in_date, check_out_date, booking_status, payment_status, total_amount, discount_applied, special_requests)
SELECT booking_seq.NEXTVAL, c.customer_id, r.room_id, SYSDATE + 10, SYSDATE + 15, 'CONFIRMED', 'PENDING', 1349.95, 0, 'Wedding anniversary'
FROM customers c, rooms r
WHERE c.email = 'isla.jones@example.com' AND r.room_number = '302'
AND ROWNUM = 1;

INSERT INTO bookings (booking_id, customer_id, room_id, check_in_date, check_out_date, booking_status, payment_status, total_amount, discount_applied, special_requests)
SELECT booking_seq.NEXTVAL, c.customer_id, r.room_id, SYSDATE + 7, SYSDATE + 12, 'CONFIRMED', 'PENDING', 1499.95, 149.99, 'Executive retreat'
FROM customers c, rooms r
WHERE c.email = 'qing.ramirez@example.com' AND r.room_number = '401'
AND ROWNUM = 1;

INSERT INTO bookings (booking_id, customer_id, room_id, check_in_date, check_out_date, booking_status, payment_status, total_amount, discount_applied, special_requests)
SELECT booking_seq.NEXTVAL, c.customer_id, r.room_id, SYSDATE + 20, SYSDATE + 25, 'CONFIRMED', 'PENDING', 2999.95, 599.99, 'Luxury vacation'
FROM customers c, rooms r
WHERE c.email = 'victor.wong@example.com' AND r.room_number = '501'
AND ROWNUM = 1;

-- Add more bookings for better data variety
INSERT INTO bookings (booking_id, customer_id, room_id, check_in_date, check_out_date, booking_status, payment_status, total_amount, discount_applied, special_requests)
SELECT booking_seq.NEXTVAL, c.customer_id, r.room_id, SYSDATE + 3, SYSDATE + 6, 'CONFIRMED', 'PENDING', 569.97, 0, 'Weekend getaway'
FROM customers c, rooms r
WHERE c.email = 'karen.lopez@example.com' AND r.room_number = '205'
AND ROWNUM = 1;

INSERT INTO bookings (booking_id, customer_id, room_id, check_in_date, check_out_date, booking_status, payment_status, total_amount, discount_applied, special_requests)
SELECT booking_seq.NEXTVAL, c.customer_id, r.room_id, SYSDATE + 15, SYSDATE + 18, 'CONFIRMED', 'PENDING', 899.97, 89.99, 'Conference speaker'
FROM customers c, rooms r
WHERE c.email = 'maria.nguyen@example.com' AND r.room_number = '402'
AND ROWNUM = 1;

COMMIT;
