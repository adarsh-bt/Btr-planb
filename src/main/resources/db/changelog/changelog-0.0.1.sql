CREATE TABLE user_zone_assignment (
                                      id SERIAL PRIMARY KEY,
                                      user_id UUID NOT NULL,
                                      zone_id INT NOT NULL,
                                      assigner_id UUID NOT NULL ,
                                      created_at TIMESTAMP,
                                      updated_at TIMESTAMP,
                                      is_active BOOLEAN DEFAULT true NOT NULL,
                                      FOREIGN KEY (zone_id) REFERENCES tbl_master_zone(zone_id) ON DELETE CASCADE

);
