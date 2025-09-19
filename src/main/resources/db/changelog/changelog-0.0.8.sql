CREATE TABLE cluster_approval_log (
                                      id BIGSERIAL PRIMARY KEY,
                                      cluster_id BIGINT NOT NULL,
                                      zone_id INT NOT NULL,
                                      total_area NUMERIC(10,2),
                                      added_by UUID,
                                      admin_id UUID,
                                      remarks TEXT,
                                      in_approved BOOLEAN DEFAULT FALSE,
                                      created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                      updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                      is_active BOOLEAN DEFAULT TRUE,

                                      CONSTRAINT cluster_details_cluster_id_fkey
                                          FOREIGN KEY (cluster_id)
                                              REFERENCES public.cluster_master(clu_master_id)
                                              ON DELETE CASCADE,

                                      CONSTRAINT cluster_approval_zone_id_fkey
                                          FOREIGN KEY (zone_id)
                                              REFERENCES public.tbl_master_zone(zone_id)
                                              ON DELETE CASCADE
);
