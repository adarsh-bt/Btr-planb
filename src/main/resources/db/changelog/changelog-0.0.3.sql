
CREATE TABLE keyplot_selections (
                                    kp_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                                    btr_id BIGINT NOT NULL,
                                    zone_id INTEGER NOT NULL REFERENCES tbl_master_zone(zone_id) ON DELETE CASCADE,

                                    intervals INTEGER,
                                    selected_date DATE DEFAULT CURRENT_DATE,
                                    agri_start_year DATE,
                                    agri_end_year DATE,

                                    is_rejected BOOLEAN,
                                    reason TEXT,
                                    reject_date DATE,
                                    status BOOLEAN,

                                    land_type VARCHAR(10),  -- Expected values: 'WET', 'DRY'
                                    localbody INTEGER,      -- Changed from TEXT to INTEGER
                                    created_by UUID NOT NULL,

                                    owner_name TEXT,
                                    address TEXT,
                                    phone_number VARCHAR(15),
                                    details_updatedby UUID,
                                    geocoordinate VARCHAR(255),


                                    CONSTRAINT fk_btr FOREIGN KEY (btr_id) REFERENCES tbl_btr_data(id),
                                    CONSTRAINT fk_localbody FOREIGN KEY (localbody) REFERENCES tbl_master_localbody(localbody_id)

);
