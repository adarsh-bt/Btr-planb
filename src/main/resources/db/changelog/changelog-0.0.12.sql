CREATE TABLE tbl_zone_season_schedule (
    schedule_id BIGSERIAL PRIMARY KEY,
    zone_id INTEGER NOT NULL,
    season_id BIGINT NOT NULL,
    cluster_type VARCHAR(50) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    extended_date DATE,
    year INTEGER NOT NULL,
    uuid UUID DEFAULT gen_random_uuid() NOT NULL,
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    remark VARCHAR(255),
    CONSTRAINT fk_zone_schedule_zone
        FOREIGN KEY (zone_id)
        REFERENCES tbl_master_zone (zone_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_zone_schedule_season
        FOREIGN KEY (season_id)
        REFERENCES season_master_tbl (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);
