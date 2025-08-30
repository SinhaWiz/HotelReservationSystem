-- ======================================================
-- 01_create_tables.sql
-- Schema definition for Hotel Reservation System
-- ======================================================

-- Remove existing objects (for clean installation)
BEGIN
  FOR t IN (SELECT table_name FROM user_tables WHERE table_name IN (
    'INVOICE_LINE_ITEMS','INVOICES','CUSTOMER_SERVICE_USAGE','ROOM_SERVICE_ASSIGNMENTS',
    'ROOM_SERVICES','BOOKING_ARCHIVE','BOOKINGS','VIP_MEMBERS','CUSTOMERS','ROOMS','ROOM_TYPES')) LOOP
    EXECUTE IMMEDIATE 'DROP TABLE '||t.table_name||' CASCADE CONSTRAINTS';
  END LOOP;
  FOR s IN (SELECT sequence_name FROM user_sequences WHERE sequence_name IN (
    'ROOM_TYPE_SEQ','ROOM_SEQ','CUSTOMER_SEQ','BOOKING_SEQ','VIP_SEQ','BOOKING_ARCHIVE_SEQ',
    'ROOM_SERVICE_SEQ','ASSIGNMENT_SEQ','USAGE_SEQ','INVOICE_SEQ','LINE_ITEM_SEQ')) LOOP
    EXECUTE IMMEDIATE 'DROP SEQUENCE '||s.sequence_name;
  END LOOP;
EXCEPTION WHEN OTHERS THEN NULL; END;
/

-- ======================================================
-- CORE TABLES
-- ======================================================

-- ROOM TYPES
CREATE TABLE room_types (
  type_id        NUMBER(10) PRIMARY KEY,
  type_name      VARCHAR2(50) NOT NULL UNIQUE,
  base_price     NUMBER(10,2) NOT NULL,
  max_occupancy  NUMBER(2) NOT NULL,
  amenities      VARCHAR2(500),
  description    VARCHAR2(500),
  created_date   DATE DEFAULT SYSDATE
);

-- ROOMS
CREATE TABLE rooms (
  room_id         NUMBER(10) PRIMARY KEY,
  room_number     VARCHAR2(10) NOT NULL UNIQUE,
  type_id         NUMBER(10) NOT NULL REFERENCES room_types(type_id),
  floor_number    NUMBER(3),
  status          VARCHAR2(20) DEFAULT 'AVAILABLE' CHECK (status IN ('AVAILABLE','OCCUPIED','MAINTENANCE','OUT_OF_ORDER')),
  last_maintenance DATE,
  last_cleaned    DATE,
  amenities       VARCHAR2(500),
  notes           VARCHAR2(1000),
  description     VARCHAR2(500),
  base_price      NUMBER(10,2),
  created_date    DATE DEFAULT SYSDATE
);

-- CUSTOMERS
CREATE TABLE customers (
  customer_id      NUMBER(10) PRIMARY KEY,
  first_name       VARCHAR2(50) NOT NULL,
  last_name        VARCHAR2(50) NOT NULL,
  email            VARCHAR2(100) UNIQUE,
  phone            VARCHAR2(20),
  address          VARCHAR2(200),
  date_of_birth    DATE,
  total_spent      NUMBER(12,2) DEFAULT 0,
  registration_date DATE DEFAULT SYSDATE,
  is_active        CHAR(1) DEFAULT 'Y' CHECK (is_active IN ('Y','N')),
  loyalty_points   NUMBER(10) DEFAULT 0,
  created_date     DATE DEFAULT SYSDATE,
  last_updated     DATE
);

-- VIP MEMBERS
CREATE TABLE vip_members (
  vip_id              NUMBER(10) PRIMARY KEY,
  customer_id         NUMBER(10) NOT NULL REFERENCES customers(customer_id),
  membership_level    VARCHAR2(20) NOT NULL CHECK (membership_level IN ('GOLD','PLATINUM','DIAMOND')),
  discount_percentage NUMBER(5,2) NOT NULL,
  membership_start_date DATE DEFAULT SYSDATE,
  membership_end_date DATE,
  benefits            VARCHAR2(500),
  is_active           CHAR(1) DEFAULT 'Y' CHECK (is_active IN ('Y','N'))
);

-- BOOKINGS
CREATE TABLE bookings (
  booking_id        NUMBER(10) PRIMARY KEY,
  customer_id       NUMBER(10) NOT NULL REFERENCES customers(customer_id),
  room_id           NUMBER(10) NOT NULL REFERENCES rooms(room_id),
  check_in_date     DATE NOT NULL,
  check_out_date    DATE NOT NULL,
  actual_check_in   DATE,
  actual_check_out  DATE,
  booking_date      DATE DEFAULT SYSDATE,
  late_checkout_hours NUMBER(5,2) DEFAULT 0,
  total_amount      NUMBER(12,2) NOT NULL,
  discount_applied  NUMBER(12,2) DEFAULT 0,
  extra_charges     NUMBER(12,2) DEFAULT 0,
  payment_status    VARCHAR2(20) DEFAULT 'PENDING' CHECK (payment_status IN ('PENDING','PAID','CANCELLED','REFUNDED')),
  booking_status    VARCHAR2(20) DEFAULT 'CONFIRMED' CHECK (booking_status IN ('CONFIRMED','CHECKED_IN','CHECKED_OUT','CANCELLED','NO_SHOW')),
  special_requests  VARCHAR2(500),
  created_by        VARCHAR2(50) DEFAULT USER,
  created_date      DATE DEFAULT SYSDATE,
  services_total    NUMBER(12,2) DEFAULT 0,
  CONSTRAINT chk_booking_dates CHECK (check_out_date > check_in_date)
);

-- BOOKING ARCHIVE
CREATE TABLE booking_archive (
  archive_id        NUMBER(10) PRIMARY KEY,
  booking_id        NUMBER(10),
  customer_id       NUMBER(10),
  room_id           NUMBER(10),
  check_in_date     DATE,
  check_out_date    DATE,
  actual_check_in   DATE,
  actual_check_out  DATE,
  booking_date      DATE,
  total_amount      NUMBER(12,2),
  discount_applied  NUMBER(12,2),
  extra_charges     NUMBER(12,2),
  payment_status    VARCHAR2(20),
  booking_status    VARCHAR2(20),
  special_requests  VARCHAR2(500),
  archived_date     DATE DEFAULT SYSDATE
);

-- ROOM SERVICES
CREATE TABLE room_services (
  service_id          NUMBER(10) PRIMARY KEY,
  service_name        VARCHAR2(100) NOT NULL UNIQUE,
  service_description VARCHAR2(500),
  service_category    VARCHAR2(50),
  base_price          NUMBER(10,2) NOT NULL,
  is_active           CHAR(1) DEFAULT 'Y' CHECK (is_active IN ('Y','N')),
  created_date        DATE DEFAULT SYSDATE
);

-- SERVICE ASSIGNMENTS
CREATE TABLE room_service_assignments (
  assignment_id    NUMBER(10) PRIMARY KEY,
  room_type_id     NUMBER(10) NOT NULL REFERENCES room_types(type_id),
  service_id       NUMBER(10) NOT NULL REFERENCES room_services(service_id),
  is_complimentary CHAR(1) DEFAULT 'N' CHECK (is_complimentary IN ('Y','N')),
  CONSTRAINT uk_rsa UNIQUE (room_type_id, service_id)
);

-- SERVICE USAGE
CREATE TABLE customer_service_usage (
  usage_id         NUMBER(12) PRIMARY KEY,
  booking_id       NUMBER(10) NOT NULL REFERENCES bookings(booking_id),
  customer_id      NUMBER(10) NOT NULL REFERENCES customers(customer_id),
  service_id       NUMBER(10) NOT NULL REFERENCES room_services(service_id),
  usage_date       DATE DEFAULT SYSDATE,
  quantity         NUMBER(5) DEFAULT 1,
  unit_price       NUMBER(10,2) NOT NULL,
  total_cost       NUMBER(12,2) NOT NULL,
  is_complimentary CHAR(1) DEFAULT 'N' CHECK (is_complimentary IN ('Y','N'))
);

-- INVOICES
CREATE TABLE invoices (
  invoice_id       NUMBER(12) PRIMARY KEY,
  booking_id       NUMBER(10) NOT NULL REFERENCES bookings(booking_id),
  customer_id      NUMBER(10) NOT NULL REFERENCES customers(customer_id),
  invoice_number   VARCHAR2(30) NOT NULL UNIQUE,
  invoice_date     DATE DEFAULT SYSDATE,
  due_date         DATE,
  subtotal         NUMBER(12,2) NOT NULL,
  tax_amount       NUMBER(12,2) DEFAULT 0,
  discount_amount  NUMBER(12,2) DEFAULT 0,
  total_amount     NUMBER(12,2) NOT NULL,
  payment_status   VARCHAR2(20) DEFAULT 'PENDING' CHECK (payment_status IN ('PENDING','PAID','OVERDUE','CANCELLED')),
  payment_date     DATE,
  payment_method   VARCHAR2(40),
  notes            VARCHAR2(500),
  created_by       VARCHAR2(50) DEFAULT USER,
  created_date     DATE DEFAULT SYSDATE
);

-- INVOICE LINE ITEMS
CREATE TABLE invoice_line_items (
  line_item_id     NUMBER(12) PRIMARY KEY,
  invoice_id       NUMBER(12) NOT NULL REFERENCES invoices(invoice_id),
  item_type        VARCHAR2(20) CHECK (item_type IN ('ROOM','SERVICE','TAX','DISCOUNT','EXTRA_CHARGE')),
  item_description VARCHAR2(200),
  quantity         NUMBER(6,2) DEFAULT 1,
  unit_price       NUMBER(12,2),
  line_total       NUMBER(12,2),
  service_id       NUMBER(10) REFERENCES room_services(service_id),
  usage_id         NUMBER(12) REFERENCES customer_service_usage(usage_id)
);

-- ======================================================
-- SEQUENCES
-- ======================================================
CREATE SEQUENCE room_type_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE room_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE customer_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE booking_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE vip_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE booking_archive_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE room_service_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE assignment_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE usage_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE invoice_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE line_item_seq START WITH 1 INCREMENT BY 1 NOCACHE;

-- ======================================================
-- INDEXES
-- ======================================================
CREATE INDEX idx_booking_room ON bookings(room_id);
CREATE INDEX idx_booking_customer ON bookings(customer_id);
CREATE INDEX idx_booking_status ON bookings(booking_status);
CREATE INDEX idx_customers_emails  ON customers(email);
CREATE INDEX idx_service_usage_booking ON customer_service_usage(booking_id);
CREATE INDEX idx_invoice_customer ON invoices(customer_id);
CREATE INDEX idx_invoice_booking ON invoices(booking_id);

COMMIT;
