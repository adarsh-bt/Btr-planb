ALTER TABLE tbl_btr_data
RENAME COLUMN main_number TO old_survey_number;

ALTER TABLE tbl_btr_data
RENAME COLUMN sub_main_number TO old_subdivision_number;

ALTER TABLE tbl_btr_data
ADD COLUMN ward_number INTEGER;

ALTER TABLE tbl_btr_data
ADD COLUMN cl_no VARCHAR(200);