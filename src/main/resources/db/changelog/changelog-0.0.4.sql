CREATE TABLE cluster_master (
                                clu_master_id BIGSERIAL PRIMARY KEY,
                                plot_id UUID NOT NULL REFERENCES keyplot_selections(kp_id) ON DELETE CASCADE,
                                cluster_number INTEGER,
                                status VARCHAR(255),
                                is_reject BOOLEAN DEFAULT FALSE,
                                investigator_remark TEXT,
                                is_active BOOLEAN DEFAULT TRUE,
                                zone_id INTEGER NOT NULL REFERENCES tbl_master_zone(zone_id) ON DELETE CASCADE,
                                created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP

    -- <-- Track user who created the record

);

CREATE TABLE public.cluster_details (
                                        clu_detail_id BIGSERIAL PRIMARY KEY,
                                        cluster_id BIGINT NOT NULL,
                                        plot_id BIGINT NOT NULL,
                                        plot_label VARCHAR(255),
                                        enumerated_area NUMERIC(10,2),
                                        created_by UUID,
                                        status BOOLEAN,
                                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                        remark TEXT,

                                        CONSTRAINT cluster_details_cluster_id_fkey
                                            FOREIGN KEY (cluster_id)
                                                REFERENCES public.cluster_master(clu_master_id)
                                                ON DELETE CASCADE,

                                        CONSTRAINT cluster_details_plot_id_fkey
                                            FOREIGN KEY (plot_id)
                                                REFERENCES public.tbl_btr_dataskilliold(id)
                                                ON DELETE CASCADE
);

