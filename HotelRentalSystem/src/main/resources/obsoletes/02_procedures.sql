-- Stored Procedures for Hotel Management System
-- Oracle Database

-- Procedure to generate invoice for a booking
CREATE OR REPLACE PROCEDURE generate_invoice(
    p_booking_id IN NUMBER,
    p_tax_rate IN NUMBER DEFAULT 10,
    p_created_by IN VARCHAR2 DEFAULT USER
) AS
    v_booking_exists NUMBER;
    v_invoice_exists NUMBER;
    v_customer_id NUMBER;
    v_subtotal NUMBER(10,2);
    v_tax_amount NUMBER(10,2);
    v_discount_amount NUMBER(10,2);
    v_total_amount NUMBER(10,2);
    v_invoice_number VARCHAR2(50);
    v_service_total NUMBER(10,2) := 0;
BEGIN
    -- Check if booking exists
    SELECT COUNT(*) INTO v_booking_exists
    FROM bookings
    WHERE booking_id = p_booking_id;

    IF v_booking_exists = 0 THEN
        RAISE_APPLICATION_ERROR(-20001, 'Booking not found');
    END IF;

    -- Check if invoice already exists for this booking
    SELECT COUNT(*) INTO v_invoice_exists
    FROM invoices
    WHERE booking_id = p_booking_id;

    IF v_invoice_exists > 0 THEN
        RAISE_APPLICATION_ERROR(-20002, 'Invoice already exists for this booking');
    END IF;

    -- Get booking details
    SELECT customer_id, (total_amount - discount_applied + extra_charges), discount_applied
    INTO v_customer_id, v_subtotal, v_discount_amount
    FROM bookings
    WHERE booking_id = p_booking_id;

    -- Calculate service usage total
    SELECT NVL(SUM(total_price), 0) INTO v_service_total
    FROM service_usage
    WHERE booking_id = p_booking_id AND status = 'COMPLETED';

    -- Add service charges to subtotal
    v_subtotal := v_subtotal + v_service_total;

    -- Calculate tax
    v_tax_amount := v_subtotal * (p_tax_rate / 100);

    -- Calculate total
    v_total_amount := v_subtotal + v_tax_amount;

    -- Generate invoice number
    v_invoice_number := 'INV-' || TO_CHAR(SYSDATE, 'YYYYMMDD') || '-' || LPAD(invoice_seq.NEXTVAL, 6, '0');

    -- Insert invoice
    INSERT INTO invoices (
        invoice_id, booking_id, customer_id, invoice_number, invoice_date, due_date,
        subtotal, tax_amount, discount_amount, total_amount, payment_status,
        created_date, created_by
    ) VALUES (
        invoice_seq.NEXTVAL, p_booking_id, v_customer_id, v_invoice_number, SYSDATE, SYSDATE + 30,
        v_subtotal, v_tax_amount, v_discount_amount, v_total_amount, 'PENDING',
        SYSDATE, p_created_by
    );

    COMMIT;
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END generate_invoice;
/

-- Procedure to check in a customer
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

    -- Check if check-in date is today or past
    IF v_check_in_date > TRUNC(SYSDATE) THEN
        RAISE_APPLICATION_ERROR(-20004, 'Cannot check in before scheduled check-in date');
    END IF;

    -- Update booking status
    UPDATE bookings
    SET booking_status = 'CHECKED_IN',
        actual_check_in = SYSTIMESTAMP,
        payment_status = CASE WHEN payment_status = 'PENDING' THEN 'PAID' ELSE payment_status END
    WHERE booking_id = p_booking_id;

    -- Update room status
    UPDATE rooms
    SET status = 'OCCUPIED'
    WHERE room_id = v_room_id;

    p_success := 1;
    COMMIT;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20005, 'Booking not found');
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END check_in_customer;
/

-- Procedure to check out a customer
CREATE OR REPLACE PROCEDURE check_out_customer(
    p_booking_id IN NUMBER,
    p_checked_out_by IN VARCHAR2 DEFAULT USER,
    p_success OUT NUMBER
) AS
    v_booking_status VARCHAR2(20);
    v_room_id NUMBER;
    v_customer_id NUMBER;
    v_total_amount NUMBER(10,2);
BEGIN
    p_success := 0;

    -- Get booking details
    SELECT booking_status, room_id, customer_id, total_amount
    INTO v_booking_status, v_room_id, v_customer_id, v_total_amount
    FROM bookings
    WHERE booking_id = p_booking_id;

    -- Check if booking is in correct status
    IF v_booking_status != 'CHECKED_IN' THEN
        RAISE_APPLICATION_ERROR(-20006, 'Booking must be CHECKED_IN to check out');
    END IF;

    -- Update booking status
    UPDATE bookings
    SET booking_status = 'CHECKED_OUT',
        actual_check_out = SYSTIMESTAMP,
        payment_status = 'PAID'
    WHERE booking_id = p_booking_id;

    -- Update room status
    UPDATE rooms
    SET status = 'AVAILABLE'
    WHERE room_id = v_room_id;

    -- Update customer total spent and loyalty points
    UPDATE customers
    SET total_spent = total_spent + v_total_amount,
        loyalty_points = loyalty_points + FLOOR(v_total_amount / 10)
    WHERE customer_id = v_customer_id;

    -- Generate invoice if not exists
    BEGIN
        generate_invoice(p_booking_id, 10, p_checked_out_by);
    EXCEPTION
        WHEN OTHERS THEN
            -- Invoice might already exist, continue
            NULL;
    END;

    p_success := 1;
    COMMIT;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20007, 'Booking not found');
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END check_out_customer;
/

-- Procedure to add room service usage
CREATE OR REPLACE PROCEDURE add_service_usage(
    p_booking_id IN NUMBER,
    p_service_id IN NUMBER,
    p_quantity IN NUMBER DEFAULT 1,
    p_created_by IN VARCHAR2 DEFAULT USER,
    p_usage_id OUT NUMBER
) AS
    v_customer_id NUMBER;
    v_service_price NUMBER(8,2);
    v_total_price NUMBER(10,2);
    v_booking_status VARCHAR2(20);
BEGIN
    -- Get booking details
    SELECT customer_id, booking_status
    INTO v_customer_id, v_booking_status
    FROM bookings
    WHERE booking_id = p_booking_id;

    -- Check if booking allows service usage
    IF v_booking_status NOT IN ('CONFIRMED', 'CHECKED_IN') THEN
        RAISE_APPLICATION_ERROR(-20008, 'Services can only be added to confirmed or checked-in bookings');
    END IF;

    -- Get service price
    SELECT price INTO v_service_price
    FROM room_services
    WHERE service_id = p_service_id AND is_active = 'Y';

    -- Calculate total price
    v_total_price := v_service_price * p_quantity;

    -- Insert service usage
    INSERT INTO service_usage (
        usage_id, booking_id, customer_id, service_id, quantity,
        unit_price, total_price, usage_date, status, created_by
    ) VALUES (
        service_usage_seq.NEXTVAL, p_booking_id, v_customer_id, p_service_id, p_quantity,
        v_service_price, v_total_price, SYSDATE, 'COMPLETED', p_created_by
    ) RETURNING usage_id INTO p_usage_id;

    -- Update booking extra charges
    UPDATE bookings
    SET extra_charges = extra_charges + v_total_price
    WHERE booking_id = p_booking_id;

    COMMIT;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        ROLLBACK;
        IF SQL%NOTFOUND THEN
            RAISE_APPLICATION_ERROR(-20009, 'Booking or service not found');
        END IF;
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END add_service_usage;
/

-- Procedure to promote customer to VIP
CREATE OR REPLACE PROCEDURE promote_to_vip(
    p_customer_id IN NUMBER,
    p_membership_level IN VARCHAR2 DEFAULT 'BRONZE',
    p_created_by IN VARCHAR2 DEFAULT USER,
    p_vip_id OUT NUMBER
) AS
    v_customer_exists NUMBER;
    v_vip_exists NUMBER;
    v_discount_pct NUMBER(5,2);
    v_annual_fee NUMBER(8,2);
    v_benefits VARCHAR2(1000);
BEGIN
    -- Check if customer exists
    SELECT COUNT(*) INTO v_customer_exists
    FROM customers
    WHERE customer_id = p_customer_id AND is_active = 'Y';

    IF v_customer_exists = 0 THEN
        RAISE_APPLICATION_ERROR(-20010, 'Customer not found or inactive');
    END IF;

    -- Check if already VIP
    SELECT COUNT(*) INTO v_vip_exists
    FROM vip_members
    WHERE customer_id = p_customer_id AND is_active = 'Y';

    IF v_vip_exists > 0 THEN
        RAISE_APPLICATION_ERROR(-20011, 'Customer is already a VIP member');
    END IF;

    -- Set benefits based on membership level
    CASE p_membership_level
        WHEN 'BRONZE' THEN
            v_discount_pct := 5;
            v_annual_fee := 100;
            v_benefits := 'Free WiFi, Late checkout until 2 PM, Welcome drink';
        WHEN 'SILVER' THEN
            v_discount_pct := 10;
            v_annual_fee := 250;
            v_benefits := 'All Bronze benefits, Room upgrade (subject to availability), Free breakfast';
        WHEN 'GOLD' THEN
            v_discount_pct := 15;
            v_annual_fee := 500;
            v_benefits := 'All Silver benefits, Free airport transfer, Concierge service';
        WHEN 'PLATINUM' THEN
            v_discount_pct := 20;
            v_annual_fee := 1000;
            v_benefits := 'All Gold benefits, Personal butler, Spa access, Priority reservations';
        ELSE
            RAISE_APPLICATION_ERROR(-20012, 'Invalid membership level');
    END CASE;

    -- Insert VIP membership
    INSERT INTO vip_members (
        vip_id, customer_id, membership_level, join_date, expiry_date,
        discount_percentage, benefits, annual_fee, is_active, created_by
    ) VALUES (
        vip_member_seq.NEXTVAL, p_customer_id, p_membership_level, SYSDATE, ADD_MONTHS(SYSDATE, 12),
        v_discount_pct, v_benefits, v_annual_fee, 'Y', p_created_by
    ) RETURNING vip_id INTO p_vip_id;

    COMMIT;
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END promote_to_vip;
/

-- Procedure to calculate occupancy rate
CREATE OR REPLACE PROCEDURE calculate_occupancy_rate(
    p_start_date IN DATE,
    p_end_date IN DATE,
    p_occupancy_rate OUT NUMBER
) AS
    v_total_rooms NUMBER;
    v_total_room_days NUMBER;
    v_occupied_room_days NUMBER;
BEGIN
    -- Get total number of rooms
    SELECT COUNT(*) INTO v_total_rooms
    FROM rooms
    WHERE status != 'OUT_OF_ORDER';

    -- Calculate total possible room days
    v_total_room_days := v_total_rooms * (p_end_date - p_start_date + 1);

    -- Calculate occupied room days
    SELECT NVL(SUM(
        CASE
            WHEN check_out_date <= p_end_date AND check_in_date >= p_start_date THEN
                check_out_date - check_in_date
            WHEN check_in_date < p_start_date AND check_out_date > p_end_date THEN
                p_end_date - p_start_date + 1
            WHEN check_in_date < p_start_date AND check_out_date <= p_end_date THEN
                check_out_date - p_start_date
            WHEN check_in_date >= p_start_date AND check_out_date > p_end_date THEN
                p_end_date - check_in_date + 1
            ELSE 0
        END
    ), 0) INTO v_occupied_room_days
    FROM bookings
    WHERE booking_status IN ('CONFIRMED', 'CHECKED_IN', 'CHECKED_OUT')
    AND ((check_in_date BETWEEN p_start_date AND p_end_date)
         OR (check_out_date BETWEEN p_start_date AND p_end_date)
         OR (check_in_date < p_start_date AND check_out_date > p_end_date));

    -- Calculate occupancy rate
    IF v_total_room_days > 0 THEN
        p_occupancy_rate := (v_occupied_room_days / v_total_room_days) * 100;
    ELSE
        p_occupancy_rate := 0;
    END IF;

EXCEPTION
    WHEN OTHERS THEN
        p_occupancy_rate := 0;
        RAISE;
END calculate_occupancy_rate;
/

-- Procedure to update payment status
CREATE OR REPLACE PROCEDURE update_payment_status(
    p_booking_id IN NUMBER,
    p_payment_status IN VARCHAR2,
    p_payment_method IN VARCHAR2 DEFAULT NULL,
    p_updated_by IN VARCHAR2 DEFAULT USER
) AS
    v_booking_exists NUMBER;
BEGIN
    -- Check if booking exists
    SELECT COUNT(*) INTO v_booking_exists
    FROM bookings
    WHERE booking_id = p_booking_id;

    IF v_booking_exists = 0 THEN
        RAISE_APPLICATION_ERROR(-20013, 'Booking not found');
    END IF;

    -- Validate payment status
    IF p_payment_status NOT IN ('PENDING', 'PARTIAL', 'PAID', 'REFUNDED') THEN
        RAISE_APPLICATION_ERROR(-20014, 'Invalid payment status');
    END IF;

    -- Update booking payment status
    UPDATE bookings
    SET payment_status = p_payment_status
    WHERE booking_id = p_booking_id;

    -- Update related invoice if exists
    UPDATE invoices
    SET payment_status = CASE
                            WHEN p_payment_status = 'PAID' THEN 'PAID'
                            WHEN p_payment_status = 'PARTIAL' THEN 'PARTIAL'
                            WHEN p_payment_status = 'REFUNDED' THEN 'CANCELLED'
                            ELSE 'PENDING'
                         END,
        payment_date = CASE WHEN p_payment_status = 'PAID' THEN SYSDATE ELSE payment_date END,
        payment_method = NVL(p_payment_method, payment_method)
    WHERE booking_id = p_booking_id;

    COMMIT;
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END update_payment_status;
/

COMMIT;
