CREATE TABLE tbl_work_allocation_verification (

    id BIGSERIAL PRIMARY KEY,

    approval_id BIGINT NOT NULL UNIQUE,

    verified_by UUID,

    status VARCHAR(20) DEFAULT 'PENDING',

    remarks TEXT,

    verified_at TIMESTAMP,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    is_active BOOLEAN DEFAULT TRUE,

    CONSTRAINT fk_work_allocation_verification_approval
        FOREIGN KEY (approval_id)
        REFERENCES tbl_work_allocation_approval(id)
        ON DELETE CASCADE

);
