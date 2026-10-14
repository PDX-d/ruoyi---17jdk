CREATE TABLE IF NOT EXISTS timetable_classroom (
  id BIGINT NOT NULL AUTO_INCREMENT,
  code VARCHAR(64) NOT NULL,
  name VARCHAR(128) NOT NULL,
  location VARCHAR(255) NOT NULL,
  capacity INT NOT NULL,
  status TINYINT NOT NULL DEFAULT 0,
  tenant_id BIGINT NOT NULL,
  creator VARCHAR(64) NOT NULL DEFAULT '',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) NOT NULL DEFAULT '',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted BIT(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (id),
  KEY idx_classroom_code (tenant_id, code, deleted),
  KEY idx_classroom_status (tenant_id, status, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS timetable_lesson (
  id BIGINT NOT NULL AUTO_INCREMENT,
  semester_id BIGINT NOT NULL,
  course_id BIGINT NOT NULL,
  teacher_id BIGINT NOT NULL,
  classroom_id BIGINT NOT NULL,
  start_time DATETIME(6) NOT NULL,
  end_time DATETIME(6) NOT NULL,
  status TINYINT NOT NULL DEFAULT 0,
  version BIGINT NOT NULL DEFAULT 1,
  tenant_id BIGINT NOT NULL,
  creator VARCHAR(64) NOT NULL DEFAULT '',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) NOT NULL DEFAULT '',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted BIT(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (id),
  KEY idx_lesson_time (tenant_id, deleted, status, start_time, end_time),
  KEY idx_lesson_semester (tenant_id, semester_id, start_time, deleted),
  KEY idx_lesson_teacher (tenant_id, teacher_id, start_time, deleted),
  KEY idx_lesson_classroom (tenant_id, classroom_id, start_time, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS timetable_lesson_class (
  id BIGINT NOT NULL AUTO_INCREMENT,
  lesson_id BIGINT NOT NULL,
  class_id BIGINT NOT NULL,
  tenant_id BIGINT NOT NULL,
  creator VARCHAR(64) NOT NULL DEFAULT '',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) NOT NULL DEFAULT '',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted BIT(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (id),
  UNIQUE KEY uk_lesson_class (tenant_id, lesson_id, class_id),
  KEY idx_class_lessons (tenant_id, class_id, deleted, lesson_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS timetable_change_request (
  id BIGINT NOT NULL AUTO_INCREMENT,
  lesson_id BIGINT NOT NULL,
  original_version BIGINT NOT NULL,
  original_start_time DATETIME(6) NOT NULL,
  original_end_time DATETIME(6) NOT NULL,
  original_classroom_id BIGINT NOT NULL,
  type VARCHAR(16) NOT NULL,
  target_start_time DATETIME(6) NULL,
  target_end_time DATETIME(6) NULL,
  target_classroom_id BIGINT NULL,
  reason VARCHAR(1000) NOT NULL,
  applicant_id BIGINT NOT NULL,
  status TINYINT NOT NULL DEFAULT 0,
  reviewer_id BIGINT NULL,
  review_time DATETIME NULL,
  review_reason VARCHAR(1000) NULL,
  tenant_id BIGINT NOT NULL,
  creator VARCHAR(64) NOT NULL DEFAULT '',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) NOT NULL DEFAULT '',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted BIT(1) NOT NULL DEFAULT b'0',
  pending_lesson_id BIGINT GENERATED ALWAYS AS
    (CASE WHEN status = 0 AND deleted = b'0' THEN lesson_id ELSE NULL END) STORED,
  PRIMARY KEY (id),
  UNIQUE KEY uk_pending_lesson (tenant_id, pending_lesson_id),
  KEY idx_change_lesson (tenant_id, lesson_id, status, deleted),
  KEY idx_change_applicant (tenant_id, applicant_id, deleted, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS timetable_schedule_guard (
  tenant_id BIGINT NOT NULL,
  creator VARCHAR(64) NOT NULL DEFAULT '',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) NOT NULL DEFAULT '',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted BIT(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
