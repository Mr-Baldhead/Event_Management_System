-- Form fields table for dynamic form builder
CREATE TABLE form_fields (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             event_id BIGINT NOT NULL,
                             label VARCHAR(100) NOT NULL,
                             field_type VARCHAR(20) NOT NULL,
                             sort_order INT NOT NULL DEFAULT 0,
                             required BOOLEAN NOT NULL DEFAULT FALSE,
                             visible BOOLEAN NOT NULL DEFAULT TRUE,
                             validation_pattern VARCHAR(500),
                             placeholder VARCHAR(200),
                             max_length INT,
                             predefined_type VARCHAR(50),
                             is_predefined BOOLEAN NOT NULL DEFAULT FALSE,
                             parent_field_id BIGINT,
                             trigger_value VARCHAR(100),
                             row_index INT NOT NULL DEFAULT 0,
                             col_position INT NOT NULL DEFAULT 0,
                             col_width INT NOT NULL DEFAULT 12,
                             created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                             CONSTRAINT fk_form_fields_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
                             CONSTRAINT fk_form_fields_parent FOREIGN KEY (parent_field_id) REFERENCES form_fields(id) ON DELETE SET NULL,

                             INDEX idx_form_fields_event (event_id),
                             INDEX idx_form_fields_sort (event_id, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Field options table for select, checkbox, radio fields
CREATE TABLE field_options (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               field_id BIGINT NOT NULL,
                               value VARCHAR(200) NOT NULL,
                               label VARCHAR(200) NOT NULL,
                               sort_order INT NOT NULL DEFAULT 0,

                               CONSTRAINT fk_field_options_field FOREIGN KEY (field_id) REFERENCES form_fields(id) ON DELETE CASCADE,

                               INDEX idx_field_options_field (field_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;