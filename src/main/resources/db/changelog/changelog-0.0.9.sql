CREATE TABLE village_localbody_map (
    id BIGSERIAL PRIMARY KEY,
    village_id BIGINT NOT NULL,
    localbody_id BIGINT NOT NULL,
    is_active BOOLEAN NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_village FOREIGN KEY (village_id) REFERENCES tbl_master_village(village_id),
    CONSTRAINT fk_localbody FOREIGN KEY (localbody_id) REFERENCES tbl_master_localbody(localbody_id)
);
