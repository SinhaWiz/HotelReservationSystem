-- Hotel Management System - Booking Stored Procedures
-- Oracle Database Stored Procedures for Booking Operations

-- Procedure to check room availability
CREATE OR REPLACE PROCEDURE check_room_availability(
    p_room_id IN NUMBER,
    p_check_in_date IN DATE,
    p_check_out_date IN DATE,
    p_is_available OUT NUMBER,
    p_message OUT VARCHAR2
) AS
    v_count NUMBER;
    v_room_status VARCHAR2(20);
BEGIN
    -- Check if room exists and get its status
    SELECT status INTO v_room_status 
    FROM rooms 
    WHERE room_id = p_room_id;
    
    -- Check if room is in maintenance
    IF v_room_status = 'MAINTENANCE' THEN
        p_is_available := 0;
        p_message := 'Room is under maintenance';
        RETURN;
    END IF;
    
    -- Check for overlapping bookings
    SELECT COUNT(*) INTO v_count
    FROM bookings
    WHERE room_id = p_room_id
    AND booking_status IN ('CONFIRMED', 'CHECKED_IN')
    AND (
        (p_check_in_date >= check_in_date AND p_check_in_date < check_out_date)
        OR (p_check_out_date > check_in_date AND p_check_out_date <= check_out_date)
        OR (p_check_in_date <= check_in_date AND p_check_out_date >= check_out_date)
    );
    
    IF v_count > 0 THEN
        p_is_available := 0;
        p_message := 'Room is not available for the selected dates';
    ELSE
        p_is_available := 1;
        p_message := 'Room is available';
    END IF;
    
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        p_is_available := 0;
        p_message := 'Room not found';
    WHEN OTHERS THEN
        p_is_available := 0;
        p_message := 'Error checking availability: ' || SQLERRM;
END check_room_availability;
/

-- Procedure to create a new booking
CREATE OR REPLACE PROCEDURE create_booking(
    p_customer_id IN NUMBER,
    p_room_id IN NUMBER,
    p_check_in_date IN DATE,
    p_check_out_date IN DATE,
    p_special_requests IN VARCHAR2 DEFAULT NULL,
    p_booking_id OUT NUMBER,
    p_total_amount OUT NUMBER,
    p_message OUT VARCHAR2
) AS
    v_is_available NUMBER;
    v_availability_msg VARCHAR2(200);
    v_base_price NUMBER(10,2);
    v_nights NUMBER;
    v_discount_percentage NUMBER(5,2) := 0;
    v_discount_amount NUMBER(12,2) := 0;
    v_customer_total_spent NUMBER(12,2);
    v_is_vip NUMBER;
BEGIN
    -- Check room availability
    check_room_availability(p_room_id, p_check_in_date, p_check_out_date, v_is_available, v_availability_msg);
    
    IF v_is_available = 0 THEN
        p_booking_id := 0;
        p_total_amount := 0;
        p_message := v_availability_msg;
        RETURN;
    END IF;
    
    -- Get room base price
    SELECT rt.base_price INTO v_base_price
    FROM rooms r
    JOIN room_types rt ON r.type_id = rt.type_id
    WHERE r.room_id = p_room_id;
    
    -- Calculate number of nights
    v_nights := p_check_out_date - p_check_in_date;
    
    -- Check if customer is VIP and get discount
    SELECT COUNT(*), NVL(MAX(vm.discount_percentage), 0)
    INTO v_is_vip, v_discount_percentage
    FROM vip_members vm
    WHERE vm.customer_id = p_customer_id
    AND vm.is_active = 'Y'
    AND (vm.membership_end_date IS NULL OR vm.membership_end_date >= SYSDATE);
    
    -- Calculate total amount
    p_total_amount := v_base_price * v_nights;
    
    -- Apply VIP discount if applicable
    IF v_is_vip > 0 THEN
        v_discount_amount := p_total_amount * (v_discount_percentage / 100);
        p_total_amount := p_total_amount - v_discount_amount;
    END IF;
    
    -- Create booking
    INSERT INTO bookings (
        booking_id, customer_id, room_id, check_in_date, check_out_date,
        total_amount, discount_applied, special_requests, booking_status
    ) VALUES (
        booking_seq.NEXTVAL, p_customer_id, p_room_id, p_check_in_date, p_check_out_date,
        p_total_amount, v_discount_amount, p_special_requests, 'CONFIRMED'
    ) RETURNING booking_id INTO p_booking_id;
    
    -- Update room status to RESERVED
    UPDATE rooms SET status = 'RESERVED' WHERE room_id = p_room_id;
    
    p_message := 'Booking created successfully';
    
    COMMIT;
    
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        p_booking_id := 0;
        p_total_amount := 0;
        p_message := 'Error creating booking: ' || SQLERRM;
END create_booking;
/

-- Procedure to check in a customer
CREATE OR REPLACE PROCEDURE check_in_customer(
    p_booking_id IN NUMBER,
    p_message OUT VARCHAR2
) AS
    v_booking_status VARCHAR2(20);
    v_room_id NUMBER;
BEGIN
    -- Get booking details
    SELECT booking_status, room_id 
    INTO v_booking_status, v_room_id
    FROM bookings 
    WHERE booking_id = p_booking_id;
    
    -- Check if booking is confirmed
    IF v_booking_status != 'CONFIRMED' THEN
        p_message := 'Booking is not in confirmed status. Current status: ' || v_booking_status;
        RETURN;
    END IF;
    
    -- Update booking status and actual check-in date
    UPDATE bookings 
    SET booking_status = 'CHECKED_IN',
        actual_check_in = SYSDATE
    WHERE booking_id = p_booking_id;
    
    -- Update room status to OCCUPIED
    UPDATE rooms SET status = 'OCCUPIED' WHERE room_id = v_room_id;
    
    p_message := 'Customer checked in successfully';
    
    COMMIT;
    
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        p_message := 'Booking not found';
    WHEN OTHERS THEN
        ROLLBACK;
        p_message := 'Error during check-in: ' || SQLERRM;
END check_in_customer;
/

-- Procedure to check out a customer with extra charge calculation
CREATE OR REPLACE PROCEDURE check_out_customer(
    p_booking_id IN NUMBER,
    p_message OUT VARCHAR2
) AS
    v_booking_status VARCHAR2(20);
    v_room_id NUMBER;
    v_check_out_date DATE;
    v_base_price NUMBER(10,2);
    v_extra_hours NUMBER;
    v_extra_charges NUMBER(12,2) := 0;
    v_hourly_rate NUMBER(10,2);
BEGIN
    -- Get booking details
    SELECT b.booking_status, b.room_id, b.check_out_date, rt.base_price
    INTO v_booking_status, v_room_id, v_check_out_date, v_base_price
    FROM bookings b
    JOIN rooms r ON b.room_id = r.room_id
    JOIN room_types rt ON r.type_id = rt.type_id
    WHERE b.booking_id = p_booking_id;
    
    -- Check if customer is checked in
    IF v_booking_status != 'CHECKED_IN' THEN
        p_message := 'Customer is not checked in. Current status: ' || v_booking_status;
        RETURN;
    END IF;
    
    -- Calculate extra charges if checkout is late
    IF SYSDATE > v_check_out_date THEN
        v_extra_hours := CEIL((SYSDATE - v_check_out_date) * 24);
        v_hourly_rate := v_base_price / 24; -- Hourly rate based on daily rate
        v_extra_charges := v_extra_hours * v_hourly_rate;
    END IF;
    
    -- Update booking status and actual check-out date
    UPDATE bookings 
    SET booking_status = 'CHECKED_OUT',
        actual_check_out = SYSDATE,
        extra_charges = v_extra_charges,
        total_amount = total_amount + v_extra_charges
    WHERE booking_id = p_booking_id;
    
    -- Update room status to AVAILABLE
    UPDATE rooms SET status = 'AVAILABLE' WHERE room_id = v_room_id;
    
    -- Update customer's total spent
    UPDATE customers 
    SET total_spent = total_spent + v_extra_charges
    WHERE customer_id = (SELECT customer_id FROM bookings WHERE booking_id = p_booking_id);
    
    IF v_extra_charges > 0 THEN
        p_message := 'Customer checked out successfully. Extra charges applied: $' || TO_CHAR(v_extra_charges, '999999.99');
    ELSE
        p_message := 'Customer checked out successfully';
    END IF;
    
    COMMIT;
    
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        p_message := 'Booking not found';
    WHEN OTHERS THEN
        ROLLBACK;
        p_message := 'Error during check-out: ' || SQLERRM;
END check_out_customer;
/

-- Procedure to cancel a booking
CREATE OR REPLACE PROCEDURE cancel_booking(
    p_booking_id IN NUMBER,
    p_message OUT VARCHAR2
) AS
    v_booking_status VARCHAR2(20);
    v_room_id NUMBER;
BEGIN
    -- Get booking details
    SELECT booking_status, room_id 
    INTO v_booking_status, v_room_id
    FROM bookings 
    WHERE booking_id = p_booking_id;
    
    -- Check if booking can be cancelled
    IF v_booking_status IN ('CHECKED_OUT', 'CANCELLED') THEN
        p_message := 'Booking cannot be cancelled. Current status: ' || v_booking_status;
        RETURN;
    END IF;
    
    -- Update booking status
    UPDATE bookings 
    SET booking_status = 'CANCELLED',
        payment_status = 'REFUNDED'
    WHERE booking_id = p_booking_id;
    
    -- Update room status to AVAILABLE if it was reserved
    UPDATE rooms 
    SET status = 'AVAILABLE' 
    WHERE room_id = v_room_id 
    AND status IN ('RESERVED', 'OCCUPIED');
    
    p_message := 'Booking cancelled successfully';
    
    COMMIT;
    
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        p_message := 'Booking not found';
    WHEN OTHERS THEN
        ROLLBACK;
        p_message := 'Error cancelling booking: ' || SQLERRM;
END cancel_booking;
/

