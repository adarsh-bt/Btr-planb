CREATE TABLE tbl_work_allocation_approval (
    id BIGSERIAL PRIMARY KEY,

    requested_by UUID,
    approved_by UUID,

    status VARCHAR(20) DEFAULT 'PENDING',
    approved_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    remark VARCHAR(255),

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    is_active BOOLEAN DEFAULT TRUE

);

ALTER TABLE tbl_work_allocation
ADD COLUMN approval_id BIGINT,
ADD COLUMN is_edit BOOLEAN DEFAULT FALSE;

ALTER TABLE tbl_work_allocation
ADD CONSTRAINT fk_work_allocation_approval
FOREIGN KEY (approval_id)
REFERENCES tbl_work_allocation_approval(id)
ON DELETE SET NULL;

