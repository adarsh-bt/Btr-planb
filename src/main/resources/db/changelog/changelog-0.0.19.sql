CREATE TABLE cluster_edit_allowed (

    id BIGSERIAL PRIMARY KEY,

    cluster_id BIGINT NOT NULL
        REFERENCES cluster_master(clu_master_id)
        ON DELETE CASCADE,

    zone_id INTEGER NOT NULL
        REFERENCES tbl_master_zone(zone_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    total_area NUMERIC(10,2),

    requested_by UUID NOT NULL,
    approved_by UUID,

    status VARCHAR(20) DEFAULT 'PENDING',

    remarks TEXT,

    approved_at TIMESTAMP,

    is_active BOOLEAN DEFAULT TRUE,

    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);