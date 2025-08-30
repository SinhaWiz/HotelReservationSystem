-- 03_procedures_and_triggers.sql
-- Unified minimal procedures & triggers aligned with Java DAOs
-- Run AFTER 01_create_tables.sql, customer_functions.sql, 02_insert_sample_data.sql
-- Safe to re-run (CREATE OR REPLACE used)

------------------------------------------------------------------------
-- PROCEDURES
------------------------------------------------------------------------

-- Promote (or update) a customer to a VIP level
CREATE OR REPLACE PROCEDURE promote_to_vip(
  p_customer_id IN NUMBER,
  p_level       IN VARCHAR2,
  p_vip_id      OUT NUMBER
) AS
  v_exists NUMBER;
  v_disc   NUMBER(5,2);
BEGIN
  p_vip_id := NULL;
  v_disc := CASE UPPER(p_level)
              WHEN 'DIAMOND'  THEN 20
              WHEN 'PLATINUM' THEN 15
              WHEN 'GOLD'     THEN 10
              ELSE 5
            END;

  SELECT COUNT(*) INTO v_exists FROM vip_members
   WHERE customer_id = p_customer_id AND is_active = 'Y';

  IF v_exists = 0 THEN
    INSERT INTO vip_members(
      vip_id, customer_id, membership_level, discount_percentage,
      membership_start_date, is_active, benefits)
    VALUES (
      vip_seq.NEXTVAL, p_customer_id, UPPER(p_level), v_disc,
      SYSDATE, 'Y', 'Priority Booking, Late Checkout')
    RETURNING vip_id INTO p_vip_id;
  ELSE
    UPDATE vip_members
       SET membership_level = UPPER(p_level),
           discount_percentage = v_disc,
           membership_end_date = NULL,
           is_active = 'Y'
     WHERE customer_id = p_customer_id
       AND is_active = 'Y'
    RETURNING vip_id INTO p_vip_id;
  END IF;
END promote_to_vip;
/

-- Archive bookings older than 60 days since actual_check_out
CREATE OR REPLACE PROCEDURE archive_old_data_60_days(
  p_cutoff IN DATE DEFAULT SYSDATE - 60
) AS
BEGIN
  -- Insert into archive if eligible and not already archived
  INSERT INTO booking_archive(
    archive_id, booking_id, customer_id, room_id,
    check_in_date, check_out_date, actual_check_in, actual_check_out,
    booking_date, total_amount, discount_applied, extra_charges,
    payment_status, booking_status, special_requests)
  SELECT booking_archive_seq.NEXTVAL, b.booking_id, b.customer_id, b.room_id,
         b.check_in_date, b.check_out_date, b.actual_check_in, b.actual_check_out,
         b.booking_date, b.total_amount, b.discount_applied, b.extra_charges,
         b.payment_status, b.booking_status, b.special_requests
    FROM bookings b
   WHERE b.booking_status = 'CHECKED_OUT'
     AND b.actual_check_out IS NOT NULL
     AND b.actual_check_out < p_cutoff
     AND NOT EXISTS (SELECT 1 FROM booking_archive a WHERE a.booking_id = b.booking_id);

  -- Delete only those now present in archive
  DELETE FROM bookings b
   WHERE b.booking_status='CHECKED_OUT'
     AND b.actual_check_out IS NOT NULL
     AND b.actual_check_out < p_cutoff
     AND EXISTS (SELECT 1 FROM booking_archive a WHERE a.booking_id = b.booking_id);
END archive_old_data_60_days;
/

-- Return VIP members with optional level filter via SYS_REFCURSOR
CREATE OR REPLACE PROCEDURE get_vip_members_detailed(
  p_level       IN VARCHAR2,
  p_result_cur  OUT SYS_REFCURSOR
) AS
BEGIN
  IF p_level IS NOT NULL THEN
    OPEN p_result_cur FOR
      SELECT vm.vip_id, vm.customer_id, vm.membership_level,
             vm.discount_percentage, vm.membership_start_date, vm.membership_end_date,
             vm.benefits, vm.is_active,
             c.first_name, c.last_name, c.email, c.phone,
             c.total_spent, c.loyalty_points,
             (SELECT COUNT(*) FROM bookings b WHERE b.customer_id = c.customer_id) booking_count
        FROM vip_members vm
        JOIN customers c ON vm.customer_id = c.customer_id
       WHERE vm.is_active = 'Y'
         AND vm.membership_level = UPPER(p_level)
       ORDER BY c.total_spent DESC;
  ELSE
    OPEN p_result_cur FOR
      SELECT vm.vip_id, vm.customer_id, vm.membership_level,
             vm.discount_percentage, vm.membership_start_date, vm.membership_end_date,
             vm.benefits, vm.is_active,
             c.first_name, c.last_name, c.email, c.phone,
             c.total_spent, c.loyalty_points,
             (SELECT COUNT(*) FROM bookings b WHERE b.customer_id = c.customer_id) booking_count
        FROM vip_members vm
        JOIN customers c ON vm.customer_id = c.customer_id
       WHERE vm.is_active = 'Y'
       ORDER BY c.total_spent DESC;
  END IF;
END get_vip_members_detailed;
/

-- Renew or deactivate expired VIP memberships
CREATE OR REPLACE PROCEDURE process_vip_renewals AS
BEGIN
  -- Deactivate expired memberships
  UPDATE vip_members
     SET is_active = 'N'
   WHERE is_active = 'Y'
     AND membership_end_date IS NOT NULL
     AND membership_end_date < SYSDATE;

  -- (Example extension logic could go here if auto-renew policy exists)
END process_vip_renewals;
/

-- Promote top spending non-VIP customers (limit 5) above threshold
CREATE OR REPLACE PROCEDURE promote_top_customers_to_vip(
  p_promoted_by IN VARCHAR2
) AS
  CURSOR c_top IS
    SELECT customer_id, total_spent
      FROM customers c
     WHERE NOT EXISTS (
            SELECT 1 FROM vip_members vm
             WHERE vm.customer_id = c.customer_id
               AND vm.is_active='Y')
       AND total_spent >= 5000
     ORDER BY total_spent DESC FETCH FIRST 5 ROWS ONLY;
  v_vip_id NUMBER;
  v_level  VARCHAR2(20);
BEGIN
  FOR r IN c_top LOOP
    v_level := CASE
                 WHEN r.total_spent >= 15000 THEN 'DIAMOND'
                 WHEN r.total_spent >= 10000 THEN 'PLATINUM'
                 ELSE 'GOLD'
               END;
    promote_to_vip(r.customer_id, v_level, v_vip_id);
  END LOOP;
END promote_top_customers_to_vip;
/

------------------------------------------------------------------------
-- TRIGGERS
------------------------------------------------------------------------

-- Maintain last_updated timestamp on customers
CREATE OR REPLACE TRIGGER trg_customers_timestamp
  BEFORE UPDATE ON customers
  FOR EACH ROW
BEGIN
  :NEW.last_updated := SYSDATE;
END trg_customers_timestamp;
/

-- Sync room status with booking status transitions
CREATE OR REPLACE TRIGGER trg_booking_status_room
  AFTER UPDATE OF booking_status ON bookings
  FOR EACH ROW
BEGIN
  IF :NEW.booking_status = 'CHECKED_IN' THEN
    UPDATE rooms SET status='OCCUPIED' WHERE room_id = :NEW.room_id;
  ELSIF :NEW.booking_status IN ('CHECKED_OUT','CANCELLED','NO_SHOW') THEN
    -- Make available only if no other active booking overlapping
    UPDATE rooms SET status='AVAILABLE'
     WHERE room_id = :NEW.room_id
       AND NOT EXISTS (
            SELECT 1 FROM bookings b
             WHERE b.room_id = :NEW.room_id
               AND b.booking_status IN ('CONFIRMED','CHECKED_IN'));
  END IF;
END trg_booking_status_room;
/

-- Update customer spend & loyalty points once booking first becomes CHECKED_OUT
CREATE OR REPLACE TRIGGER trg_customer_spend_loyalty
  AFTER UPDATE OF booking_status ON bookings
  FOR EACH ROW
WHEN (NEW.booking_status = 'CHECKED_OUT' AND (OLD.booking_status IS NULL OR OLD.booking_status <> 'CHECKED_OUT'))
BEGIN
  UPDATE customers
     SET total_spent    = total_spent + NVL(:NEW.total_amount,0),
         loyalty_points = loyalty_points + calculate_loyalty_points(NVL(:NEW.total_amount,0))
   WHERE customer_id = :NEW.customer_id;
END trg_customer_spend_loyalty;
/

-- Periodically (random) archive old bookings
CREATE OR REPLACE TRIGGER trg_booking_archive_maintenance
  AFTER INSERT OR UPDATE ON bookings
DECLARE
  PRAGMA AUTONOMOUS_TRANSACTION;
BEGIN
  -- ~2% chance to run each DML
  IF DBMS_RANDOM.VALUE(0,100) < 2 THEN
    archive_old_data_60_days;
    COMMIT;
  END IF;
END trg_booking_archive_maintenance;
/

------------------------------------------------------------------------
-- OPTIONAL VALIDATION QUERIES (run manually)
-- SELECT trigger_name, status FROM user_triggers WHERE trigger_name LIKE 'TRG_%';
-- SELECT object_name, status FROM user_objects WHERE object_type IN ('PROCEDURE','FUNCTION','TRIGGER') AND status='INVALID';
------------------------------------------------------------------------

