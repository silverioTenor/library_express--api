-- ============================================================================
-- 1. ADMIN USER
-- ============================================================================
-- Renamed from tb_user to admin_user to avoid PostgreSQL reserved word conflicts.
CREATE TABLE admin_user (
     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
     name VARCHAR(150) NOT NULL,
     email VARCHAR(255) NOT NULL UNIQUE,
     created_by UUID REFERENCES admin_user(id) ON DELETE SET NULL,
     updated_by UUID REFERENCES admin_user(id) ON DELETE SET NULL,
     created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
     updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Note: Primary keys automatically create an implicit unique index in Postgres.
-- Explicit PK indexes were removed to avoid duplication, keeping only audit/FK indexes if needed.
CREATE INDEX idx_admin_user_email ON admin_user(email);

-- ============================================================================
-- 2. CUSTOMER
-- ============================================================================
CREATE TABLE customer (
     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
     name VARCHAR(150) NOT NULL,
     email VARCHAR(255) NOT NULL UNIQUE,
     created_by UUID REFERENCES admin_user(id) ON DELETE SET NULL,
     updated_by UUID REFERENCES admin_user(id) ON DELETE SET NULL,
     created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
     updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_customer_email ON customer(email);

-- ============================================================================
-- 3. BOOK
-- ============================================================================
CREATE TABLE book (
     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
     isbn VARCHAR(17) NOT NULL UNIQUE, -- Fixed: Table cannot have two PRIMARY KEY declarations.
     title VARCHAR(200) NOT NULL,
     author TEXT[] NOT NULL,
     publication_year INT NOT NULL,
     status VARCHAR(20) NOT NULL,
     category TEXT[] NULL,
     description VARCHAR(500) NULL,
     created_by UUID REFERENCES admin_user(id) ON DELETE SET NULL,
     updated_by UUID REFERENCES admin_user(id) ON DELETE SET NULL,
     created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
     updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- ============================================================================
-- 4. LOAN
-- ============================================================================
CREATE TABLE loan (
     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
     book_id UUID NOT NULL REFERENCES book(id), -- Inline reference for clarity
     customer_id UUID NOT NULL REFERENCES customer(id), -- Inline reference for clarity
     status VARCHAR(20) NOT NULL,
     start_date DATE NOT NULL,
     end_date DATE NOT NULL,
     created_by UUID REFERENCES admin_user(id) ON DELETE SET NULL,
     updated_by UUID REFERENCES admin_user(id) ON DELETE SET NULL,
     created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
     updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
     closed_at TIMESTAMP WITH TIME ZONE NULL, -- Fixed: Removed wrong DEFAULT CURRENT_TIMESTAMP for nullable audit
     closed_by UUID REFERENCES admin_user(id) ON DELETE SET NULL -- Fixed: Changed from TIMESTAMP to UUID
);

-- Performance indexes for relational foreign key lookups
CREATE INDEX idx_loan_book_id ON loan(book_id);
CREATE INDEX idx_loan_customer_id ON loan(customer_id);

-- ============================================================================
-- 5. TRIGGERS (AUTOMATIC UPDATED_AT)
-- ============================================================================
CREATE OR REPLACE FUNCTION automatic_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = CURRENT_TIMESTAMP;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ADMIN USER TRIGGER
CREATE TRIGGER trigger_admin_user_updated_at
  BEFORE UPDATE ON admin_user
  FOR EACH ROW
  EXECUTE FUNCTION automatic_updated_at();

-- CUSTOMER TRIGGER
CREATE TRIGGER trigger_customer_updated_at
  BEFORE UPDATE ON customer
  FOR EACH ROW
  EXECUTE FUNCTION automatic_updated_at();

-- BOOK TRIGGER
CREATE TRIGGER trigger_book_updated_at
  BEFORE UPDATE ON book
  FOR EACH ROW
  EXECUTE FUNCTION automatic_updated_at();

-- LOAN TRIGGER
CREATE TRIGGER trigger_loan_updated_at
  BEFORE UPDATE ON loan
  FOR EACH ROW
  EXECUTE FUNCTION automatic_updated_at();
