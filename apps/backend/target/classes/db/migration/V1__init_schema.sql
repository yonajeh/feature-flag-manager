CREATE TABLE applications (
    id TEXT PRIMARY KEY NOT NULL,
    name VARCHAR(64) NOT NULL UNIQUE,
    display_name VARCHAR(255) NOT NULL,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL
);

CREATE TABLE api_tokens (
    id TEXT PRIMARY KEY NOT NULL,
    application_id TEXT NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    token_hash VARCHAR(64) NOT NULL,
    token_prefix VARCHAR(8) NOT NULL,
    status VARCHAR(16) NOT NULL,
    last_used_at INTEGER,
    expires_at INTEGER,
    created_at INTEGER NOT NULL,
    revoked_at INTEGER
);

CREATE INDEX idx_api_tokens_token_hash ON api_tokens(token_hash);
CREATE INDEX idx_api_tokens_application_id ON api_tokens(application_id);

CREATE TABLE feature_flags (
    id TEXT PRIMARY KEY NOT NULL,
    application_id TEXT NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    flag_key VARCHAR(128) NOT NULL,
    enabled INTEGER NOT NULL DEFAULT 0,
    description TEXT,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL,
    CONSTRAINT uq_feature_flags_app_key UNIQUE (application_id, flag_key)
);

CREATE INDEX idx_feature_flags_application_id ON feature_flags(application_id);
