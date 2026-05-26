USE hr_recruitment;

SET @resume_original_file_name_exists = (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'candidate'
    AND COLUMN_NAME = 'resume_original_file_name'
);
SET @sql = IF(@resume_original_file_name_exists = 0,
  'ALTER TABLE candidate ADD COLUMN resume_original_file_name VARCHAR(255)',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @resume_content_type_exists = (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'candidate'
    AND COLUMN_NAME = 'resume_content_type'
);
SET @sql = IF(@resume_content_type_exists = 0,
  'ALTER TABLE candidate ADD COLUMN resume_content_type VARCHAR(120)',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @resume_storage_path_exists = (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'candidate'
    AND COLUMN_NAME = 'resume_storage_path'
);
SET @sql = IF(@resume_storage_path_exists = 0,
  'ALTER TABLE candidate ADD COLUMN resume_storage_path VARCHAR(500)',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @resume_text_exists = (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'candidate'
    AND COLUMN_NAME = 'resume_text'
);
SET @sql = IF(@resume_text_exists = 0,
  'ALTER TABLE candidate ADD COLUMN resume_text TEXT',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @resume_uploaded_at_exists = (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'candidate'
    AND COLUMN_NAME = 'resume_uploaded_at'
);
SET @sql = IF(@resume_uploaded_at_exists = 0,
  'ALTER TABLE candidate ADD COLUMN resume_uploaded_at DATETIME',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ai_match_score_exists = (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'resume_screening'
    AND COLUMN_NAME = 'ai_match_score'
);
SET @sql = IF(@ai_match_score_exists = 0,
  'ALTER TABLE resume_screening ADD COLUMN ai_match_score INT',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ai_quick_review_exists = (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'resume_screening'
    AND COLUMN_NAME = 'ai_quick_review'
);
SET @sql = IF(@ai_quick_review_exists = 0,
  'ALTER TABLE resume_screening ADD COLUMN ai_quick_review TEXT',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
