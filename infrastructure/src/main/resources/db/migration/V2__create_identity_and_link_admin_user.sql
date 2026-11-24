-- ============================================================================
-- 1. ROLE (closed lookup; ids are explicit because US-1602 relies on role_id = 1)
-- ============================================================================
CREATE TABLE role (
     id SMALLINT PRIMARY KEY,
     name VARCHAR(30) NOT NULL UNIQUE,
     description VARCHAR(30) NULL
);

INSERT INTO role (id, name, description) VALUES
     (1, 'SA', 'Super Administrator'),
     (2, 'ADMIN', 'Administrator'),
     (3, 'SUPERVISOR', ''),
     (4, 'OPERATOR', '');

-- ============================================================================
-- 2. IDENTITY STATUS (closed lookup; ids are explicit, see ck_identity_pending_without_hash)
-- ============================================================================
CREATE TABLE identity_status (
     id SMALLINT PRIMARY KEY,
     name VARCHAR(30) NOT NULL UNIQUE
);

INSERT INTO identity_status (id, name) VALUES
     (1, 'PENDING_ACTIVATION'),
     (2, 'ACTIVE'),
     (3, 'BLOCKED');

-- ============================================================================
-- 3. IDENTITY (credentials only: no name, no role)
-- ============================================================================
CREATE TABLE identity (
     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
     email VARCHAR(255) NOT NULL,
     password_hash VARCHAR(255) NULL,
     status_id SMALLINT NOT NULL REFERENCES identity_status(id),
     failed_attempts SMALLINT NOT NULL DEFAULT 0,
     locked_until TIMESTAMP WITH TIME ZONE NULL,
     created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
     updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
     -- PENDING_ACTIVATION (id 1) has no password hash; every other status has one.
     CONSTRAINT ck_identity_pending_without_hash
         CHECK ((status_id = 1) = (password_hash IS NULL))
);

-- The e-mail is unique across the system, regardless of case.
CREATE UNIQUE INDEX ux_identity_email_lower ON identity (lower(email));

CREATE TRIGGER trigger_identity_updated_at
  BEFORE UPDATE ON identity
  FOR EACH ROW
  EXECUTE FUNCTION automatic_updated_at();

-- ============================================================================
-- 4. ADMIN USER: credentials move to identity; the user gains identity and role
-- ============================================================================
-- Dropping the column also drops its UNIQUE constraint and idx_admin_user_email.
-- Runs on an empty table (confirmed), which is what makes the NOT NULL columns safe.
ALTER TABLE admin_user DROP COLUMN email;

ALTER TABLE admin_user
    ADD COLUMN identity_id UUID NOT NULL UNIQUE REFERENCES identity(id),
    ADD COLUMN role_id SMALLINT NOT NULL REFERENCES role(id);