 ALTER TABLE keyplots_limit_log
RENAME COLUMN in_active TO is_inactive;
ALTER TABLE cluster_master
ADD COLUMN IF NOT EXISTS is_editable BOOLEAN DEFAULT FALSE NOT NULL;


