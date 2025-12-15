ALTER TABLE tbl_btr_data
ALTER COLUMN house_number TYPE VARCHAR(255)
USING house_number::VARCHAR;
