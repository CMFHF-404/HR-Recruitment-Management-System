USE hr_recruitment;

SET @admin_role_exists = (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'admin'
    AND COLUMN_NAME = 'role'
);
SET @sql = IF(@admin_role_exists = 0,
  'ALTER TABLE admin ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT ''HR''',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @manager_status_exists = (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'resume_screening'
    AND COLUMN_NAME = 'manager_status'
);
SET @sql = IF(@manager_status_exists = 0,
  'ALTER TABLE resume_screening ADD COLUMN manager_status VARCHAR(20) NOT NULL DEFAULT ''NOT_SUBMITTED''',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @manager_comment_exists = (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'resume_screening'
    AND COLUMN_NAME = 'manager_comment'
);
SET @sql = IF(@manager_comment_exists = 0,
  'ALTER TABLE resume_screening ADD COLUMN manager_comment TEXT',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @manager_review_time_exists = (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'resume_screening'
    AND COLUMN_NAME = 'manager_review_time'
);
SET @sql = IF(@manager_review_time_exists = 0,
  'ALTER TABLE resume_screening ADD COLUMN manager_review_time DATETIME',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
