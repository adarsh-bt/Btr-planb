

CREATE TABLE IF NOT EXISTS tbl_work_allocation (
    id BIGSERIAL PRIMARY KEY,
    zone_id INT NOT NULL REFERENCES tbl_master_zone(zone_id) ON DELETE CASCADE,
    lbcode VARCHAR(100),

    village_wet_area DECIMAL(18,2),
    village_dry_area DECIMAL(18,2),
    village_total_area DECIMAL(18,2),
    forest_area_a DECIMAL(18,2),
    forest_area_b DECIMAL(18,2),
    forest_area_c DECIMAL(18,2),
    area_under_plant DECIMAL(18,2),
    forest_exclude_unclutivate DECIMAL(18,2),
    forest_exclude_not_unclutivate DECIMAL(18,2),
    kayal_exclude_area DECIMAL(18,2),
    other_exclude_f_wet DECIMAL(18,2),
    other_excluded_f_dry DECIMAL(18,2),
    other_exclude_f_total DECIMAL(18,2),
    no_of_plots_wet DECIMAL(18,2),
    no_of_plots_dry DECIMAL(18,2),
    no_of_plots_total DECIMAL(18,2),
    total_area_wet DECIMAL(18,2),
    total_area_dry DECIMAL(18,2),
    total_area_for_estimation DECIMAL(18,2),

    remarks TEXT,
    user_id UUID,
    created DATE DEFAULT CURRENT_DATE,
    updated DATE,
    is_active BOOLEAN DEFAULT TRUE,
    agri_start DATE,
    agri_end DATE
);