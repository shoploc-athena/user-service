-- Make granted_by nullable (first association account created at deployment without granter)
ALTER TABLE user_permission ALTER COLUMN granted_by DROP NOT NULL;

-- Drop existing tables (no production data yet)
DROP TABLE merchant_request CASCADE;
DROP TABLE user_permission CASCADE;

-- Recreate user_permission with UUID primary key
CREATE TABLE user_permission (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    granted_by UUID REFERENCES app_user(id) ON DELETE SET NULL,
    permission VARCHAR(50) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    UNIQUE (user_id, permission),
    CHECK (granted_by <> user_id OR granted_by IS NULL),
    CHECK (permission IN ('MANAGE_AGENTS', 'VALIDATE_MERCHANTS', 'CONFIGURE_PROGRAM', 'CONFIGURE_BENEFITS', 'VIEW_INDICATORS', 'EXPORT_INDICATORS', 'PUBLISH_ANNOUNCEMENTS'))
);

-- Recreate merchant_request with UUID primary key
CREATE TABLE merchant_request (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    store_name VARCHAR(150) NOT NULL,
    store_address VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    refusal_reason VARCHAR(255),
    reviewed_by UUID REFERENCES app_user(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0,
    CHECK (status IN ('PENDING', 'ACCEPTED', 'REFUSED'))
);

CREATE INDEX idx_merchant_request_user_id ON merchant_request(user_id);
CREATE INDEX idx_merchant_request_status ON merchant_request(status);
