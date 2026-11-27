-- At most one SUPER_ADMIN (role.id = 1) may exist. The predicate must be a
-- constant, which is why role ids are seeded explicitly.
CREATE UNIQUE INDEX uq_admin_user_single_super_admin
    ON admin_user (role_id)
    WHERE role_id = 1;