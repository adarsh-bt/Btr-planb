CREATE TABLE keyplots_limit_log (
                                    id BIGSERIAL PRIMARY KEY,
                                    keyplots_limit BIGINT NOT NULL CHECK (keyplots_limit > 0),
                                    in_active BOOLEAN DEFAULT FALSE,
                                    added_by UUID,
                                    remarks TEXT,
                                    agri_start_year DATE NOT NULL,
                                    agri_end_year DATE NOT NULL,
                                    is_active BOOLEAN DEFAULT TRUE,
                                    edit_permitter UUID,
                                    is_edited BOOLEAN DEFAULT FALSE,
                                    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                    CHECK (agri_end_year >= agri_start_year)
);
