-- ======================================================
-- 03_triggers_and_functions.sql
-- Triggers and Functions for Hotel Reservation System
-- ======================================================

-- ======================================================
-- FUNCTIONS
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

-- Calculate room price for a stay
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

-- Format booking summary
CREATE OR REPLACE FUNCTION format_booking_summary(
    p_booking_id IN NUMBER
) RETURN VARCHAR2 AS
    v_summary VARCHAR2(1000);
    v_customer_name VARCHAR2(100);
    v_room_number VARCHAR2(10);
    v_check_in DATE;
    v_check_out DATE;
    v_status VARCHAR2(20);
BEGIN
    SELECT
        c.first_name || ' ' || c.last_name,
        r.room_number,
        b.check_in_date,
        b.check_out_date,
        b.booking_status
    INTO
        v_customer_name,
        v_room_number,
        v_check_in,
        v_check_out,
        v_status
    FROM bookings b
    JOIN customers c ON b.customer_id = c.customer_id
    JOIN rooms r ON b.room_id = r.room_id
    WHERE b.booking_id = p_booking_id;

    v_summary := 'Booking #' || p_booking_id || ': ' ||
                 v_customer_name || ' - Room ' || v_room_number || ', ' ||
                 TO_CHAR(v_check_in, 'YYYY-MM-DD') || ' to ' ||
                 TO_CHAR(v_check_out, 'YYYY-MM-DD') || ' (' ||
                 (v_check_out - v_check_in) || ' days) - ' || v_status;

    RETURN v_summary;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RETURN 'Booking not found';
END format_booking_summary;
/

-- Get booking count for a customer
CREATE OR REPLACE FUNCTION get_customer_booking_count(
    p_customer_id IN NUMBER,
    p_include_cancelled IN CHAR DEFAULT 'N'
) RETURN NUMBER AS
    v_count NUMBER;
BEGIN
    IF p_include_cancelled = 'Y' THEN
        SELECT COUNT(*) INTO v_count
        FROM bookings
        WHERE customer_id = p_customer_id;
    ELSE
        SELECT COUNT(*) INTO v_count
        FROM bookings
        WHERE customer_id = p_customer_id
        AND booking_status != 'CANCELLED';
    END IF;

    RETURN v_count;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RETURN 0;
END get_customer_booking_count;
/

-- Get room occupancy percentage for a period
CREATE OR REPLACE FUNCTION get_room_occupancy(
    p_room_id IN NUMBER,
    p_start_date IN DATE,
    p_end_date IN DATE
) RETURN NUMBER AS
    v_occupied_days NUMBER := 0;
    v_total_days NUMBER;
    v_occupancy NUMBER;
BEGIN
    -- Calculate total days in period
    v_total_days := p_end_date - p_start_date;

    -- Calculate occupied days
    SELECT NVL(SUM(
        LEAST(check_out_date, p_end_date) -
        GREATEST(check_in_date, p_start_date)
    ), 0) INTO v_occupied_days
    FROM bookings
    WHERE room_id = p_room_id
    AND booking_status IN ('CONFIRMED', 'CHECKED_IN', 'CHECKED_OUT')
    AND check_in_date < p_end_date
    AND check_out_date > p_start_date;

    -- Calculate occupancy percentage
    IF v_total_days > 0 THEN
        v_occupancy := (v_occupied_days / v_total_days) * 100;
    ELSE
        v_occupancy := 0;
    END IF;

    RETURN ROUND(v_occupancy, 2);
END get_room_occupancy;
/

-- ======================================================
-- TRIGGERS
-- ======================================================

-- Automatically update room status when booking status changes
CREATE OR REPLACE TRIGGER trg_booking_status_change
AFTER UPDATE OF booking_status ON bookings
FOR EACH ROW
BEGIN
    -- Room becomes occupied when guest checks in
    IF :OLD.booking_status = 'CONFIRMED' AND :NEW.booking_status = 'CHECKED_IN' THEN
        UPDATE rooms SET status = 'OCCUPIED' WHERE room_id = :NEW.room_id;

    -- Room becomes available when guest checks out
    ELSIF :OLD.booking_status = 'CHECKED_IN' AND :NEW.booking_status = 'CHECKED_OUT' THEN
        UPDATE rooms SET status = 'AVAILABLE', last_cleaned = NULL WHERE room_id = :NEW.room_id;

    -- Room becomes available when booking is cancelled
    ELSIF :OLD.booking_status IN ('CONFIRMED', 'CHECKED_IN') AND :NEW.booking_status = 'CANCELLED' THEN
        UPDATE rooms SET status = 'AVAILABLE' WHERE room_id = :NEW.room_id;
    END IF;
END;
/

-- Automatically update customer VIP status when total spent increases
CREATE OR REPLACE TRIGGER trg_customer_spending_update
AFTER UPDATE OF total_spent ON customers
FOR EACH ROW
WHEN (NEW.total_spent > OLD.total_spent)
BEGIN
    -- Update VIP status when spending thresholds are met
    IF :NEW.total_spent >= 5000 AND :OLD.total_spent < 5000 THEN
        update_vip_status(:NEW.customer_id);
    ELSIF :NEW.total_spent >= 10000 AND :OLD.total_spent < 10000 THEN
        update_vip_status(:NEW.customer_id);
    ELSIF :NEW.total_spent >= 20000 AND :OLD.total_spent < 20000 THEN
        update_vip_status(:NEW.customer_id);
    END IF;
END;
/

-- Archive booking when it's checked out
CREATE OR REPLACE TRIGGER trg_archive_completed_booking
AFTER UPDATE OF booking_status ON bookings
FOR EACH ROW
WHEN (NEW.booking_status = 'CHECKED_OUT' AND OLD.booking_status = 'CHECKED_IN')
DECLARE
    v_archive_days NUMBER := 365;  -- Archive after 1 year
BEGIN
    -- Create job to archive this booking after 365 days
    DBMS_SCHEDULER.CREATE_JOB (
        job_name => 'ARCHIVE_BOOKING_' || :NEW.booking_id,
        job_type => 'PLSQL_BLOCK',
        job_action => 'BEGIN
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
                          WHERE booking_id = ' || :NEW.booking_id || ';

                          DELETE FROM bookings
                          WHERE booking_id = ' || :NEW.booking_id || ';

                          COMMIT;
                       END;',
        start_date => SYSTIMESTAMP + INTERVAL 'DAY' v_archive_days,
        enabled => TRUE,
        auto_drop => TRUE
    );
EXCEPTION
    WHEN OTHERS THEN
        -- If we can't create a job, just log the error
        NULL;
END;
/

-- Automatically update last_updated in customers table
CREATE OR REPLACE TRIGGER trg_customer_last_updated
BEFORE UPDATE ON customers
FOR EACH ROW
BEGIN
    :NEW.last_updated := SYSDATE;
END;
/

-- Prevent deleting active bookings
CREATE OR REPLACE TRIGGER trg_prevent_active_booking_delete
BEFORE DELETE ON bookings
FOR EACH ROW
BEGIN
    IF :OLD.booking_status IN ('CONFIRMED', 'CHECKED_IN') THEN
        RAISE_APPLICATION_ERROR(-20101, 'Cannot delete active bookings. Please cancel the booking first.');
    END IF;
END;
/

-- Prevent changes to checked-out bookings
CREATE OR REPLACE TRIGGER trg_prevent_completed_booking_update
BEFORE UPDATE ON bookings
FOR EACH ROW
BEGIN
    IF :OLD.booking_status = 'CHECKED_OUT' AND
       (:NEW.check_in_date != :OLD.check_in_date OR
        :NEW.check_out_date != :OLD.check_out_date OR
        :NEW.room_id != :OLD.room_id OR
        :NEW.customer_id != :OLD.customer_id) THEN
        RAISE_APPLICATION_ERROR(-20102, 'Cannot modify core details of completed bookings.');
    END IF;
END;
/

-- Update invoice due date when invoice date changes
CREATE OR REPLACE TRIGGER trg_invoice_date_change
BEFORE INSERT OR UPDATE OF invoice_date ON invoices
FOR EACH ROW
BEGIN
    IF :NEW.due_date IS NULL THEN
        :NEW.due_date := :NEW.invoice_date + 15;  -- 15 days payment terms
    END IF;
END;
/

-- Validate room availability when inserting new booking
CREATE OR REPLACE TRIGGER trg_validate_room_availability
BEFORE INSERT ON bookings
FOR EACH ROW
DECLARE
    v_room_available NUMBER;
    v_room_status VARCHAR2(20);
BEGIN
    -- Check if the room is available for the requested dates
    SELECT COUNT(*) INTO v_room_available
    FROM bookings
    WHERE room_id = :NEW.room_id
    AND booking_status IN ('CONFIRMED', 'CHECKED_IN')
    AND ((:NEW.check_in_date BETWEEN check_in_date AND check_out_date - 1)
         OR (:NEW.check_out_date - 1 BETWEEN check_in_date AND check_out_date - 1)
         OR (check_in_date BETWEEN :NEW.check_in_date AND :NEW.check_out_date - 1));

    -- Check room status
    SELECT status INTO v_room_status
    FROM rooms
    WHERE room_id = :NEW.room_id;

    -- Validate
    IF v_room_available > 0 THEN
        RAISE_APPLICATION_ERROR(-20103, 'Room is already booked for the selected dates');
    END IF;

    IF v_room_status != 'AVAILABLE' THEN
        RAISE_APPLICATION_ERROR(-20104, 'Room is not available (status: ' || v_room_status || ')');
    END IF;

    -- Ensure check-out is after check-in
    IF :NEW.check_out_date <= :NEW.check_in_date THEN
        RAISE_APPLICATION_ERROR(-20105, 'Check-out date must be after check-in date');
    END IF;
END;
/
