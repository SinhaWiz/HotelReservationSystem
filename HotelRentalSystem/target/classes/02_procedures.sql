-- ======================================================
-- 02_procedures.sql
-- Stored Procedures for Hotel Reservation System
-- ======================================================

-- ======================================================
-- BOOKING MANAGEMENT PROCEDURES
-- ======================================================

-- Create a new booking with validation
CREATE OR REPLACE PROCEDURE create_booking(
    p_customer_id IN NUMBER,
    p_room_id IN NUMBER,
    p_check_in_date IN DATE,
    p_check_out_date IN DATE,
    p_total_amount IN NUMBER,
    p_special_requests IN VARCHAR2 DEFAULT NULL,
    p_booking_id OUT NUMBER
) AS
    v_room_available NUMBER;
    v_room_status VARCHAR2(20);
BEGIN
    -- Check if the room is available for the requested dates
    SELECT COUNT(*) INTO v_room_available
    FROM bookings
    WHERE room_id = p_room_id
    AND booking_status IN ('CONFIRMED', 'CHECKED_IN')
    AND ((p_check_in_date BETWEEN check_in_date AND check_out_date - 1)
         OR (p_check_out_date - 1 BETWEEN check_in_date AND check_out_date - 1)
         OR (check_in_date BETWEEN p_check_in_date AND p_check_out_date - 1));

    -- Check room status
    SELECT status INTO v_room_status
    FROM rooms
    WHERE room_id = p_room_id;

    -- Throw an error if the room is not available
    IF v_room_available > 0 OR v_room_status != 'AVAILABLE' THEN
        RAISE_APPLICATION_ERROR(-20001, 'Room is not available for the selected dates');
    END IF;

    -- Create the booking
    p_booking_id := booking_seq.NEXTVAL;

    INSERT INTO bookings (
        booking_id, customer_id, room_id, check_in_date, check_out_date,
        total_amount, booking_status, payment_status, special_requests,
        booking_date, created_date
    ) VALUES (
        p_booking_id, p_customer_id, p_room_id, p_check_in_date, p_check_out_date,
        p_total_amount, 'CONFIRMED', 'PENDING', p_special_requests,
        SYSDATE, SYSDATE
    );

    COMMIT;
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END create_booking;
/

-- Check-in procedure
CREATE OR REPLACE PROCEDURE check_in_guest(
    p_booking_id IN NUMBER
) AS
    v_status VARCHAR2(20);
    v_room_id NUMBER;
BEGIN
    -- Check if booking exists and is in the correct status
    SELECT booking_status, room_id INTO v_status, v_room_id
    FROM bookings
    WHERE booking_id = p_booking_id;

    IF v_status != 'CONFIRMED' THEN
        RAISE_APPLICATION_ERROR(-20002, 'Booking is not in CONFIRMED status');
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

    COMMIT;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(-20003, 'Booking not found');
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END check_in_guest;
/

-- Check-out procedure
CREATE OR REPLACE PROCEDURE check_out_guest(
    p_booking_id IN NUMBER,
    p_extra_charges IN NUMBER DEFAULT 0,
    p_late_checkout_hours IN NUMBER DEFAULT 0
) AS
    v_status VARCHAR2(20);
    v_room_id NUMBER;
    v_customer_id NUMBER;
    v_total_amount NUMBER;
    v_services_total NUMBER := 0;
    v_total_with_extras NUMBER;
BEGIN
    -- Check if booking exists and is in the correct status
    SELECT booking_status, room_id, customer_id, total_amount
    INTO v_status, v_room_id, v_customer_id, v_total_amount
    FROM bookings
    WHERE booking_id = p_booking_id;

    IF v_status != 'CHECKED_IN' THEN
        RAISE_APPLICATION_ERROR(-20004, 'Booking is not in CHECKED_IN status');
    END IF;

    -- Calculate service charges
    SELECT NVL(SUM(total_cost), 0) INTO v_services_total
    FROM customer_service_usage
    WHERE booking_id = p_booking_id;

    v_total_with_extras := v_total_amount + v_services_total + p_extra_charges;

    -- Update booking status
    UPDATE bookings
    SET booking_status = 'CHECKED_OUT',
        actual_check_out = SYSDATE,
        extra_charges = p_extra_charges,
        late_checkout_hours = p_late_checkout_hours,
        services_total = v_services_total
    WHERE booking_id = p_booking_id;

    -- Update room status
    UPDATE rooms
    SET status = 'AVAILABLE',
        last_cleaned = NULL  -- Indicates needs cleaning
    WHERE room_id = v_room_id;

    -- Update customer spending
    UPDATE customers
    SET total_spent = total_spent + v_total_with_extras,
        loyalty_points = loyalty_points + FLOOR(v_total_with_extras / 10)  -- 1 point per $10 spent
    WHERE customer_id = v_customer_id;

    -- Create an invoice automatically
    generate_invoice(p_booking_id);

    COMMIT;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(-20005, 'Booking not found');
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END check_out_guest;
/

-- Cancel booking procedure
CREATE OR REPLACE PROCEDURE cancel_booking(
    p_booking_id IN NUMBER,
    p_refund_amount IN NUMBER DEFAULT NULL
) AS
    v_status VARCHAR2(20);
    v_room_id NUMBER;
    v_payment_status VARCHAR2(20);
BEGIN
    -- Check if booking exists and can be cancelled
    SELECT booking_status, room_id, payment_status INTO v_status, v_room_id, v_payment_status
    FROM bookings
    WHERE booking_id = p_booking_id;

    IF v_status IN ('CHECKED_OUT', 'CANCELLED') THEN
        RAISE_APPLICATION_ERROR(-20006, 'Booking cannot be cancelled');
    END IF;

    -- Update booking status
    UPDATE bookings
    SET booking_status = 'CANCELLED',
        payment_status = CASE
                           WHEN v_payment_status = 'PAID' AND p_refund_amount IS NOT NULL
                           THEN 'REFUNDED'
                           ELSE payment_status
                         END
    WHERE booking_id = p_booking_id;

    -- If the room was occupied, make it available again
    IF v_status = 'CHECKED_IN' THEN
        UPDATE rooms
        SET status = 'AVAILABLE'
        WHERE room_id = v_room_id;
    END IF;

    COMMIT;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(-20007, 'Booking not found');
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END cancel_booking;
/

-- ======================================================
-- INVOICE MANAGEMENT PROCEDURES
-- ======================================================

-- Generate invoice for a booking
CREATE OR REPLACE PROCEDURE generate_invoice(
    p_booking_id IN NUMBER
) AS
    v_invoice_id NUMBER;
    v_customer_id NUMBER;
    v_room_charge NUMBER;
    v_service_total NUMBER := 0;
    v_tax_rate NUMBER := 0.08; -- 8% tax
    v_subtotal NUMBER;
    v_tax_amount NUMBER;
    v_total_amount NUMBER;
    v_discount NUMBER := 0;
    v_invoice_number VARCHAR2(30);
BEGIN
    -- Get booking information
    SELECT customer_id, total_amount, discount_applied, services_total
    INTO v_customer_id, v_room_charge, v_discount, v_service_total
    FROM bookings
    WHERE booking_id = p_booking_id;

    -- Calculate invoice amounts
    v_subtotal := v_room_charge + v_service_total;
    v_tax_amount := v_subtotal * v_tax_rate;
    v_total_amount := v_subtotal + v_tax_amount - v_discount;

    -- Create invoice
    v_invoice_id := invoice_seq.NEXTVAL;
    v_invoice_number := 'INV-' || TO_CHAR(SYSDATE, 'YYYYMMDD') || '-' || LPAD(v_invoice_id, 4, '0');

    INSERT INTO invoices (
        invoice_id, booking_id, customer_id, invoice_number,
        invoice_date, due_date, subtotal, tax_amount,
        discount_amount, total_amount, payment_status
    ) VALUES (
        v_invoice_id, p_booking_id, v_customer_id, v_invoice_number,
        SYSDATE, SYSDATE + 15, v_subtotal, v_tax_amount,
        v_discount, v_total_amount, 'PENDING'
    );

    -- Add line item for room charge
    INSERT INTO invoice_line_items (
        line_item_id, invoice_id, item_type, item_description,
        quantity, unit_price, line_total
    ) VALUES (
        line_item_seq.NEXTVAL, v_invoice_id, 'ROOM', 'Room Charge',
        1, v_room_charge, v_room_charge
    );

    -- Add line items for services
    FOR svc IN (
        SELECT service_id, usage_id, service_name, quantity, unit_price, total_cost
        FROM customer_service_usage csu
        JOIN room_services rs ON csu.service_id = rs.service_id
        WHERE booking_id = p_booking_id
    ) LOOP
        INSERT INTO invoice_line_items (
            line_item_id, invoice_id, item_type, item_description,
            quantity, unit_price, line_total, service_id, usage_id
        ) VALUES (
            line_item_seq.NEXTVAL, v_invoice_id, 'SERVICE', svc.service_name,
            svc.quantity, svc.unit_price, svc.total_cost, svc.service_id, svc.usage_id
        );
    END LOOP;

    -- Add tax line item
    INSERT INTO invoice_line_items (
        line_item_id, invoice_id, item_type, item_description,
        quantity, unit_price, line_total
    ) VALUES (
        line_item_seq.NEXTVAL, v_invoice_id, 'TAX', 'Tax (' || (v_tax_rate * 100) || '%)',
        1, v_tax_amount, v_tax_amount
    );

    -- Add discount line item if applicable
    IF v_discount > 0 THEN
        INSERT INTO invoice_line_items (
            line_item_id, invoice_id, item_type, item_description,
            quantity, unit_price, line_total
        ) VALUES (
            line_item_seq.NEXTVAL, v_invoice_id, 'DISCOUNT', 'Discount',
            1, -v_discount, -v_discount
        );
    END IF;

    COMMIT;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(-20008, 'Booking not found');
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END generate_invoice;
/

-- Process payment for an invoice
CREATE OR REPLACE PROCEDURE process_payment(
    p_invoice_id IN NUMBER,
    p_payment_method IN VARCHAR2,
    p_payment_amount IN NUMBER,
    p_payment_reference IN VARCHAR2 DEFAULT NULL
) AS
    v_total_amount NUMBER;
    v_booking_id NUMBER;
BEGIN
    -- Get invoice details
    SELECT total_amount, booking_id INTO v_total_amount, v_booking_id
    FROM invoices
    WHERE invoice_id = p_invoice_id;

    -- Verify payment amount
    IF p_payment_amount < v_total_amount THEN
        RAISE_APPLICATION_ERROR(-20009, 'Payment amount is less than invoice total');
    END IF;

    -- Update invoice
    UPDATE invoices
    SET payment_status = 'PAID',
        payment_date = SYSDATE,
        payment_method = p_payment_method,
        notes = CASE
                  WHEN notes IS NULL THEN 'Payment Ref: ' || p_payment_reference
                  ELSE notes || '; Payment Ref: ' || p_payment_reference
                END
    WHERE invoice_id = p_invoice_id;

    -- Update booking payment status
    UPDATE bookings
    SET payment_status = 'PAID'
    WHERE booking_id = v_booking_id;

    COMMIT;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(-20010, 'Invoice not found');
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END process_payment;
/

-- ======================================================
-- CUSTOMER MANAGEMENT PROCEDURES
-- ======================================================

-- Create or update VIP status for eligible customers
CREATE OR REPLACE PROCEDURE update_vip_status(
    p_customer_id IN NUMBER
) AS
    v_total_spent NUMBER;
    v_membership_level VARCHAR2(20);
    v_discount_percentage NUMBER;
    v_vip_exists NUMBER;
    v_benefits VARCHAR2(500);
BEGIN
    -- Get customer's total spending
    SELECT total_spent INTO v_total_spent
    FROM customers
    WHERE customer_id = p_customer_id;

    -- Determine membership level based on spending
    IF v_total_spent >= 20000 THEN
        v_membership_level := 'DIAMOND';
        v_discount_percentage := 20;
        v_benefits := 'Priority Check-in, Late Checkout, Room Upgrades, Complimentary Breakfast & Spa';
    ELSIF v_total_spent >= 10000 THEN
        v_membership_level := 'PLATINUM';
        v_discount_percentage := 15;
        v_benefits := 'Priority Check-in, Late Checkout, Room Upgrades';
    ELSIF v_total_spent >= 5000 THEN
        v_membership_level := 'GOLD';
        v_discount_percentage := 10;
        v_benefits := 'Late Checkout, Room Upgrades';
    ELSE
        -- Not eligible for VIP status
        RETURN;
    END IF;

    -- Check if customer already has VIP status
    SELECT COUNT(*) INTO v_vip_exists
    FROM vip_members
    WHERE customer_id = p_customer_id;

    IF v_vip_exists = 0 THEN
        -- Create new VIP member
        INSERT INTO vip_members (
            vip_id, customer_id, membership_level, discount_percentage,
            membership_start_date, benefits, is_active
        ) VALUES (
            vip_seq.NEXTVAL, p_customer_id, v_membership_level, v_discount_percentage,
            SYSDATE, v_benefits, 'Y'
        );
    ELSE
        -- Update existing VIP status
        UPDATE vip_members
        SET membership_level = v_membership_level,
            discount_percentage = v_discount_percentage,
            benefits = v_benefits,
            is_active = 'Y'
        WHERE customer_id = p_customer_id;
    END IF;

    COMMIT;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(-20011, 'Customer not found');
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END update_vip_status;
/

-- ======================================================
-- ROOM MANAGEMENT PROCEDURES
-- ======================================================

-- Update room status
CREATE OR REPLACE PROCEDURE update_room_status(
    p_room_id IN NUMBER,
    p_status IN VARCHAR2
) AS
BEGIN
    UPDATE rooms
    SET status = p_status,
        last_maintenance = CASE
                            WHEN p_status = 'MAINTENANCE' THEN SYSDATE
                            ELSE last_maintenance
                           END,
        last_cleaned = CASE
                        WHEN p_status = 'AVAILABLE' THEN SYSDATE
                        ELSE last_cleaned
                       END
    WHERE room_id = p_room_id;

    COMMIT;
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END update_room_status;
/

-- Add service to a booking
CREATE OR REPLACE PROCEDURE add_service_to_booking(
    p_booking_id IN NUMBER,
    p_service_id IN NUMBER,
    p_quantity IN NUMBER DEFAULT 1,
    p_is_complimentary IN CHAR DEFAULT 'N'
) AS
    v_customer_id NUMBER;
    v_service_price NUMBER;
    v_total_cost NUMBER;
    v_is_active CHAR(1);
BEGIN
    -- Get booking information
    SELECT customer_id INTO v_customer_id
    FROM bookings
    WHERE booking_id = p_booking_id;

    -- Get service information
    SELECT base_price, is_active INTO v_service_price, v_is_active
    FROM room_services
    WHERE service_id = p_service_id;

    -- Check if service is active
    IF v_is_active = 'N' THEN
        RAISE_APPLICATION_ERROR(-20012, 'Service is not active');
    END IF;

    -- Calculate total cost
    IF p_is_complimentary = 'Y' THEN
        v_total_cost := 0;
    ELSE
        v_total_cost := v_service_price * p_quantity;
    END IF;

    -- Add service usage
    INSERT INTO customer_service_usage (
        usage_id, booking_id, customer_id, service_id,
        usage_date, quantity, unit_price, total_cost, is_complimentary
    ) VALUES (
        usage_seq.NEXTVAL, p_booking_id, v_customer_id, p_service_id,
        SYSDATE, p_quantity, v_service_price, v_total_cost, p_is_complimentary
    );

    -- Update services total in booking
    UPDATE bookings
    SET services_total = services_total + v_total_cost
    WHERE booking_id = p_booking_id;

    COMMIT;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(-20013, 'Booking or service not found');
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END add_service_to_booking;
/

-- Archive old bookings
CREATE OR REPLACE PROCEDURE archive_old_bookings(
    p_days_old IN NUMBER DEFAULT 365
) AS
    v_cutoff_date DATE := SYSDATE - p_days_old;
BEGIN
    -- Move old bookings to archive
    INSERT INTO booking_archive (
        archive_id, booking_id, customer_id, room_id,
        check_in_date, check_out_date, actual_check_in, actual_check_out,
        booking_date, total_amount, discount_applied, extra_charges,
        payment_status, booking_status, special_requests, archived_date
    )
    SELECT
        booking_archive_seq.NEXTVAL, booking_id, customer_id, room_id,
        check_in_date, check_out_date, actual_check_in, actual_check_out,
        booking_date, total_amount, discount_applied, extra_charges,
        payment_status, booking_status, special_requests, SYSDATE
    FROM bookings
    WHERE check_out_date < v_cutoff_date
    AND booking_status IN ('CHECKED_OUT', 'CANCELLED', 'NO_SHOW');

    -- Delete archived bookings
    DELETE FROM bookings
    WHERE check_out_date < v_cutoff_date
    AND booking_status IN ('CHECKED_OUT', 'CANCELLED', 'NO_SHOW');

    COMMIT;
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END archive_old_bookings;
/

-- Search for available rooms procedure
CREATE OR REPLACE PROCEDURE find_available_rooms(
    p_check_in_date IN DATE,
    p_check_out_date IN DATE,
    p_room_type_id IN NUMBER DEFAULT NULL,
    p_floor_number IN NUMBER DEFAULT NULL,
    p_min_price IN NUMBER DEFAULT NULL,
    p_max_price IN NUMBER DEFAULT NULL,
    p_results OUT SYS_REFCURSOR
) AS
BEGIN
    OPEN p_results FOR
        SELECT r.room_id, r.room_number, r.floor_number,
               rt.type_name, rt.base_price, rt.max_occupancy,
               r.amenities, r.description
        FROM rooms r
        JOIN room_types rt ON r.type_id = rt.type_id
        WHERE r.status = 'AVAILABLE'
        AND (p_room_type_id IS NULL OR r.type_id = p_room_type_id)
        AND (p_floor_number IS NULL OR r.floor_number = p_floor_number)
        AND (p_min_price IS NULL OR rt.base_price >= p_min_price)
        AND (p_max_price IS NULL OR rt.base_price <= p_max_price)
        AND NOT EXISTS (
            SELECT 1
            FROM bookings b
            WHERE b.room_id = r.room_id
            AND b.booking_status IN ('CONFIRMED', 'CHECKED_IN')
            AND ((p_check_in_date BETWEEN b.check_in_date AND b.check_out_date - 1)
                 OR (p_check_out_date - 1 BETWEEN b.check_in_date AND b.check_out_date - 1)
                 OR (b.check_in_date BETWEEN p_check_in_date AND p_check_out_date - 1))
        )
        ORDER BY rt.base_price;
END find_available_rooms;
/

-- Generate booking report for a date range
CREATE OR REPLACE PROCEDURE generate_booking_report(
    p_start_date IN DATE,
    p_end_date IN DATE,
    p_report_cursor OUT SYS_REFCURSOR
) AS
BEGIN
    OPEN p_report_cursor FOR
        SELECT
            b.booking_id,
            c.first_name || ' ' || c.last_name AS customer_name,
            c.email AS customer_email,
            r.room_number,
            rt.type_name AS room_type,
            b.check_in_date,
            b.check_out_date,
            b.booking_status,
            b.payment_status,
            b.total_amount,
            b.services_total,
            (b.total_amount + b.services_total) AS grand_total,
            (b.check_out_date - b.check_in_date) AS stay_duration,
            b.created_date AS booking_date
        FROM
            bookings b
            JOIN customers c ON b.customer_id = c.customer_id
            JOIN rooms r ON b.room_id = r.room_id
            JOIN room_types rt ON r.type_id = rt.type_id
        WHERE
            b.check_in_date BETWEEN p_start_date AND p_end_date
            OR b.check_out_date BETWEEN p_start_date AND p_end_date
            OR (b.check_in_date <= p_start_date AND b.check_out_date >= p_end_date)
        ORDER BY
            b.check_in_date;
END generate_booking_report;
/

-- Generate revenue report by room type
CREATE OR REPLACE PROCEDURE generate_revenue_report(
    p_start_date IN DATE,
    p_end_date IN DATE,
    p_report_cursor OUT SYS_REFCURSOR
) AS
BEGIN
    OPEN p_report_cursor FOR
        SELECT
            rt.type_name,
            COUNT(DISTINCT b.booking_id) AS booking_count,
            SUM(b.total_amount) AS room_revenue,
            SUM(b.services_total) AS service_revenue,
            SUM(b.total_amount + b.services_total) AS total_revenue,
            ROUND(SUM(b.total_amount + b.services_total) /
                  COUNT(DISTINCT b.booking_id), 2) AS average_revenue_per_booking,
            ROUND(SUM(b.total_amount) /
                  SUM(b.check_out_date - b.check_in_date), 2) AS average_daily_rate
        FROM
            bookings b
            JOIN rooms r ON b.room_id = r.room_id
            JOIN room_types rt ON r.type_id = rt.type_id
        WHERE
            b.booking_status IN ('CHECKED_OUT', 'CHECKED_IN')
            AND (
                (b.check_in_date BETWEEN p_start_date AND p_end_date)
                OR (b.check_out_date BETWEEN p_start_date AND p_end_date)
                OR (b.check_in_date <= p_start_date AND b.check_out_date >= p_end_date)
            )
        GROUP BY
            rt.type_name
        ORDER BY
            total_revenue DESC;
END generate_revenue_report;
/
