-- ======================================================
-- Hotel Reservation System - Stored Procedures and Functions
-- File: 02_procedures.sql
-- Purpose: Create all stored procedures and functions
-- ======================================================

-- ======================================================
-- UTILITY FUNCTIONS
-- ======================================================

-- Calculate stay duration in days
CREATE OR REPLACE FUNCTION calculate_stay_days(
    p_check_in DATE,
    p_check_out DATE
) RETURN NUMBER AS
BEGIN
    RETURN p_check_out - p_check_in;
END calculate_stay_days;
/

-- Calculate room price for a stay with VIP discount
CREATE OR REPLACE FUNCTION calculate_room_price(
    p_room_id IN NUMBER,
    p_check_in IN DATE,
    p_check_out IN DATE,
    p_is_vip IN CHAR DEFAULT 'N',
    p_discount_pct IN NUMBER DEFAULT 0
) RETURN NUMBER AS
    v_base_price NUMBER;
    v_days NUMBER;
    v_total NUMBER;
    v_discount NUMBER := 0;
BEGIN
    -- Get room base price
    SELECT base_price INTO v_base_price
    FROM rooms
    WHERE room_id = p_room_id;

    -- Calculate number of days
    v_days := p_check_out - p_check_in;

    -- Calculate total
    v_total := v_base_price * v_days;

    -- Apply discount if applicable
    IF p_is_vip = 'Y' AND p_discount_pct > 0 THEN
        v_discount := v_total * (p_discount_pct / 100);
        v_total := v_total - v_discount;
    END IF;

    RETURN v_total;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RETURN 0;
END calculate_room_price;
/

-- Check if customer is VIP
CREATE OR REPLACE FUNCTION is_customer_vip(
    p_customer_id IN NUMBER
) RETURN CHAR AS
    v_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_count
    FROM vip_members
    WHERE customer_id = p_customer_id
    AND is_active = 'Y';

    IF v_count > 0 THEN
        RETURN 'Y';
    ELSE
        RETURN 'N';
    END IF;
END is_customer_vip;
/

-- Get VIP discount percentage
CREATE OR REPLACE FUNCTION get_vip_discount(
    p_customer_id IN NUMBER
) RETURN NUMBER AS
    v_discount NUMBER;
BEGIN
    SELECT NVL(discount_percentage, 0) INTO v_discount
    FROM vip_members
    WHERE customer_id = p_customer_id
    AND is_active = 'Y';

    RETURN v_discount;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RETURN 0;
END get_vip_discount;
/

-- ======================================================
-- BOOKING MANAGEMENT PROCEDURES
-- ======================================================

-- Create a new booking
CREATE OR REPLACE PROCEDURE create_booking(
    p_customer_id IN NUMBER,
    p_room_id IN NUMBER,
    p_check_in_date IN DATE,
    p_check_out_date IN DATE,
    p_special_requests IN VARCHAR2 DEFAULT NULL,
    p_booking_id OUT NUMBER
) AS
    v_room_available NUMBER;
    v_base_price NUMBER;
    v_days NUMBER;
    v_total_amount NUMBER;
    v_is_vip CHAR(1);
    v_discount_pct NUMBER := 0;
BEGIN
    -- Validate dates
    IF p_check_out_date <= p_check_in_date THEN
        RAISE_APPLICATION_ERROR(-20001, 'Check-out date must be after check-in date');
    END IF;

    -- Check room availability
    SELECT COUNT(*) INTO v_room_available
    FROM rooms r
    WHERE r.room_id = p_room_id
    AND r.status = 'AVAILABLE'
    AND NOT EXISTS (
        SELECT 1 FROM bookings b
        WHERE b.room_id = p_room_id
        AND b.booking_status IN ('CONFIRMED', 'CHECKED_IN')
        AND (p_check_in_date < b.check_out_date AND p_check_out_date > b.check_in_date)
    );

    IF v_room_available = 0 THEN
        RAISE_APPLICATION_ERROR(-20002, 'Room is not available for the selected dates');
    END IF;

    -- Get room price and check VIP status
    SELECT base_price INTO v_base_price FROM rooms WHERE room_id = p_room_id;
    v_is_vip := is_customer_vip(p_customer_id);

    IF v_is_vip = 'Y' THEN
        v_discount_pct := get_vip_discount(p_customer_id);
    END IF;

    -- Calculate total amount
    v_total_amount := calculate_room_price(p_room_id, p_check_in_date, p_check_out_date, v_is_vip, v_discount_pct);

    -- Create booking
    p_booking_id := booking_seq.NEXTVAL;

    INSERT INTO bookings (
        booking_id, customer_id, room_id, check_in_date, check_out_date,
        total_amount, discount_applied, booking_status, special_requests
    ) VALUES (
        p_booking_id, p_customer_id, p_room_id, p_check_in_date, p_check_out_date,
        v_total_amount, (v_base_price * (p_check_out_date - p_check_in_date) - v_total_amount),
        'CONFIRMED', p_special_requests
    );

    COMMIT;
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END create_booking;
/

-- Check in a customer
CREATE OR REPLACE PROCEDURE check_in_customer(
    p_booking_id IN NUMBER,
    p_checked_in_by IN VARCHAR2 DEFAULT USER,
    p_success OUT NUMBER
) AS
    v_booking_status VARCHAR2(20);
    v_room_id NUMBER;
    v_check_in_date DATE;
BEGIN
    p_success := 0;

    -- Get booking details
    SELECT booking_status, room_id, check_in_date
    INTO v_booking_status, v_room_id, v_check_in_date
    FROM bookings
    WHERE booking_id = p_booking_id;

    -- Check if booking is in correct status
    IF v_booking_status != 'CONFIRMED' THEN
        RAISE_APPLICATION_ERROR(-20003, 'Booking must be CONFIRMED to check in');
    END IF;

    -- Update booking status
    UPDATE bookings
    SET booking_status = 'CHECKED_IN',
        actual_check_in = SYSDATE
    WHERE booking_id = p_booking_id;

    -- Update room status
    UPDATE rooms
    SET status = 'OCCUPIED'
    WHERE room_id = v_room_id;

    p_success := 1;
    COMMIT;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(-20004, 'Booking not found');
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END check_in_customer;
/

-- Check out a customer
CREATE OR REPLACE PROCEDURE check_out_customer(
    p_booking_id IN NUMBER,
    p_late_checkout_hours IN NUMBER DEFAULT 0,
    p_extra_charges IN NUMBER DEFAULT 0,
    p_checked_out_by IN VARCHAR2 DEFAULT USER,
    p_success OUT NUMBER
) AS
    v_booking_status VARCHAR2(20);
    v_room_id NUMBER;
BEGIN
    p_success := 0;

    -- Get booking details
    SELECT booking_status, room_id
    INTO v_booking_status, v_room_id
    FROM bookings
    WHERE booking_id = p_booking_id;

    -- Check if booking is in correct status
    IF v_booking_status != 'CHECKED_IN' THEN
        RAISE_APPLICATION_ERROR(-20005, 'Booking must be CHECKED_IN to check out');
    END IF;

    -- Update booking status
    UPDATE bookings
    SET booking_status = 'CHECKED_OUT',
        actual_check_out = SYSDATE,
        late_checkout_hours = p_late_checkout_hours,
        extra_charges = extra_charges + p_extra_charges
    WHERE booking_id = p_booking_id;

    -- Update room status
    UPDATE rooms
    SET status = 'AVAILABLE',
        last_cleaned = NULL
    WHERE room_id = v_room_id;

    p_success := 1;
    COMMIT;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(-20006, 'Booking not found');
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END check_out_customer;
/

-- ======================================================
-- VIP MANAGEMENT PROCEDURES
-- ======================================================

-- Update customer VIP status based on spending
CREATE OR REPLACE PROCEDURE update_vip_status(
    p_customer_id IN NUMBER
) AS
    v_total_spent NUMBER;
    v_existing_vip NUMBER;
    v_new_level VARCHAR2(20);
    v_new_discount NUMBER;
BEGIN
    -- Get customer's total spending
    SELECT total_spent INTO v_total_spent
    FROM customers
    WHERE customer_id = p_customer_id;

    -- Determine VIP level based on spending
    IF v_total_spent >= 20000 THEN
        v_new_level := 'DIAMOND';
        v_new_discount := 20;
    ELSIF v_total_spent >= 10000 THEN
        v_new_level := 'PLATINUM';
        v_new_discount := 15;
    ELSIF v_total_spent >= 5000 THEN
        v_new_level := 'GOLD';
        v_new_discount := 10;
    ELSE
        -- Not eligible for VIP
        DELETE FROM vip_members WHERE customer_id = p_customer_id;
        RETURN;
    END IF;

    -- Check if customer is already VIP
    SELECT COUNT(*) INTO v_existing_vip
    FROM vip_members
    WHERE customer_id = p_customer_id;

    IF v_existing_vip = 0 THEN
        -- Create new VIP membership
        INSERT INTO vip_members (
            vip_id, customer_id, membership_level, discount_percentage,
            benefits, is_active
        ) VALUES (
            vip_seq.NEXTVAL, p_customer_id, v_new_level, v_new_discount,
            'Priority Booking, Late Checkout, Room Upgrades', 'Y'
        );
    ELSE
        -- Update existing VIP membership
        UPDATE vip_members
        SET membership_level = v_new_level,
            discount_percentage = v_new_discount
        WHERE customer_id = p_customer_id;
    END IF;

    COMMIT;
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END update_vip_status;
/

-- ======================================================
-- INVOICE MANAGEMENT PROCEDURES
-- ======================================================

-- Generate invoice for a booking
CREATE OR REPLACE PROCEDURE generate_invoice(
    p_booking_id IN NUMBER,
    p_tax_rate IN NUMBER DEFAULT 8,
    p_created_by IN VARCHAR2 DEFAULT USER
) AS
    v_booking_exists NUMBER;
    v_invoice_exists NUMBER;
    v_customer_id NUMBER;
    v_room_charge NUMBER;
    v_service_total NUMBER := 0;
    v_discount_amount NUMBER;
    v_extra_charges NUMBER;
    v_subtotal NUMBER;
    v_tax_amount NUMBER;
    v_total_amount NUMBER;
    v_invoice_number VARCHAR2(30);
    v_invoice_id NUMBER;
BEGIN
    -- Check if booking exists
    SELECT COUNT(*) INTO v_booking_exists
    FROM bookings
    WHERE booking_id = p_booking_id;

    IF v_booking_exists = 0 THEN
        RAISE_APPLICATION_ERROR(-20007, 'Booking not found');
    END IF;

    -- Check if invoice already exists
    SELECT COUNT(*) INTO v_invoice_exists
    FROM invoices
    WHERE booking_id = p_booking_id;

    IF v_invoice_exists > 0 THEN
        RAISE_APPLICATION_ERROR(-20008, 'Invoice already exists for this booking');
    END IF;

    -- Get booking details
    SELECT customer_id, total_amount, NVL(discount_applied, 0),
           NVL(extra_charges, 0), NVL(services_total, 0)
    INTO v_customer_id, v_room_charge, v_discount_amount, v_extra_charges, v_service_total
    FROM bookings
    WHERE booking_id = p_booking_id;

    -- Calculate invoice amounts
    v_subtotal := v_room_charge + v_service_total + v_extra_charges;
    v_tax_amount := v_subtotal * (p_tax_rate / 100);
    v_total_amount := v_subtotal + v_tax_amount - v_discount_amount;

    -- Generate invoice number
    v_invoice_id := invoice_seq.NEXTVAL;
    v_invoice_number := 'INV-' || TO_CHAR(SYSDATE, 'YYYYMMDD') || '-' || LPAD(v_invoice_id, 6, '0');

    -- Create invoice
    INSERT INTO invoices (
        invoice_id, booking_id, customer_id, invoice_number,
        invoice_date, due_date, subtotal, tax_amount,
        discount_amount, total_amount, payment_status, created_by
    ) VALUES (
        v_invoice_id, p_booking_id, v_customer_id, v_invoice_number,
        SYSDATE, SYSDATE + 30, v_subtotal, v_tax_amount,
        v_discount_amount, v_total_amount, 'PENDING', p_created_by
    );

    -- Add line items
    INSERT INTO invoice_line_items (
        line_item_id, invoice_id, item_type, item_description,
        quantity, unit_price, line_total
    ) VALUES (
        line_item_seq.NEXTVAL, v_invoice_id, 'ROOM', 'Room Charges',
        1, v_room_charge, v_room_charge
    );

    IF v_service_total > 0 THEN
        INSERT INTO invoice_line_items (
            line_item_id, invoice_id, item_type, item_description,
            quantity, unit_price, line_total
        ) VALUES (
            line_item_seq.NEXTVAL, v_invoice_id, 'SERVICE', 'Service Charges',
            1, v_service_total, v_service_total
        );
    END IF;

    IF v_extra_charges > 0 THEN
        INSERT INTO invoice_line_items (
            line_item_id, invoice_id, item_type, item_description,
            quantity, unit_price, line_total
        ) VALUES (
            line_item_seq.NEXTVAL, v_invoice_id, 'EXTRA_CHARGE', 'Additional Charges',
            1, v_extra_charges, v_extra_charges
        );
    END IF;

    INSERT INTO invoice_line_items (
        line_item_id, invoice_id, item_type, item_description,
        quantity, unit_price, line_total
    ) VALUES (
        line_item_seq.NEXTVAL, v_invoice_id, 'TAX', 'Tax (' || p_tax_rate || '%)',
        1, v_tax_amount, v_tax_amount
    );

    IF v_discount_amount > 0 THEN
        INSERT INTO invoice_line_items (
            line_item_id, invoice_id, item_type, item_description,
            quantity, unit_price, line_total
        ) VALUES (
            line_item_seq.NEXTVAL, v_invoice_id, 'DISCOUNT', 'VIP Discount',
            1, -v_discount_amount, -v_discount_amount
        );
    END IF;

    COMMIT;
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END generate_invoice;
/

-- ======================================================
-- SERVICE MANAGEMENT PROCEDURES
-- ======================================================

-- Add service usage for a booking
CREATE OR REPLACE PROCEDURE add_service_usage(
    p_booking_id IN NUMBER,
    p_customer_id IN NUMBER,
    p_service_id IN NUMBER,
    p_quantity IN NUMBER DEFAULT 1,
    p_usage_id OUT NUMBER,
    p_success OUT NUMBER,
    p_message OUT VARCHAR2
) AS
    v_booking_status VARCHAR2(20);
    v_booking_customer_id NUMBER;
    v_service_price NUMBER;
    v_is_complimentary CHAR(1);
    v_room_type_id NUMBER;
    v_total_cost NUMBER;
BEGIN
    p_success := 0;
    p_message := '';

    -- Validate booking exists and is active
    BEGIN
        SELECT booking_status, customer_id
        INTO v_booking_status, v_booking_customer_id
        FROM bookings
        WHERE booking_id = p_booking_id;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            p_message := 'Booking not found';
            RETURN;
    END;

    -- Check if booking is active (confirmed or checked in)
    IF v_booking_status NOT IN ('CONFIRMED', 'CHECKED_IN') THEN
        p_message := 'Services can only be added to confirmed or active bookings';
        RETURN;
    END IF;

    -- Verify customer matches booking
    IF v_booking_customer_id != p_customer_id THEN
        p_message := 'Customer does not match booking';
        RETURN;
    END IF;

    -- Get service price and check if service exists
    BEGIN
        SELECT base_price INTO v_service_price
        FROM room_services
        WHERE service_id = p_service_id AND is_active = 'Y';
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            p_message := 'Service not found or inactive';
            RETURN;
    END;

    -- Check if service is complimentary for this room type
    BEGIN
        SELECT rt.type_id INTO v_room_type_id
        FROM bookings b
        JOIN rooms r ON b.room_id = r.room_id
        JOIN room_types rt ON r.type_id = rt.type_id
        WHERE b.booking_id = p_booking_id;

        SELECT is_complimentary INTO v_is_complimentary
        FROM room_service_assignments
        WHERE room_type_id = v_room_type_id AND service_id = p_service_id;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            v_is_complimentary := 'N';
    END;

    -- Calculate total cost
    IF v_is_complimentary = 'Y' THEN
        v_total_cost := 0;
    ELSE
        v_total_cost := v_service_price * p_quantity;
    END IF;

    -- Create service usage record
    p_usage_id := usage_seq.NEXTVAL;

    INSERT INTO customer_service_usage (
        usage_id, booking_id, customer_id, service_id,
        usage_date, quantity, unit_price, total_cost, is_complimentary
    ) VALUES (
        p_usage_id, p_booking_id, p_customer_id, p_service_id,
        SYSDATE, p_quantity, v_service_price, v_total_cost, v_is_complimentary
    );

    p_success := 1;
    p_message := 'Service added successfully';
    COMMIT;

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        p_success := 0;
        p_message := 'Error adding service: ' || SQLERRM;
END add_service_usage;
/

-- Get available services for a booking
CREATE OR REPLACE PROCEDURE get_available_services(
    p_booking_id IN NUMBER,
    p_services_cursor OUT SYS_REFCURSOR,
    p_success OUT NUMBER,
    p_message OUT VARCHAR2
) AS
    v_room_type_id NUMBER;
BEGIN
    p_success := 0;

    -- Get room type for the booking
    BEGIN
        SELECT rt.type_id INTO v_room_type_id
        FROM bookings b
        JOIN rooms r ON b.room_id = r.room_id
        JOIN room_types rt ON r.type_id = rt.type_id
        WHERE b.booking_id = p_booking_id;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            p_message := 'Booking not found';
            RETURN;
    END;

    -- Get all services available for this room type
    OPEN p_services_cursor FOR
        SELECT rs.service_id, rs.service_name, rs.service_description,
               rs.service_category, rs.base_price,
               NVL(rsa.is_complimentary, 'N') as is_complimentary,
               CASE
                   WHEN NVL(rsa.is_complimentary, 'N') = 'Y' THEN 'Complimentary'
                   ELSE TO_CHAR(rs.base_price, 'FM999,990.00')
               END as display_price
        FROM room_services rs
        LEFT JOIN room_service_assignments rsa ON rs.service_id = rsa.service_id
                                               AND rsa.room_type_id = v_room_type_id
        WHERE rs.is_active = 'Y'
        ORDER BY rs.service_category, rs.service_name;

    p_success := 1;
    p_message := 'Services retrieved successfully';
END get_available_services;
/

-- Get customer service summary
CREATE OR REPLACE PROCEDURE get_customer_service_summary(
    p_customer_id IN NUMBER,
    p_service_cursor OUT SYS_REFCURSOR,
    p_total_cost OUT NUMBER,
    p_success OUT NUMBER,
    p_message OUT VARCHAR2
) AS
BEGIN
    p_success := 0;
    p_total_cost := 0;

    -- Get service usage summary
    OPEN p_service_cursor FOR
        SELECT rs.service_name, rs.service_category,
               SUM(csu.quantity) as total_quantity,
               AVG(csu.unit_price) as avg_unit_price,
               SUM(csu.total_cost) as total_cost,
               COUNT(*) as usage_count
        FROM customer_service_usage csu
        JOIN room_services rs ON csu.service_id = rs.service_id
        WHERE csu.customer_id = p_customer_id
        GROUP BY rs.service_name, rs.service_category
        ORDER BY total_cost DESC;

    -- Get total cost
    SELECT NVL(SUM(total_cost), 0) INTO p_total_cost
    FROM customer_service_usage
    WHERE customer_id = p_customer_id;

    p_success := 1;
    p_message := 'Summary retrieved successfully';
END get_customer_service_summary;
/

-- ======================================================
-- REPORTING / ANALYTICS FUNCTIONS
-- ======================================================

-- Total revenue from all completed (checked-out) bookings.
-- Revenue definition: net room charge (already discounted) + services_total + extra_charges.
-- (Discount already applied inside total_amount; discount_applied column is informational.)
CREATE OR REPLACE FUNCTION get_total_revenue
RETURN NUMBER IS
    v_total NUMBER := 0;
BEGIN
    SELECT NVL(SUM(total_amount + NVL(services_total,0) + NVL(extra_charges,0)),0)
      INTO v_total
      FROM bookings
     WHERE booking_status = 'CHECKED_OUT';
    RETURN v_total;
EXCEPTION
    WHEN OTHERS THEN
        RETURN 0; -- Fail-safe
END get_total_revenue;
/

COMMIT;
