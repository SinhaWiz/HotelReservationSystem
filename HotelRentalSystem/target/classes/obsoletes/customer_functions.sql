-- CONSOLIDATED CUSTOMER / VIP FUNCTIONS (Minimal set used by Java DAOs)
-- Drop old versions (optional when running manually)
-- Aligned with: CustomerDAO (calculate_customer_discount), VIPMemberDAO (check_vip_eligibility), triggers (calculate_loyalty_points)

-- Loyalty points: 1 point per $10 spent (rounded down)
CREATE OR REPLACE FUNCTION calculate_loyalty_points(
  p_amount_spent IN NUMBER
) RETURN NUMBER AS
BEGIN
  RETURN FLOOR(NVL(p_amount_spent,0)/10);
END calculate_loyalty_points;
/

-- Discount tier: based on total_spent OR active VIP membership
CREATE OR REPLACE FUNCTION calculate_customer_discount(
  p_customer_id IN NUMBER
) RETURN NUMBER AS
  v_total NUMBER;
  v_vip_count NUMBER;
  v_vip_discount NUMBER := 0;
BEGIN
  SELECT total_spent INTO v_total FROM customers WHERE customer_id = p_customer_id;
  -- Active VIP discount (if any)
  SELECT COUNT(*), NVL(MAX(discount_percentage),0)
    INTO v_vip_count, v_vip_discount
    FROM vip_members
   WHERE customer_id = p_customer_id
     AND is_active='Y'
     AND (membership_end_date IS NULL OR membership_end_date >= SYSDATE);
  IF v_vip_count > 0 THEN
    RETURN v_vip_discount; -- VIP overrides tier logic
  END IF;
  IF v_total >= 15000 THEN RETURN 20; -- 20% high tier
  ELSIF v_total >= 10000 THEN RETURN 15; -- 15%
  ELSIF v_total >= 5000 THEN RETURN 10; -- 10%
  ELSIF v_total >= 2000 THEN RETURN 5;  -- 5%
  ELSE RETURN 0; END IF;
EXCEPTION
  WHEN NO_DATA_FOUND THEN RETURN 0;
  WHEN OTHERS THEN RETURN 0;
END calculate_customer_discount;
/

-- Eligibility check returning an indicator used by Java & (optionally) admin UI
-- Possible returns: ALREADY_VIP | DIAMOND | PLATINUM | GOLD | NOT_ELIGIBLE | CUSTOMER_NOT_FOUND | ERROR
CREATE OR REPLACE FUNCTION check_vip_eligibility(
  p_customer_id IN NUMBER,
  p_threshold   IN NUMBER DEFAULT 5000
) RETURN VARCHAR2 AS
  v_total NUMBER;
  v_is_vip NUMBER;
BEGIN
  SELECT total_spent INTO v_total FROM customers WHERE customer_id = p_customer_id;
  SELECT COUNT(*) INTO v_is_vip FROM vip_members WHERE customer_id=p_customer_id AND is_active='Y';
  IF v_is_vip > 0 THEN RETURN 'ALREADY_VIP'; END IF;
  IF v_total >= 15000 THEN RETURN 'DIAMOND';
  ELSIF v_total >= 10000 THEN RETURN 'PLATINUM';
  ELSIF v_total >= p_threshold THEN RETURN 'GOLD';
  ELSE RETURN 'NOT_ELIGIBLE'; END IF;
EXCEPTION
  WHEN NO_DATA_FOUND THEN RETURN 'CUSTOMER_NOT_FOUND';
  WHEN OTHERS THEN RETURN 'ERROR';
END check_vip_eligibility;
/

