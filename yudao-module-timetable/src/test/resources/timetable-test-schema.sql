CREATE TABLE timetable_classroom (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(64) NOT NULL, name VARCHAR(128) NOT NULL, location VARCHAR(255) NOT NULL,
  capacity INT NOT NULL, status TINYINT NOT NULL DEFAULT 0,
  tenant_id BIGINT NOT NULL,
  creator VARCHAR(64) DEFAULT '', create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '', update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE TABLE timetable_lesson (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  semester_id BIGINT NOT NULL, course_id BIGINT NOT NULL, teacher_id BIGINT NOT NULL,
  classroom_id BIGINT NOT NULL, start_time TIMESTAMP(6) NOT NULL, end_time TIMESTAMP(6) NOT NULL,
  status TINYINT NOT NULL DEFAULT 0, version BIGINT NOT NULL DEFAULT 1,
  tenant_id BIGINT NOT NULL,
  creator VARCHAR(64) DEFAULT '', create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '', update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE TABLE timetable_lesson_class (
  id BIGINT AUTO_INCREMENT PRIMARY KEY, lesson_id BIGINT NOT NULL, class_id BIGINT NOT NULL,
  tenant_id BIGINT NOT NULL,
  creator VARCHAR(64) DEFAULT '', create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '', update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  UNIQUE (tenant_id, lesson_id, class_id)
);
CREATE TABLE timetable_change_request (
  id BIGINT AUTO_INCREMENT PRIMARY KEY, lesson_id BIGINT NOT NULL, original_version BIGINT NOT NULL,
  original_start_time TIMESTAMP(6) NOT NULL, original_end_time TIMESTAMP(6) NOT NULL,
  original_classroom_id BIGINT NOT NULL, type VARCHAR(16) NOT NULL,
  target_start_time TIMESTAMP(6), target_end_time TIMESTAMP(6), target_classroom_id BIGINT,
  reason VARCHAR(1000) NOT NULL, applicant_id BIGINT NOT NULL, status TINYINT NOT NULL DEFAULT 0,
  reviewer_id BIGINT, review_time TIMESTAMP, review_reason VARCHAR(1000), tenant_id BIGINT NOT NULL,
  creator VARCHAR(64) DEFAULT '', create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '', update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  pending_lesson_id BIGINT GENERATED ALWAYS AS
    (CASE WHEN status = 0 AND deleted = FALSE THEN lesson_id ELSE NULL END),
  UNIQUE (tenant_id, pending_lesson_id)
);
CREATE TABLE timetable_schedule_guard (
  tenant_id BIGINT PRIMARY KEY,
  creator VARCHAR(64) DEFAULT '', create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '', update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE TABLE timetable_test_notification (
  id BIGINT AUTO_INCREMENT PRIMARY KEY, user_id BIGINT NOT NULL, template_code VARCHAR(64) NOT NULL
);
