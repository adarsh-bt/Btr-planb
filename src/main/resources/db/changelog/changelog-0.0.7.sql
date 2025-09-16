CREATE TABLE cluster_limit_log (
                                    id BIGSERIAL PRIMARY KEY,
                                    cluster_min NUMERIC(10, 1) NOT NULL CHECK (cluster_min > 0),
                                    cluster_max NUMERIC(10, 1) NOT NULL CHECK (cluster_max > 0),
                                    tso_approval_limit NUMERIC(10, 1) NOT NULL CHECK (cluster_max > 0),

                                    added_by UUID,
                                    remarks TEXT,
                                    agri_start_year DATE NOT NULL,
                                    agri_end_year DATE NOT NULL,
                                    in_active BOOLEAN DEFAULT FALSE,
                                    edit_permitter UUID,
                                    is_edited BOOLEAN DEFAULT FALSE,
                                    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                    is_active BOOLEAN DEFAULT TRUE,
                                    CHECK (agri_end_year >= agri_start_year)
);
