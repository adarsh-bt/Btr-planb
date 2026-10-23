CREATE TABLE keyplot_deletion_audit (
    audit_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    request_id UUID NOT NULL UNIQUE,

    zone_id BIGINT NOT NULL,
    agri_year VARCHAR(9) NOT NULL,

    requested_by UUID NOT NULL,
    approved_by UUID NOT NULL,

    deletion_reason TEXT NOT NULL,
    admin_remark TEXT,

    keyplots_deleted INTEGER NOT NULL DEFAULT 0,
    cluster_master_deleted INTEGER NOT NULL DEFAULT 0,
    cluster_details_deleted INTEGER NOT NULL DEFAULT 0,
    tour_diary_deleted INTEGER NOT NULL DEFAULT 0,
    form_data_deleted INTEGER NOT NULL DEFAULT 0,
    nuc_deleted INTEGER NOT NULL DEFAULT 0,
    crop_records_deleted INTEGER NOT NULL DEFAULT 0,

    status VARCHAR(30) NOT NULL,
    requested_at TIMESTAMPTZ NOT NULL,
    approved_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ,

    deletion_summary JSONB,
    failure_details JSONB,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE keyplot_deletion_request (
    request_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    request_number VARCHAR(40) NOT NULL UNIQUE,

    keyplot_id UUID NOT NULL,
    zone_id BIGINT NOT NULL,
    agri_year VARCHAR(9) NOT NULL,

    reason TEXT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',

    requested_by UUID NOT NULL,
    requested_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    reviewed_by UUID,
    reviewed_at TIMESTAMPTZ,
    admin_remark TEXT,

    impact_snapshot JSONB,
    execution_started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    failure_reason TEXT,

    correlation_id UUID NOT NULL DEFAULT gen_random_uuid(),
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT ck_keyplot_deletion_request_status
        CHECK (status IN (
            'PENDING', 'APPROVED', 'IN_PROGRESS',
            'RECONCILIATION_REQUIRED', 'COMPLETED',
            'FAILED', 'REJECTED', 'CANCELLED'
        ))
);

CREATE INDEX idx_kp_deletion_request_status
    ON keyplot_deletion_request(status, requested_at DESC);

CREATE INDEX idx_kp_deletion_request_keyplot
    ON keyplot_deletion_request(keyplot_id, status);

CREATE UNIQUE INDEX uq_active_keyplot_deletion_request
    ON keyplot_deletion_request(keyplot_id)
    WHERE status IN (
        'PENDING', 'APPROVED', 'IN_PROGRESS',
        'RECONCILIATION_REQUIRED'
    );



    ALTER TABLE keyplot_deletion_request
    ADD COLUMN cluster_number INTEGER;