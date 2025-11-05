ALTER TABLE tbl_zone_season_schedule
ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Modify tbl_zone_season_schedule to replace cluster_type with frame_id reference
ALTER TABLE tbl_zone_season_schedule
DROP COLUMN IF EXISTS cluster_type,
ADD COLUMN frame_id BIGINT NOT NULL,
ADD CONSTRAINT fk_zone_schedule_frame
    FOREIGN KEY (frame_id)
    REFERENCES tbl_master_frame(frame_id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT;


-- Step 1: Change tb_subdivision_no from INT to VARCHAR(255)
ALTER TABLE tbl_btr_data
ALTER COLUMN tb_subdivision_no TYPE VARCHAR(255);

-- Step 2: Add is_active and uuid columns
ALTER TABLE tbl_btr_data
ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE NOT NULL,
ADD COLUMN IF NOT EXISTS uuid UUID DEFAULT gen_random_uuid() NOT NULL;
