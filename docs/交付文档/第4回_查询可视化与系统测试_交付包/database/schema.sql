CREATE DATABASE IF NOT EXISTS hr_recruitment DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE hr_recruitment;

CREATE TABLE IF NOT EXISTS admin (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  name VARCHAR(50) NOT NULL,
  role VARCHAR(20) NOT NULL,
  created_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS position (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  department VARCHAR(100) NOT NULL,
  headcount INT NOT NULL,
  requirements TEXT NOT NULL,
  publish_date DATE NOT NULL,
  status VARCHAR(20) NOT NULL,
  created_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS candidate (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL,
  gender VARCHAR(10) NOT NULL,
  phone VARCHAR(30) NOT NULL,
  email VARCHAR(120) NOT NULL,
  education VARCHAR(50) NOT NULL,
  school VARCHAR(120) NOT NULL,
  position_id BIGINT NOT NULL,
  note TEXT,
  resume_original_file_name VARCHAR(255),
  resume_content_type VARCHAR(120),
  resume_storage_path VARCHAR(500),
  resume_text TEXT,
  resume_uploaded_at DATETIME,
  created_at DATETIME NOT NULL,
  CONSTRAINT fk_candidate_position FOREIGN KEY (position_id) REFERENCES position(id)
);

CREATE TABLE IF NOT EXISTS resume_screening (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  candidate_id BIGINT NOT NULL UNIQUE,
  status VARCHAR(20) NOT NULL,
  comment TEXT,
  ai_match_score INT,
  ai_quick_review TEXT,
  screening_time DATETIME,
  manager_status VARCHAR(20) NOT NULL,
  manager_comment TEXT,
  manager_review_time DATETIME,
  CONSTRAINT fk_screening_candidate FOREIGN KEY (candidate_id) REFERENCES candidate(id)
);

CREATE TABLE IF NOT EXISTS interview (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  candidate_id BIGINT NOT NULL UNIQUE,
  interview_time DATETIME,
  location VARCHAR(120),
  interviewer VARCHAR(50),
  status VARCHAR(30) NOT NULL,
  evaluation TEXT,
  CONSTRAINT fk_interview_candidate FOREIGN KEY (candidate_id) REFERENCES candidate(id)
);

CREATE TABLE IF NOT EXISTS offer_result (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  candidate_id BIGINT NOT NULL UNIQUE,
  status VARCHAR(30) NOT NULL,
  salary_note VARCHAR(120),
  remark TEXT,
  registered_at DATETIME NOT NULL,
  CONSTRAINT fk_offer_candidate FOREIGN KEY (candidate_id) REFERENCES candidate(id)
);
