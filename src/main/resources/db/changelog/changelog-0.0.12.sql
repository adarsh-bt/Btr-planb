CREATE TABLE season_master_tbl(
    id SERIAL PRIMARY KEY,
    season_name VARCHAR(100) NOT NULL,
    default_start DATE NOT NULL,
    default_end DATE NOT NULL,
    uuid UUID DEFAULT gen_random_uuid(),
    is_active BOOLEAN DEFAULT TRUE
);