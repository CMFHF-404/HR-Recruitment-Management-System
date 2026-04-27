USE hr_recruitment;

INSERT INTO admin (username, password, name, created_at)
VALUES ('admin', '$2a$10$mZwKovDHpfHPgwtWUwAEKetYKDWsiDvUcaxcfTG13GxE6LnlYr2be', 'HR 管理员', NOW())
ON DUPLICATE KEY UPDATE username = username;

INSERT INTO position (name, department, headcount, requirements, publish_date, status, created_at)
VALUES
('Java 开发工程师', '技术部', 2, '熟悉 Spring Boot、MySQL，具备良好的编码习惯。', CURDATE(), 'OPEN', NOW()),
('人事专员', '人力资源部', 1, '熟悉招聘流程，具备良好的沟通协调能力。', CURDATE(), 'OPEN', NOW());
