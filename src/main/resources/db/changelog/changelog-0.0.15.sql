ALTER TABLE tbl_zone_season_schedule
RENAME COLUMN uuid TO userid;

ALTER TABLE season_master_tbl
RENAME COLUMN uuid TO userid;

ALTER TABLE tbl_btr_data
RENAME COLUMN uuid TO userid;

