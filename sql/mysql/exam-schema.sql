CREATE TABLE IF NOT EXISTS exam_venue (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(128) NOT NULL,
  address VARCHAR(255) NOT NULL,
  capacity INT NOT NULL,
  latitude DECIMAL(10,7) NOT NULL,
  longitude DECIMAL(10,7) NOT NULL,
  radius_meters DECIMAL(10,2) NOT NULL DEFAULT 100.00,
  status TINYINT NOT NULL DEFAULT 0,
  version BIGINT NOT NULL DEFAULT 1,
  tenant_id BIGINT NOT NULL,
  creator VARCHAR(64) NOT NULL DEFAULT '',
  create_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updater VARCHAR(64) NOT NULL DEFAULT '',
  update_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  deleted BIT(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (id),
  KEY idx_exam_venue_status (tenant_id, status, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS exam_session (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(128) NOT NULL,
  semester_id BIGINT NOT NULL,
  course_id BIGINT NOT NULL,
  start_time DATETIME(6) NOT NULL,
  end_time DATETIME(6) NOT NULL,
  venue_id BIGINT NOT NULL,
  venue_name VARCHAR(128) NOT NULL,
  venue_address VARCHAR(255) NOT NULL,
  venue_capacity INT NOT NULL,
  venue_latitude DECIMAL(10,7) NOT NULL,
  venue_longitude DECIMAL(10,7) NOT NULL,
  radius_meters DECIMAL(10,2) NOT NULL,
  session_version BIGINT NOT NULL DEFAULT 1,
  score_config_locked BIT(1) NOT NULL DEFAULT b'0',
  tenant_id BIGINT NOT NULL,
  creator VARCHAR(64) NOT NULL DEFAULT '',
  create_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updater VARCHAR(64) NOT NULL DEFAULT '',
  update_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  deleted BIT(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (id),
  KEY idx_exam_session_semester (tenant_id, deleted, semester_id, start_time, id),
  KEY idx_exam_session_course (tenant_id, semester_id, course_id, deleted, start_time),
  KEY idx_exam_session_venue (tenant_id, venue_id, deleted, start_time, end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS exam_session_class (
  id BIGINT NOT NULL AUTO_INCREMENT,
  session_id BIGINT NOT NULL,
  class_id BIGINT NOT NULL,
  tenant_id BIGINT NOT NULL,
  creator VARCHAR(64) NOT NULL DEFAULT '',
  create_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updater VARCHAR(64) NOT NULL DEFAULT '',
  update_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  deleted BIT(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (id),
  active_class_id BIGINT GENERATED ALWAYS AS (CASE WHEN deleted = b'0' THEN class_id ELSE NULL END) STORED,
  UNIQUE KEY uk_exam_session_class (tenant_id, session_id, active_class_id),
  KEY idx_exam_class_session (tenant_id, class_id, deleted, session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS exam_participant (
  id BIGINT NOT NULL AUTO_INCREMENT,
  session_id BIGINT NOT NULL,
  student_id BIGINT NOT NULL,
  student_no VARCHAR(64) NOT NULL,
  student_name VARCHAR(128) NOT NULL,
  class_id BIGINT NOT NULL,
  class_name VARCHAR(128) NOT NULL,
  sign_time DATETIME(6),
  sign_latitude DECIMAL(10,7),
  sign_longitude DECIMAL(10,7),
  sign_accuracy DECIMAL(10,2),
  position_time DATETIME(6),
  final_score DECIMAL(5,2),
  row_version BIGINT NOT NULL DEFAULT 1,
  tenant_id BIGINT NOT NULL,
  creator VARCHAR(64) NOT NULL DEFAULT '',
  create_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updater VARCHAR(64) NOT NULL DEFAULT '',
  update_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  deleted BIT(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (id),
  active_student_id BIGINT GENERATED ALWAYS AS (CASE WHEN deleted = b'0' THEN student_id ELSE NULL END) STORED,
  UNIQUE KEY uk_exam_participant_student (tenant_id, session_id, active_student_id),
  KEY idx_exam_student_session (tenant_id, student_id, deleted, session_id),
  KEY idx_exam_participant_class (tenant_id, session_id, class_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS exam_score_item (
  id BIGINT NOT NULL AUTO_INCREMENT,
  session_id BIGINT NOT NULL,
  name VARCHAR(128) NOT NULL,
  weight DECIMAL(5,2) NOT NULL,
  sort INT NOT NULL,
  tenant_id BIGINT NOT NULL,
  creator VARCHAR(64) NOT NULL DEFAULT '',
  create_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updater VARCHAR(64) NOT NULL DEFAULT '',
  update_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  deleted BIT(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (id),
  active_name VARCHAR(128) GENERATED ALWAYS AS (CASE WHEN deleted = b'0' THEN name ELSE NULL END) STORED,
  UNIQUE KEY uk_exam_item_name (tenant_id, session_id, active_name),
  KEY idx_exam_item_session (tenant_id, session_id, deleted, sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS exam_score_detail (
  id BIGINT NOT NULL AUTO_INCREMENT,
  participant_id BIGINT NOT NULL,
  item_id BIGINT NOT NULL,
  score DECIMAL(5,2) NOT NULL,
  tenant_id BIGINT NOT NULL,
  creator VARCHAR(64) NOT NULL DEFAULT '',
  create_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updater VARCHAR(64) NOT NULL DEFAULT '',
  update_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  deleted BIT(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (id),
  UNIQUE KEY uk_exam_score_detail (tenant_id, participant_id, item_id),
  KEY idx_exam_score_item (tenant_id, item_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS exam_schedule_guard (
  tenant_id BIGINT NOT NULL,
  creator VARCHAR(64) NOT NULL DEFAULT '',
  create_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updater VARCHAR(64) NOT NULL DEFAULT '',
  update_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  deleted BIT(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

