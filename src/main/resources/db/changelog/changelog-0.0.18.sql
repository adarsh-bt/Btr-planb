ALTER TABLE keyplots_limit_log
RENAME COLUMN in_active TO is_inactive;


ALTER TABLE cluster_details
ADD COLUMN display_order INTEGER;
UPDATE cluster_details
SET display_order = clu_detail_id;
