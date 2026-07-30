ALTER TABLE tbl_work_allocation_approval
ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN revoked_by UUID,
ADD COLUMN revoked_remark TEXT,
ADD COLUMN revoked_at TIMESTAMP