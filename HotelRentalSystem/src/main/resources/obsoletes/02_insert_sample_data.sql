-- CONSOLIDATED SAMPLE DATA (Aligned with consolidated schema)
-- Run AFTER 01_create_tables.sql

-- Room Types
INSERT INTO room_types(type_id, type_name, base_price, max_occupancy, amenities, description)
VALUES (room_type_seq.NEXTVAL,'STANDARD',100,2,'WiFi,TV','Standard room');
INSERT INTO room_types(type_id, type_name, base_price, max_occupancy, amenities, description)
VALUES (room_type_seq.NEXTVAL,'DELUXE',180,3,'WiFi,TV,MiniBar','Deluxe room');
INSERT INTO room_types(type_id, type_name, base_price, max_occupancy, amenities, description)
VALUES (room_type_seq.NEXTVAL,'SUITE',300,4,'WiFi,TV,MiniBar,Balcony','Suite');

-- Rooms (one per type x a few)
INSERT INTO rooms(room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '10'||ROWNUM, type_id, 1, 'AVAILABLE', base_price FROM room_types;
INSERT INTO rooms(room_id, room_number, type_id, floor_number, status, base_price)
SELECT room_seq.NEXTVAL, '20'||ROWNUM, type_id, 2, 'AVAILABLE', base_price FROM room_types;

-- Customers
INSERT INTO customers(customer_id, first_name, last_name, email, phone, address, total_spent, loyalty_points)
VALUES (customer_seq.NEXTVAL,'John','Doe','john@example.com','123456','Address 1',0,0);
INSERT INTO customers(customer_id, first_name, last_name, email, phone, address, total_spent, loyalty_points)
VALUES (customer_seq.NEXTVAL,'Jane','Smith','jane@example.com','987654','Address 2',6000,150);
INSERT INTO customers(customer_id, first_name, last_name, email, phone, address, total_spent, loyalty_points)
VALUES (customer_seq.NEXTVAL,'Alice','Brown','alice@example.com','555111','Address 3',12000,500);

-- VIP Members (for high spenders)
INSERT INTO vip_members(vip_id, customer_id, membership_level, discount_percentage, benefits)
SELECT vip_seq.NEXTVAL, c.customer_id,
       CASE WHEN c.total_spent >= 10000 THEN 'PLATINUM' ELSE 'GOLD' END,
       CASE WHEN c.total_spent >= 10000 THEN 15 ELSE 10 END,
       'Priority Booking, Late Checkout'
FROM customers c WHERE c.total_spent >= 5000;

-- One CONFIRMED future booking, one CHECKED_IN, one CHECKED_OUT
INSERT INTO bookings(booking_id, customer_id, room_id, check_in_date, check_out_date, total_amount, booking_status, payment_status)
VALUES (booking_seq.NEXTVAL, 1, (SELECT MIN(room_id) FROM rooms), SYSDATE+1, SYSDATE+3, 300, 'CONFIRMED','PENDING');
INSERT INTO bookings(booking_id, customer_id, room_id, check_in_date, check_out_date, total_amount, booking_status, payment_status, actual_check_in)
VALUES (booking_seq.NEXTVAL, 2, (SELECT MIN(room_id) FROM rooms WHERE rownum=1)+1, SYSDATE-1, SYSDATE+1, 400, 'CHECKED_IN','PENDING', SYSDATE-1);
INSERT INTO bookings(booking_id, customer_id, room_id, check_in_date, check_out_date, total_amount, booking_status, payment_status, actual_check_in, actual_check_out)
VALUES (booking_seq.NEXTVAL, 3, (SELECT MIN(room_id) FROM rooms WHERE rownum=1)+2, SYSDATE-10, SYSDATE-7, 900, 'CHECKED_OUT','PAID', SYSDATE-10, SYSDATE-7);

-- Room Services
INSERT INTO room_services(service_id, service_name, service_category, base_price) VALUES (room_service_seq.NEXTVAL,'Room Cleaning','HOUSEKEEPING',25);
INSERT INTO room_services(service_id, service_name, service_category, base_price) VALUES (room_service_seq.NEXTVAL,'Breakfast','FOOD',35);
INSERT INTO room_services(service_id, service_name, service_category, base_price) VALUES (room_service_seq.NEXTVAL,'Late Checkout','ACCOMMODATION',30);

-- Assign all services to all room types (simple mapping)
INSERT INTO room_service_assignments(assignment_id, room_type_id, service_id, is_complimentary)
SELECT assignment_seq.NEXTVAL, rt.type_id, rs.service_id,
       CASE WHEN rs.service_name='Room Cleaning' THEN 'Y' ELSE 'N' END
FROM room_types rt CROSS JOIN room_services rs;

-- Sample service usage for CHECKED_IN booking
INSERT INTO customer_service_usage(usage_id, booking_id, customer_id, service_id, quantity, unit_price, total_cost)
SELECT usage_seq.NEXTVAL,
       (SELECT booking_id FROM bookings WHERE booking_status='CHECKED_IN' AND ROWNUM=1),
       2,
       (SELECT service_id FROM room_services WHERE service_name='Breakfast'),
       2, 35, 70 FROM dual;

-- Sample invoice for CHECKED_OUT booking
DECLARE
  v_booking NUMBER; v_invoice NUMBER; v_sub NUMBER; v_tax NUMBER; v_total NUMBER;
BEGIN
  SELECT booking_id INTO v_booking FROM bookings WHERE booking_status='CHECKED_OUT' AND ROWNUM=1;
  v_sub := 900; v_tax := v_sub*0.1; v_total := v_sub+v_tax;
  INSERT INTO invoices(invoice_id, booking_id, customer_id, invoice_number, subtotal, tax_amount, total_amount, payment_status)
  SELECT invoice_seq.NEXTVAL, b.booking_id, b.customer_id, 'INV-'||TO_CHAR(SYSDATE,'YYYYMMDD')||'-'||LPAD(invoice_seq.CURRVAL,4,'0'), v_sub, v_tax, v_total,'PAID'
  FROM bookings b WHERE b.booking_id = v_booking;
  INSERT INTO invoice_line_items(line_item_id, invoice_id, item_type, item_description, quantity, unit_price, line_total)
  VALUES (line_item_seq.NEXTVAL, invoice_seq.CURRVAL,'ROOM','Room Charge',1,v_sub,v_sub);
  INSERT INTO invoice_line_items(line_item_id, invoice_id, item_type, item_description, quantity, unit_price, line_total)
  VALUES (line_item_seq.NEXTVAL, invoice_seq.CURRVAL,'TAX','Tax 10%',1,v_tax,v_tax);
END;
/

COMMIT;

-- Verification
SELECT 'ROOM_TYPES='||COUNT(*) FROM room_types;
SELECT 'ROOMS='||COUNT(*) FROM rooms;
SELECT 'CUSTOMERS='||COUNT(*) FROM customers;
SELECT 'VIP_MEMBERS='||COUNT(*) FROM vip_members;
SELECT 'BOOKINGS='||COUNT(*) FROM bookings;

-- EXTENDED DATASET (Added)
-- Optional wipe block (commented out)
-- DELETE FROM invoice_line_items; DELETE FROM invoices; DELETE FROM customer_service_usage; DELETE FROM room_service_assignments; DELETE FROM room_services; DELETE FROM bookings; DELETE FROM vip_members; DELETE FROM customers; DELETE FROM rooms; DELETE FROM room_types; COMMIT;

------------------------------------------------------------
-- Additional ROOM TYPES (idempotent)
------------------------------------------------------------
MERGE INTO room_types rt USING (
  SELECT 'Standard Queen' type_name,120 base_price,2 max_occ,'WiFi;TV;Desk' amenities,'Cozy standard queen room' descr FROM dual UNION ALL
  SELECT 'Standard Twin',115,2,'WiFi;TV','Twin beds for business travelers' FROM dual UNION ALL
  SELECT 'Deluxe King',185,2,'WiFi;4K TV;Mini Bar;Sofa','Spacious deluxe king' FROM dual UNION ALL
  SELECT 'Family Suite',250,5,'WiFi;2 TVs;Kitchenette;Sofa Bed','Ideal for families' FROM dual UNION ALL
  SELECT 'Executive Suite',320,3,'WiFi;4K TV;Office Desk;Mini Bar;Coffee Machine','Suite for executives' FROM dual UNION ALL
  SELECT 'Presidential Suite',600,4,'Private Lounge;Butler;Spa Bath;Panoramic View','Top tier luxury' FROM dual
) src ON (rt.type_name = src.type_name)
WHEN NOT MATCHED THEN INSERT (type_id,type_name,base_price,max_occupancy,amenities,description,created_date)
VALUES (room_type_seq.NEXTVAL,src.type_name,src.base_price,src.max_occ,src.amenities,src.descr,SYSDATE);
COMMIT;

------------------------------------------------------------
-- Additional ROOMS (idempotent per room_number)
------------------------------------------------------------
INSERT INTO rooms(room_id,room_number,type_id,floor_number,status,base_price,created_date)
SELECT room_seq.NEXTVAL,'101',(SELECT type_id FROM room_types WHERE type_name='Standard Queen'),1,'AVAILABLE',120,SYSDATE FROM dual
WHERE NOT EXISTS (SELECT 1 FROM rooms WHERE room_number='101');
INSERT INTO rooms(room_id,room_number,type_id,floor_number,status,base_price,created_date)
SELECT room_seq.NEXTVAL,'102',(SELECT type_id FROM room_types WHERE type_name='Standard Queen'),1,'AVAILABLE',120,SYSDATE FROM dual
WHERE NOT EXISTS (SELECT 1 FROM rooms WHERE room_number='102');
INSERT INTO rooms(room_id,room_number,type_id,floor_number,status,base_price,created_date)
SELECT room_seq.NEXTVAL,'103',(SELECT type_id FROM room_types WHERE type_name='Standard Twin'),1,'AVAILABLE',115,SYSDATE FROM dual
WHERE NOT EXISTS (SELECT 1 FROM rooms WHERE room_number='103');
INSERT INTO rooms(room_id,room_number,type_id,floor_number,status,base_price,created_date)
SELECT room_seq.NEXTVAL,'104',(SELECT type_id FROM room_types WHERE type_name='Standard Twin'),1,'MAINTENANCE',115,SYSDATE FROM dual
WHERE NOT EXISTS (SELECT 1 FROM rooms WHERE room_number='104');
INSERT INTO rooms(room_id,room_number,type_id,floor_number,status,base_price,created_date)
SELECT room_seq.NEXTVAL,'201',(SELECT type_id FROM room_types WHERE type_name='Deluxe King'),2,'AVAILABLE',185,SYSDATE FROM dual
WHERE NOT EXISTS (SELECT 1 FROM rooms WHERE room_number='201');
INSERT INTO rooms(room_id,room_number,type_id,floor_number,status,base_price,created_date)
SELECT room_seq.NEXTVAL,'202',(SELECT type_id FROM room_types WHERE type_name='Deluxe King'),2,'OCCUPIED',185,SYSDATE FROM dual
WHERE NOT EXISTS (SELECT 1 FROM rooms WHERE room_number='202');
INSERT INTO rooms(room_id,room_number,type_id,floor_number,status,base_price,created_date)
SELECT room_seq.NEXTVAL,'203',(SELECT type_id FROM room_types WHERE type_name='Family Suite'),2,'AVAILABLE',250,SYSDATE FROM dual
WHERE NOT EXISTS (SELECT 1 FROM rooms WHERE room_number='203');
INSERT INTO rooms(room_id,room_number,type_id,floor_number,status,base_price,created_date)
SELECT room_seq.NEXTVAL,'204',(SELECT type_id FROM room_types WHERE type_name='Family Suite'),2,'AVAILABLE',250,SYSDATE FROM dual
WHERE NOT EXISTS (SELECT 1 FROM rooms WHERE room_number='204');
INSERT INTO rooms(room_id,room_number,type_id,floor_number,status,base_price,created_date)
SELECT room_seq.NEXTVAL,'301',(SELECT type_id FROM room_types WHERE type_name='Executive Suite'),3,'AVAILABLE',320,SYSDATE FROM dual
WHERE NOT EXISTS (SELECT 1 FROM rooms WHERE room_number='301');
INSERT INTO rooms(room_id,room_number,type_id,floor_number,status,base_price,created_date)
SELECT room_seq.NEXTVAL,'302',(SELECT type_id FROM room_types WHERE type_name='Executive Suite'),3,'AVAILABLE',320,SYSDATE FROM dual
WHERE NOT EXISTS (SELECT 1 FROM rooms WHERE room_number='302');
INSERT INTO rooms(room_id,room_number,type_id,floor_number,status,base_price,created_date)
SELECT room_seq.NEXTVAL,'401',(SELECT type_id FROM room_types WHERE type_name='Presidential Suite'),4,'AVAILABLE',600,SYSDATE FROM dual
WHERE NOT EXISTS (SELECT 1 FROM rooms WHERE room_number='401');
DECLARE
  v_type NUMBER;
BEGIN
  SELECT type_id INTO v_type FROM room_types WHERE type_name='Standard Queen';
  FOR i IN 105..118 LOOP
    INSERT INTO rooms(room_id,room_number,type_id,floor_number,status,base_price,created_date)
    SELECT room_seq.NEXTVAL, TO_CHAR(i), v_type, CEIL(i/100), 'AVAILABLE',120,SYSDATE FROM dual
    WHERE NOT EXISTS (SELECT 1 FROM rooms WHERE room_number=TO_CHAR(i));
  END LOOP;
END;
/
COMMIT;

------------------------------------------------------------
-- Additional CUSTOMERS (idempotent by email)
------------------------------------------------------------
MERGE INTO customers c USING (
  SELECT 'alice.morgan@example.com' email,'Alice' fn,'Morgan' ln,'+1-555-1001' phone,'123 Maple St' addr,'1990-05-04' dob FROM dual UNION ALL
  SELECT 'brian.chen@example.com','Brian','Chen','+1-555-1002','45 Oak Ave','1985-09-14' FROM dual UNION ALL
  SELECT 'carla.diaz@example.com','Carla','Diaz','+1-555-1003','9 Pine Rd','1992-11-30' FROM dual UNION ALL
  SELECT 'david.edwards@example.com','David','Edwards','+1-555-1004','77 Birch Blvd','1978-03-22' FROM dual UNION ALL
  SELECT 'emma.foster@example.com','Emma','Foster','+1-555-1005','5 Cedar Ct','1995-07-17' FROM dual UNION ALL
  SELECT 'farid.ghani@example.com','Farid','Ghani','+1-555-1006','10 Elm Way','1988-01-10' FROM dual UNION ALL
  SELECT 'grace.hopper@example.com','Grace','Hopper','+1-555-1007','1 Innovate Pl','1980-12-09' FROM dual UNION ALL
  SELECT 'henry.iverson@example.com','Henry','Iverson','+1-555-1008','42 Logic Ln','1991-02-18' FROM dual UNION ALL
  SELECT 'isla.jones@example.com','Isla','Jones','+1-555-1009','88 Harbor Dr','1993-04-02' FROM dual UNION ALL
  SELECT 'jack.kumar@example.com','Jack','Kumar','+1-555-1010','301 Sunset Strip','1986-08-25' FROM dual
) src ON (c.email = src.email)
WHEN NOT MATCHED THEN INSERT (customer_id,first_name,last_name,email,phone,address,date_of_birth,total_spent,loyalty_points,created_date)
VALUES (customer_seq.NEXTVAL,src.fn,src.ln,src.email,src.phone,src.addr,TO_DATE(src.dob,'YYYY-MM-DD'),0,0,SYSDATE);
-- Generated test customers (skip if exists)
DECLARE
  v_email VARCHAR2(100);
BEGIN
  FOR i IN 11..40 LOOP
    v_email := LOWER('testfirst'||i)||'.'||LOWER('testlast'||i)||'@example.com';
    INSERT INTO customers(customer_id,first_name,last_name,email,phone,address,date_of_birth)
    SELECT customer_seq.NEXTVAL,'TestFirst'||i,'TestLast'||i,v_email,'+1-555-2'||TO_CHAR(100+i),'AutoGen Address',TO_DATE('1990-01-01','YYYY-MM-DD') + MOD(i,365) FROM dual
    WHERE NOT EXISTS (SELECT 1 FROM customers WHERE email=v_email);
  END LOOP;
END;
/
COMMIT;

------------------------------------------------------------
-- Additional ROOM SERVICES (idempotent by service_name)
------------------------------------------------------------
MERGE INTO room_services rs USING (
  SELECT 'Breakfast Buffet' name,'Full hot breakfast' descr,'FOOD' cat,18 price FROM dual UNION ALL
  SELECT 'Airport Pickup','Private car airport transfer','TRANSPORT',55 FROM dual UNION ALL
  SELECT 'Laundry Service','Per load wash & fold','LAUNDRY',25 FROM dual UNION ALL
  SELECT 'Spa Massage 60m','One hour massage','SPA',90 FROM dual UNION ALL
  SELECT 'In-Room Dining','Room service order','FOOD',12 FROM dual
) src ON (rs.service_name = src.name)
WHEN NOT MATCHED THEN INSERT (service_id,service_name,service_description,service_category,base_price,is_active,created_date)
VALUES (room_service_seq.NEXTVAL,src.name,src.descr,src.cat,src.price,'Y',SYSDATE);
COMMIT;

------------------------------------------------------------
-- Additional BOOKINGS (made idempotent)
------------------------------------------------------------
-- Alice historic stay
INSERT INTO bookings(booking_id,customer_id,room_id,check_in_date,check_out_date,total_amount,discount_applied,extra_charges,payment_status,booking_status,created_by)
SELECT booking_seq.NEXTVAL,
       (SELECT customer_id FROM customers WHERE email='alice.morgan@example.com'),
       (SELECT room_id FROM rooms WHERE room_number='101'),
       TO_DATE('2025-08-25','YYYY-MM-DD'),
       TO_DATE('2025-08-28','YYYY-MM-DD'),
       120*3,0,0,'PAID','CHECKED_OUT',USER
FROM dual
WHERE NOT EXISTS (
  SELECT 1 FROM bookings b JOIN customers c ON b.customer_id=c.customer_id
  WHERE c.email='alice.morgan@example.com'
    AND b.check_in_date=TO_DATE('2025-08-25','YYYY-MM-DD')
    AND b.check_out_date=TO_DATE('2025-08-28','YYYY-MM-DD')
    AND b.room_id=(SELECT room_id FROM rooms WHERE room_number='101')
);
-- Brian current booking
INSERT INTO bookings
SELECT booking_seq.NEXTVAL,
       (SELECT customer_id FROM customers WHERE email='brian.chen@example.com'),
       (SELECT room_id FROM rooms WHERE room_number='201'),
       TO_DATE('2025-08-28','YYYY-MM-DD'),
       TO_DATE('2025-09-02','YYYY-MM-DD'),
       185*5,0,0,'PENDING','CONFIRMED',USER
FROM dual
WHERE NOT EXISTS (
  SELECT 1 FROM bookings b JOIN customers c ON b.customer_id=c.customer_id
  WHERE c.email='brian.chen@example.com'
    AND b.check_in_date=TO_DATE('2025-08-28','YYYY-MM-DD')
    AND b.check_out_date=TO_DATE('2025-09-02','YYYY-MM-DD')
    AND b.room_id=(SELECT room_id FROM rooms WHERE room_number='201')
);
-- Carla future booking
INSERT INTO bookings
SELECT booking_seq.NEXTVAL,
       (SELECT customer_id FROM customers WHERE email='carla.diaz@example.com'),
       (SELECT room_id FROM rooms WHERE room_number='203'),
       TO_DATE('2025-09-10','YYYY-MM-DD'),
       TO_DATE('2025-09-15','YYYY-MM-DD'),
       250*5,0,0,'PENDING','CONFIRMED',USER
FROM dual
WHERE NOT EXISTS (
  SELECT 1 FROM bookings b JOIN customers c ON b.customer_id=c.customer_id
  WHERE c.email='carla.diaz@example.com'
    AND b.check_in_date=TO_DATE('2025-09-10','YYYY-MM-DD')
    AND b.check_out_date=TO_DATE('2025-09-15','YYYY-MM-DD')
    AND b.room_id=(SELECT room_id FROM rooms WHERE room_number='203')
);
-- Generated test bookings (skip duplicates)
DECLARE
  v_cust NUMBER;
  v_room NUMBER;
  v_start DATE := TO_DATE('2025-09-01','YYYY-MM-DD');
  v_len NUMBER;
  v_check_in DATE;
  v_check_out DATE;
BEGIN
  FOR i IN 0..9 LOOP
    SELECT customer_id INTO v_cust FROM (
      SELECT customer_id, ROW_NUMBER() OVER (ORDER BY customer_id) rn
      FROM customers WHERE email LIKE 'testfirst%') WHERE rn = i+1;
    SELECT room_id INTO v_room FROM (
      SELECT room_id, ROW_NUMBER() OVER (ORDER BY room_id) rn FROM rooms WHERE status='AVAILABLE'
    ) WHERE rn = MOD(i,10)+1;
    v_len := 2 + MOD(i,4);
    v_check_in := v_start + i;
    v_check_out := v_start + i + v_len;
    -- Only insert if not already present
    INSERT INTO bookings(booking_id,customer_id,room_id,check_in_date,check_out_date,total_amount,payment_status,booking_status)
    SELECT booking_seq.NEXTVAL,v_cust,v_room,v_check_in,v_check_out,120*v_len,'PENDING','CONFIRMED' FROM dual
    WHERE NOT EXISTS (
      SELECT 1 FROM bookings b
      WHERE b.customer_id = v_cust
        AND b.room_id = v_room
        AND b.check_in_date = v_check_in
        AND b.check_out_date = v_check_out
    );
  END LOOP;
END;
/
COMMIT;

------------------------------------------------------------
-- Additional SERVICE USAGE (idempotent)
------------------------------------------------------------
INSERT INTO customer_service_usage(usage_id,booking_id,customer_id,service_id,usage_date,quantity,unit_price,total_cost)
SELECT usage_seq.NEXTVAL,
       (SELECT MIN(booking_id) FROM bookings b JOIN customers c ON b.customer_id=c.customer_id WHERE c.email='alice.morgan@example.com' AND b.check_in_date=TO_DATE('2025-08-25','YYYY-MM-DD')),
       (SELECT customer_id FROM customers WHERE email='alice.morgan@example.com'),
       (SELECT service_id FROM room_services WHERE service_name='Breakfast Buffet'),
       TO_DATE('2025-08-26','YYYY-MM-DD'),2,18,36 FROM dual
WHERE NOT EXISTS (
  SELECT 1 FROM customer_service_usage u JOIN customers c ON u.customer_id=c.customer_id
  WHERE c.email='alice.morgan@example.com'
    AND u.usage_date=TO_DATE('2025-08-26','YYYY-MM-DD')
    AND u.service_id=(SELECT service_id FROM room_services WHERE service_name='Breakfast Buffet')
);
INSERT INTO customer_service_usage
SELECT usage_seq.NEXTVAL,
       (SELECT booking_id FROM bookings b JOIN customers c ON b.customer_id=c.customer_id WHERE c.email='brian.chen@example.com' AND b.check_in_date=TO_DATE('2025-08-28','YYYY-MM-DD') AND ROWNUM=1),
       (SELECT customer_id FROM customers WHERE email='brian.chen@example.com'),
       (SELECT service_id FROM room_services WHERE service_name='Spa Massage 60m'),
       TO_DATE('2025-08-29','YYYY-MM-DD'),1,90,90 FROM dual
WHERE NOT EXISTS (
  SELECT 1 FROM customer_service_usage u JOIN customers c ON u.customer_id=c.customer_id
  WHERE c.email='brian.chen@example.com'
    AND u.usage_date=TO_DATE('2025-08-29','YYYY-MM-DD')
    AND u.service_id=(SELECT service_id FROM room_services WHERE service_name='Spa Massage 60m')
);
COMMIT;

------------------------------------------------------------
-- Additional INVOICE & LINE ITEMS (idempotent)
------------------------------------------------------------
-- Create invoice only if none exists yet for Alice's checked out booking
INSERT INTO invoices(invoice_id,booking_id,customer_id,invoice_number,invoice_date,due_date,subtotal,tax_amount,discount_amount,total_amount,payment_status,payment_method,created_by)
SELECT invoice_seq.NEXTVAL,
       b.booking_id,
       b.customer_id,
       'INV-'||TO_CHAR(invoice_seq.CURRVAL,'FM00000'),
       SYSDATE,
       SYSDATE+15,
       b.total_amount + (SELECT NVL(SUM(total_cost),0) FROM customer_service_usage u WHERE u.booking_id=b.booking_id),
       ROUND((b.total_amount)*0.08,2),
       0,
       b.total_amount + (SELECT NVL(SUM(total_cost),0) FROM customer_service_usage u WHERE u.booking_id=b.booking_id) + ROUND((b.total_amount)*0.08,2),
       'PAID','CARD',USER
FROM bookings b
JOIN customers c ON b.customer_id=c.customer_id
WHERE c.email='alice.morgan@example.com'
  AND b.booking_status='CHECKED_OUT'
  AND NOT EXISTS (SELECT 1 FROM invoices i WHERE i.booking_id=b.booking_id)
  AND ROWNUM=1;
-- Insert line items only if invoice exists and no line items yet
DECLARE
  v_invoice NUMBER;
  v_booking NUMBER;
  v_start DATE;
  v_end DATE;
  v_price NUMBER;
  v_line_count NUMBER;
BEGIN
  SELECT inv.invoice_id, inv.booking_id INTO v_invoice, v_booking FROM invoices inv
    JOIN bookings b ON inv.booking_id=b.booking_id
    JOIN customers c ON b.customer_id=c.customer_id
    WHERE c.email='alice.morgan@example.com' AND b.booking_status='CHECKED_OUT' AND ROWNUM=1;
  SELECT COUNT(*) INTO v_line_count FROM invoice_line_items WHERE invoice_id=v_invoice;
  IF v_line_count = 0 THEN
    SELECT check_in_date, check_out_date, total_amount/(check_out_date - check_in_date)
      INTO v_start, v_end, v_price
    FROM bookings WHERE booking_id=v_booking;
    FOR d IN 0..(v_end - v_start -1) LOOP
      INSERT INTO invoice_line_items(line_item_id,invoice_id,item_type,item_description,quantity,unit_price,line_total)
      VALUES (line_item_seq.NEXTVAL,v_invoice,'ROOM','Room Night '||TO_CHAR(v_start + d,'YYYY-MM-DD'),1,v_price,v_price);
    END LOOP;
    INSERT INTO invoice_line_items(line_item_id,invoice_id,item_type,item_description,quantity,unit_price,line_total,service_id,usage_id)
    SELECT line_item_seq.NEXTVAL,v_invoice,'SERVICE','Breakfast Buffet (2)',2,18,36,
           (SELECT service_id FROM room_services WHERE service_name='Breakfast Buffet'),
           (SELECT usage_id FROM customer_service_usage u WHERE u.booking_id=v_booking AND ROWNUM=1) FROM dual;
    INSERT INTO invoice_line_items(line_item_id,invoice_id,item_type,item_description,quantity,unit_price,line_total)
    SELECT line_item_seq.NEXTVAL,v_invoice,'TAX','Room Tax 8%',1,
           ROUND((SELECT subtotal FROM invoices WHERE invoice_id=v_invoice) -
                 (SELECT discount_amount FROM invoices WHERE invoice_id=v_invoice) -
                 (SELECT tax_amount FROM invoices WHERE invoice_id=v_invoice),2),
           (SELECT tax_amount FROM invoices WHERE invoice_id=v_invoice) FROM dual;
  END IF;
END;
/
COMMIT;

-- End Extended Dataset
