-- ======================================================
-- Hotel Reservation System - Triggers
-- File: 03_triggers.sql
-- Purpose: Create all database triggers for business logic
-- ======================================================

-- ======================================================
-- BOOKING MANAGEMENT TRIGGERS
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
    IF :OLD.booking_status = 'CHECKED_OUT' AND :NEW.booking_status != 'CHECKED_OUT' THEN
        RAISE_APPLICATION_ERROR(-20102, 'Cannot modify completed bookings.');
    END IF;
END;
/

-- ======================================================
-- CUSTOMER MANAGEMENT TRIGGERS
-- ======================================================

-- Automatically update last_updated timestamp in customers table
CREATE OR REPLACE TRIGGER trg_customer_last_updated
    BEFORE UPDATE ON customers
    FOR EACH ROW
BEGIN
    :NEW.last_updated := SYSDATE;
END;
/

-- Automatically update customer VIP status when total spent increases
CREATE OR REPLACE TRIGGER trg_customer_spending_update
    AFTER UPDATE OF total_spent ON customers
    FOR EACH ROW
    WHEN (NEW.total_spent > OLD.total_spent)
BEGIN
    update_vip_status(:NEW.customer_id, :NEW.total_spent);
END;
/


-- Update loyalty points when customer total spending increases
CREATE OR REPLACE TRIGGER trg_update_loyalty_points
    BEFORE UPDATE OF total_spent ON customers
    FOR EACH ROW
    WHEN (NEW.total_spent > OLD.total_spent)
BEGIN
    -- Add 1 loyalty point for every $10 spent
    :NEW.loyalty_points := :OLD.loyalty_points + FLOOR((:NEW.total_spent - :OLD.total_spent) / 10);
END;
/

-- ======================================================
-- INVOICE MANAGEMENT TRIGGERS
-- ======================================================

-- Update customer total spent when invoice is paid
CREATE OR REPLACE TRIGGER trg_invoice_payment_update
    AFTER UPDATE OF payment_status ON invoices
    FOR EACH ROW
    WHEN (NEW.payment_status = 'PAID' AND OLD.payment_status != 'PAID')
BEGIN
    -- Add invoice total to customer's total spent
    UPDATE customers
    SET total_spent = total_spent + :NEW.total_amount
    WHERE customer_id = :NEW.customer_id;
END;
/

-- Automatically set payment date when invoice is marked as paid
CREATE OR REPLACE TRIGGER trg_invoice_payment_date
    BEFORE UPDATE OF payment_status ON invoices
    FOR EACH ROW
    WHEN (NEW.payment_status = 'PAID' AND OLD.payment_status != 'PAID')
BEGIN
    :NEW.payment_date := SYSDATE;
END;
/

-- ======================================================
-- SERVICE USAGE TRIGGERS
-- ======================================================

-- NOTE: Original row-level trigger querying CUSTOMER_SERVICE_USAGE caused ORA-04091 (mutating table).
-- Replaced with a compound trigger collecting affected booking_ids and updating after statement.
CREATE OR REPLACE TRIGGER trg_update_services_total
FOR INSERT OR UPDATE OR DELETE ON customer_service_usage
COMPOUND TRIGGER
    TYPE t_booking_ids IS TABLE OF NUMBER INDEX BY PLS_INTEGER;
    g_booking_ids t_booking_ids;

    PROCEDURE add_booking_id(p_id NUMBER) IS
    BEGIN
        IF p_id IS NULL THEN RETURN; END IF;
        FOR i IN 1 .. g_booking_ids.COUNT LOOP
            IF g_booking_ids(i) = p_id THEN
                RETURN; -- already captured
            END IF;
        END LOOP;
        g_booking_ids(g_booking_ids.COUNT + 1) := p_id;
    END;

AFTER EACH ROW IS
BEGIN
    IF INSERTING OR UPDATING THEN
        add_booking_id(:NEW.booking_id);
    ELSIF DELETING THEN
        add_booking_id(:OLD.booking_id);
    END IF;
END AFTER EACH ROW;

AFTER STATEMENT IS
BEGIN
    FOR i IN 1 .. g_booking_ids.COUNT LOOP
        UPDATE bookings b
        SET services_total = (
            SELECT NVL(SUM(total_cost), 0)
            FROM customer_service_usage u
            WHERE u.booking_id = g_booking_ids(i)
        )
        WHERE b.booking_id = g_booking_ids(i);
    END LOOP;
END AFTER STATEMENT;
END;
/

-- ======================================================
-- ROOM MAINTENANCE TRIGGERS
-- ======================================================

-- Update last maintenance date when room status changes to maintenance
CREATE OR REPLACE TRIGGER trg_room_maintenance_date
    BEFORE UPDATE OF status ON rooms
    FOR EACH ROW
    WHEN (NEW.status = 'MAINTENANCE' AND OLD.status != 'MAINTENANCE')
BEGIN
    :NEW.last_maintenance := SYSDATE;
END;
/

-- Prevent room deletion if there are active bookings
CREATE OR REPLACE TRIGGER trg_prevent_room_delete_with_bookings
    BEFORE DELETE ON rooms
    FOR EACH ROW
DECLARE
    v_active_bookings NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_active_bookings
    FROM bookings
    WHERE room_id = :OLD.room_id
      AND booking_status IN ('CONFIRMED', 'CHECKED_IN');

    IF v_active_bookings > 0 THEN
        RAISE_APPLICATION_ERROR(-20103, 'Cannot delete room with active bookings.');
    END IF;
END;
/

-- ======================================================
-- ARCHIVE MANAGEMENT TRIGGERS
-- ======================================================

-- Archive completed bookings after check-out (with delay)
CREATE OR REPLACE TRIGGER trg_schedule_booking_archive
    AFTER UPDATE OF booking_status ON bookings
    FOR EACH ROW
    WHEN (NEW.booking_status = 'CHECKED_OUT' AND OLD.booking_status = 'CHECKED_IN')
DECLARE
    v_archive_days NUMBER := 365;  -- Archive after 1 year
BEGIN
    -- Create a job to archive this booking after specified days
    BEGIN
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
                start_date => SYSTIMESTAMP + INTERVAL '365' DAY,
                enabled => TRUE,
                auto_drop => TRUE
        );
    EXCEPTION
        WHEN OTHERS THEN
            -- If scheduler job creation fails, just continue
            NULL;
    END;
END;
/

-- ======================================================
-- VALIDATION TRIGGERS
-- ======================================================

-- Validate email format in customers table
CREATE OR REPLACE TRIGGER trg_validate_customer_email
    BEFORE INSERT OR UPDATE OF email ON customers
    FOR EACH ROW
    WHEN (NEW.email IS NOT NULL)
BEGIN
    IF NOT REGEXP_LIKE(:NEW.email, '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$') THEN
        RAISE_APPLICATION_ERROR(-20104, 'Invalid email format.');
    END IF;
END;
/

-- Validate room number format
CREATE OR REPLACE TRIGGER trg_validate_room_number
    BEFORE INSERT OR UPDATE OF room_number ON rooms
    FOR EACH ROW
BEGIN
    -- Room number should be numeric and between 3-4 digits
    IF NOT REGEXP_LIKE(:NEW.room_number, '^[0-9]{3,4}$') THEN
        RAISE_APPLICATION_ERROR(-20105, 'Room number must be 3-4 digits.');
    END IF;
END;
/

-- Validate booking dates
CREATE OR REPLACE TRIGGER trg_validate_booking_dates
    BEFORE INSERT OR UPDATE ON bookings
    FOR EACH ROW
BEGIN
    -- Check-in date cannot be in the past (except for same day)
    IF TRUNC(:NEW.check_in_date) < TRUNC(SYSDATE) THEN
        RAISE_APPLICATION_ERROR(-20106, 'Check-in date cannot be in the past.');
    END IF;

    -- Check-out date must be after check-in date
    IF :NEW.check_out_date <= :NEW.check_in_date THEN
        RAISE_APPLICATION_ERROR(-20107, 'Check-out date must be after check-in date.');
    END IF;

    -- Booking cannot be longer than 30 days
    IF (:NEW.check_out_date - :NEW.check_in_date) > 30 THEN
        RAISE_APPLICATION_ERROR(-20108, 'Booking cannot exceed 30 days.');
    END IF;
END;
/

-- Update customer total spent when booking is checked out
CREATE OR REPLACE TRIGGER trg_update_revenue_on_checkout
    AFTER UPDATE OF booking_status ON bookings
    FOR EACH ROW
    WHEN (NEW.booking_status = 'CHECKED_OUT' AND OLD.booking_status = 'CHECKED_IN')
DECLARE
    v_total_revenue NUMBER;
BEGIN
    -- Calculate total revenue for this booking (room + services + extra charges)
    v_total_revenue := :NEW.total_amount + NVL(:NEW.services_total, 0) + NVL(:NEW.extra_charges, 0);

    -- Update customer's total spent
    UPDATE customers
    SET total_spent = total_spent + v_total_revenue
    WHERE customer_id = :NEW.customer_id;
END;
/

COMMIT;