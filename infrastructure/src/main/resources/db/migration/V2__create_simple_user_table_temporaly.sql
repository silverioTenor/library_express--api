-- Created early due to US-1502
-- This table will be rebuilt in US-1601
CREATE TABLE tb_user (
     id VARCHAR(36) PRIMARY KEY,
     name VARCHAR(150) NOT NULL,
     email VARCHAR(255) NOT NULL UNIQUE
);

CREATE INDEX idx_user_id ON tb_user(id);