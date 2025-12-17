
CREATE TABLE crop_rejection_log (
                                    id BIGSERIAL PRIMARY KEY,

                                    crop_id BIGINT NOT NULL,
                                    cluster_id BIGINT,
                                    keyplot_id UUID,
                                    zone_id BIGINT,

                                    land_type VARCHAR(50),

                                    is_rejected BOOLEAN DEFAULT FALSE,
                                    rejection_reason TEXT,
                                    is_limit_exceeded BOOLEAN DEFAULT FALSE,
                                    is_current_assignment BOOLEAN DEFAULT TRUE,

                                    rejected_by UUID,
                                    rejected_at TIMESTAMP WITHOUT TIME ZONE,
                                    assigned_on TIMESTAMP WITHOUT TIME ZONE,
                                    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,

                                    CONSTRAINT fk_cluster_id
                                        FOREIGN KEY (cluster_id)
                                            REFERENCES public.cluster_master(clu_master_id)
                                            ON DELETE SET NULL,

                                    CONSTRAINT fk_keyplot_id
                                        FOREIGN KEY (keyplot_id)
                                            REFERENCES public.keyplot_selections(kp_id)
                                            ON DELETE SET NULL
);
