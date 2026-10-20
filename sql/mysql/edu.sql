-- 学院管理系统（edu 模块）：辅导员 / 教师 / 年级班级 / 学生 / 学期 建表、字典、菜单、角色与初始化数据
-- 数据库：MySQL 8.0+，脚本可重复执行

-- 1. 辅导员表
DROP TABLE IF EXISTS `edu_counselor`;
CREATE TABLE `edu_counselor` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '辅导员编号',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `job_no` varchar(64) NOT NULL COMMENT '工号',
  `name` varchar(64) NOT NULL COMMENT '姓名',
  `gender` tinyint NULL DEFAULT NULL COMMENT '性别',
  `mobile` varchar(20) NULL DEFAULT NULL COMMENT '手机号',
  `title` varchar(64) NULL DEFAULT NULL COMMENT '职称',
  `dept_id` bigint NULL DEFAULT NULL COMMENT '所属院系编号',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0开启 1关闭）',
  `remark` varchar(500) NULL DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `active_job_no` varchar(64) GENERATED ALWAYS AS (IF(`deleted` = b'0', `job_no`, NULL)) VIRTUAL COMMENT '未删除时的工号（用于唯一约束，已删除记录为NULL）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_tenant_job_no` (`tenant_id`, `active_job_no`) USING BTREE
) ENGINE = InnoDB COMMENT = '辅导员表';

-- 2. 辅导员带班表
DROP TABLE IF EXISTS `edu_counselor_class`;
CREATE TABLE `edu_counselor_class` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `counselor_id` bigint NOT NULL COMMENT '辅导员编号',
  `class_name` varchar(64) NOT NULL COMMENT '班级名称',
  `creator` varchar(64) NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_counselor_id` (`counselor_id`) USING BTREE,
  UNIQUE KEY `uk_tenant_class_name` (`tenant_id`, `class_name`) USING BTREE
) ENGINE = InnoDB COMMENT = '辅导员带班表';

-- 3. 教师表
DROP TABLE IF EXISTS `edu_teacher`;
CREATE TABLE `edu_teacher` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '教师编号',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `job_no` varchar(64) NOT NULL COMMENT '工号',
  `name` varchar(64) NOT NULL COMMENT '姓名',
  `gender` tinyint NULL DEFAULT NULL COMMENT '性别',
  `mobile` varchar(20) NULL DEFAULT NULL COMMENT '手机号',
  `title` varchar(64) NULL DEFAULT NULL COMMENT '职称',
  `education` varchar(64) NULL DEFAULT NULL COMMENT '学历',
  `dept_id` bigint NULL DEFAULT NULL COMMENT '所属院系编号',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0开启 1关闭）',
  `remark` varchar(500) NULL DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `active_job_no` varchar(64) GENERATED ALWAYS AS (IF(`deleted` = b'0', `job_no`, NULL)) VIRTUAL COMMENT '未删除时的工号（用于唯一约束，已删除记录为NULL）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_tenant_job_no` (`tenant_id`, `active_job_no`) USING BTREE
) ENGINE = InnoDB COMMENT = '教师表';

-- 菜单权限（辅导员 / 教师）
-- 一级目录：学院管理
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, component_name, status)
VALUES ('学院管理', '', 1, 100, 0, '/edu', 'ep:school', '', '', 0);
SELECT @eduDirId := LAST_INSERT_ID();

-- 二级菜单：辅导员管理
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, component_name, status)
VALUES ('辅导员管理', '', 2, 1, @eduDirId, 'counselor', 'ep:user', 'edu/counselor/index', 'EduCounselor', 0);
SELECT @counselorMenuId := LAST_INSERT_ID();

-- 辅导员管理按钮
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, status)
VALUES ('辅导员查询', 'edu:counselor:query', 3, 1, @counselorMenuId, '', '', '', 0);
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, status)
VALUES ('辅导员创建', 'edu:counselor:create', 3, 2, @counselorMenuId, '', '', '', 0);
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, status)
VALUES ('辅导员更新', 'edu:counselor:update', 3, 3, @counselorMenuId, '', '', '', 0);
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, status)
VALUES ('辅导员删除', 'edu:counselor:delete', 3, 4, @counselorMenuId, '', '', '', 0);

-- 二级菜单：教师管理
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, component_name, status)
VALUES ('教师管理', '', 2, 2, @eduDirId, 'teacher', 'ep:avatar', 'edu/teacher/index', 'EduTeacher', 0);
SELECT @teacherMenuId := LAST_INSERT_ID();

-- 教师管理按钮
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, status)
VALUES ('教师查询', 'edu:teacher:query', 3, 1, @teacherMenuId, '', '', '', 0);
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, status)
VALUES ('教师创建', 'edu:teacher:create', 3, 2, @teacherMenuId, '', '', '', 0);
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, status)
VALUES ('教师更新', 'edu:teacher:update', 3, 3, @teacherMenuId, '', '', '', 0);
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, status)
VALUES ('教师删除', 'edu:teacher:delete', 3, 4, @teacherMenuId, '', '', '', 0);


-- 学生管理 / 学期管理 / 年级班级管理：建表、字典、菜单、角色与播种
-- 业务表用 DROP + CREATE 重建（会清空这些表的数据）；字典 / 菜单 / 角色 / 播种均幂等可重复执行。

-- 0. 菜单树自愈：重复的「学院管理」目录只保留最早一个，其余子节点改挂到保留节点后逻辑删除
SET @eduDirId := (SELECT MIN(id) FROM (SELECT id FROM system_menu WHERE parent_id = 0 AND path = '/edu' AND deleted = b'0') t);

UPDATE system_menu SET parent_id = @eduDirId
WHERE deleted = b'0' AND parent_id <> @eduDirId
  AND parent_id IN (SELECT id FROM (SELECT id FROM system_menu WHERE parent_id = 0 AND path = '/edu' AND deleted = b'0') t);

UPDATE system_menu SET deleted = b'1'
WHERE parent_id = 0 AND path = '/edu' AND deleted = b'0' AND id <> @eduDirId;

SET @mEduStudent   := (SELECT MIN(id) FROM (SELECT id FROM system_menu WHERE deleted = b'0' AND component_name = 'EduStudent') t);
SET @mEduSemester  := (SELECT MIN(id) FROM (SELECT id FROM system_menu WHERE deleted = b'0' AND component_name = 'EduSemester') t);
SET @mEduCounselor := (SELECT MIN(id) FROM (SELECT id FROM system_menu WHERE deleted = b'0' AND component_name = 'EduCounselor') t);
SET @mEduTeacher   := (SELECT MIN(id) FROM (SELECT id FROM system_menu WHERE deleted = b'0' AND component_name = 'EduTeacher') t);

UPDATE system_menu SET parent_id = @mEduStudent
WHERE deleted = b'0' AND parent_id <> @mEduStudent
  AND parent_id IN (SELECT id FROM (SELECT id FROM system_menu WHERE deleted = b'0' AND component_name = 'EduStudent') t);
UPDATE system_menu SET parent_id = @mEduSemester
WHERE deleted = b'0' AND parent_id <> @mEduSemester
  AND parent_id IN (SELECT id FROM (SELECT id FROM system_menu WHERE deleted = b'0' AND component_name = 'EduSemester') t);
UPDATE system_menu SET parent_id = @mEduCounselor
WHERE deleted = b'0' AND parent_id <> @mEduCounselor
  AND parent_id IN (SELECT id FROM (SELECT id FROM system_menu WHERE deleted = b'0' AND component_name = 'EduCounselor') t);
UPDATE system_menu SET parent_id = @mEduTeacher
WHERE deleted = b'0' AND parent_id <> @mEduTeacher
  AND parent_id IN (SELECT id FROM (SELECT id FROM system_menu WHERE deleted = b'0' AND component_name = 'EduTeacher') t);

UPDATE system_menu SET deleted = b'1' WHERE deleted = b'0' AND component_name = 'EduStudent'   AND id <> @mEduStudent;
UPDATE system_menu SET deleted = b'1' WHERE deleted = b'0' AND component_name = 'EduSemester'  AND id <> @mEduSemester;
UPDATE system_menu SET deleted = b'1' WHERE deleted = b'0' AND component_name = 'EduCounselor' AND id <> @mEduCounselor;
UPDATE system_menu SET deleted = b'1' WHERE deleted = b'0' AND component_name = 'EduTeacher'   AND id <> @mEduTeacher;

-- 按钮按 permission 去重：用临时表承载「保留编号」，避免 MySQL 1093（更新表同时在子查询读该表）
CREATE TEMPORARY TABLE IF NOT EXISTS tmp_edu_menu_keep (
  permission varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限标识（排序规则与 system_menu.permission 保持一致）',
  keep_id bigint NOT NULL COMMENT '保留的菜单编号',
  PRIMARY KEY (permission)
);
DELETE FROM tmp_edu_menu_keep;
INSERT INTO tmp_edu_menu_keep (permission, keep_id)
SELECT permission, MIN(id) FROM system_menu
WHERE deleted = b'0' AND permission LIKE 'edu:%' AND permission <> ''
GROUP BY permission;
UPDATE system_menu m
JOIN tmp_edu_menu_keep k ON k.permission = m.permission
SET m.deleted = b'1'
WHERE m.deleted = b'0' AND m.id <> k.keep_id;
DROP TEMPORARY TABLE tmp_edu_menu_keep;

-- 0.1 一级目录「学院管理」：不存在才创建，sort=1 紧随首页；后续菜单统一挂到 @eduDirId
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status)
SELECT '学院管理', '', 1, 1, 0, '/edu', 'ep:school', '', '', 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM system_menu m WHERE m.parent_id = 0 AND m.path = '/edu' AND m.deleted = b'0');
SET @eduDirId := (SELECT id FROM system_menu WHERE parent_id = 0 AND path = '/edu' AND deleted = b'0' ORDER BY id LIMIT 1);
UPDATE system_menu SET sort = 1 WHERE id = @eduDirId AND parent_id = 0 AND path = '/edu' AND deleted = b'0';

-- 1. 年级表
DROP TABLE IF EXISTS `edu_grade`;
CREATE TABLE `edu_grade` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '年级编号',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `name` varchar(64) NOT NULL COMMENT '年级名称（如 2023级）',
  `code` varchar(32) NULL DEFAULT NULL COMMENT '年级编码（如 2023，用于升级与统计）',
  `enrollment_year` int NULL DEFAULT NULL COMMENT '入学年份（如 2023）',
  `dept_id` bigint NULL DEFAULT NULL COMMENT '所属院系编号',
  `major` varchar(128) NULL DEFAULT NULL COMMENT '专业',
  `counselor_id` bigint NULL DEFAULT NULL COMMENT '年级负责人（辅导员）编号，模块五完善',
  `sort` int NOT NULL DEFAULT 0 COMMENT '显示顺序',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0开启 1关闭）',
  `remark` varchar(500) NULL DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `active_name` varchar(64) GENERATED ALWAYS AS (IF(`deleted` = b'0', `name`, NULL)) VIRTUAL COMMENT '未删除时的年级名称（用于唯一约束，已删除记录为NULL）',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_dept_id` (`dept_id`) USING BTREE,
  UNIQUE KEY `uk_tenant_name` (`tenant_id`, `active_name`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '年级表（模块五 年级班级管理 最小实现）';

-- 2. 班级表：以「年级 + 名称」唯一，允许不同年级同名班级
DROP TABLE IF EXISTS `edu_class`;
CREATE TABLE `edu_class` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '班级编号',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `grade_id` bigint NOT NULL COMMENT '年级编号',
  `name` varchar(64) NOT NULL COMMENT '班级名称',
  `class_no` varchar(64) NULL DEFAULT NULL COMMENT '班号（如 20230101）',
  `dept_id` bigint NULL DEFAULT NULL COMMENT '所属院系编号',
  `counselor_id` bigint NULL DEFAULT NULL COMMENT '辅导员编号（模块一 辅导员管理）',
  `head_teacher_id` bigint NULL DEFAULT NULL COMMENT '班主任编号（模块二 教师管理）',
  `sort` int NOT NULL DEFAULT 0 COMMENT '显示顺序',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0开启 1关闭）',
  `remark` varchar(500) NULL DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `active_grade_name` varchar(128) GENERATED ALWAYS AS (IF(`deleted` = b'0', CONCAT(`grade_id`, '-', `name`), NULL)) VIRTUAL COMMENT '未删除时的「年级-班级」组合（用于唯一约束，已删除记录为NULL）',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_grade_id` (`grade_id`) USING BTREE,
  KEY `idx_dept_id` (`dept_id`) USING BTREE,
  KEY `idx_counselor_id` (`counselor_id`) USING BTREE,
  UNIQUE KEY `uk_tenant_grade_name` (`tenant_id`, `active_grade_name`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '班级表（模块五 年级班级管理 最小实现）';

-- 3. 学生表
--    家庭信息合并在本表；user_id 为自动建号后回写的系统账号；证件照 3 项 URL 由前端上传写入。
DROP TABLE IF EXISTS `edu_student`;
CREATE TABLE `edu_student` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '学生编号',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `student_no` varchar(64) NOT NULL COMMENT '学号（同时作为系统登录用户名）',
  `name` varchar(64) NOT NULL COMMENT '姓名',
  `gender` tinyint NULL DEFAULT NULL COMMENT '性别（字典 system_user_sex，1男 2女）',
  `birth_date` date NULL DEFAULT NULL COMMENT '出生日期（不允许晚于今天，前端 disabledDate + 后端 @PastOrPresent 双校验）',
  `id_card` varchar(32) NULL DEFAULT NULL COMMENT '身份证号',
  `nation` varchar(32) NULL DEFAULT NULL COMMENT '民族',
  `political_status` tinyint NULL DEFAULT NULL COMMENT '政治面貌（字典 edu_political_status）',
  `native_place` varchar(128) NULL DEFAULT NULL COMMENT '籍贯',
  `grade_id` bigint NULL DEFAULT NULL COMMENT '年级编号（edu_grade，下拉选择）',
  `class_id` bigint NULL DEFAULT NULL COMMENT '班级编号（edu_class，下拉选择，依赖年级）',
  `major_id` bigint NULL DEFAULT NULL COMMENT '专业编号（占位表 dict_major，下拉选择，依赖年级；字典中心模块待接入）',
  `major` varchar(128) NULL DEFAULT NULL COMMENT '专业名称（冗余存储，便于展示；来源 dict_major）',
  `dept_id` bigint NULL DEFAULT NULL COMMENT '所属院系编号（system_dept，下拉选择）',
  `education_system` tinyint NULL DEFAULT NULL COMMENT '学制（字典 edu_education_system，已移除「三年制」）',
  `enrollment_year` int NULL DEFAULT NULL COMMENT '入学年份',
  `admission_date` date NULL DEFAULT NULL COMMENT '入学日期',
  `graduate_date` date NULL DEFAULT NULL COMMENT '毕业日期',
  `dormitory` varchar(64) NULL DEFAULT NULL COMMENT '宿舍',
  `mobile` varchar(20) NULL DEFAULT NULL COMMENT '手机号',
  `email` varchar(64) NULL DEFAULT NULL COMMENT '邮箱',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '学籍状态（字典 edu_student_status，0在读 1休学 2退学 3毕业 4复学）',
  `photo_url` varchar(512) NULL DEFAULT NULL COMMENT '证件照地址',
  `id_card_front_url` varchar(512) NULL DEFAULT NULL COMMENT '身份证正面地址',
  `id_card_back_url` varchar(512) NULL DEFAULT NULL COMMENT '身份证反面地址',
  `user_id` bigint NULL DEFAULT NULL COMMENT '关联系统账号编号（自动建号后回写）',
  `guardian_name` varchar(64) NULL DEFAULT NULL COMMENT '监护人姓名（由父/母姓名合并，优先父亲）',
  `guardian_mobile` varchar(20) NULL DEFAULT NULL COMMENT '监护人手机号（由父/母电话合并，优先父亲）',
  `address` varchar(255) NULL DEFAULT NULL COMMENT '家庭住址',
  `emergency_contact` varchar(64) NULL DEFAULT NULL COMMENT '紧急联系人',
  `emergency_mobile` varchar(20) NULL DEFAULT NULL COMMENT '紧急联系人电话',
  `remark` varchar(500) NULL DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `active_student_no` varchar(64) GENERATED ALWAYS AS (IF(`deleted` = b'0', `student_no`, NULL)) VIRTUAL COMMENT '未删除时的学号（用于唯一约束，已删除记录为NULL）',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_grade_id` (`grade_id`) USING BTREE,
  KEY `idx_class_id` (`class_id`) USING BTREE,
  KEY `idx_major_id` (`major_id`) USING BTREE,
  KEY `idx_dept_id` (`dept_id`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  UNIQUE KEY `uk_tenant_student_no` (`tenant_id`, `active_student_no`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学生表（模块三 学生管理）';

-- 4. 学生标签表：同租户未删除记录的名称与 sort 均唯一；颜色为后端 TagColorEnum 校验的固定 16 色
DROP TABLE IF EXISTS `edu_student_tag`;
CREATE TABLE `edu_student_tag` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '标签编号',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `name` varchar(64) NOT NULL COMMENT '标签名称（同租户下未删除记录唯一）',
  `color` varchar(16) NOT NULL DEFAULT '#409EFF' COMMENT '标签颜色（固定 16 色，由后端 TagColorEnum 校验）',
  `sort` int NOT NULL DEFAULT 0 COMMENT '显示顺序（同租户下未删除记录连续递增且唯一，仅后端维护）',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0启用 1停用）',
  `remark` varchar(500) NULL DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `active_name` varchar(64) GENERATED ALWAYS AS (IF(`deleted` = b'0', `name`, NULL)) VIRTUAL COMMENT '未删除时的标签名称（用于唯一约束，已删除记录为NULL）',
  `active_sort` int GENERATED ALWAYS AS (IF(`deleted` = b'0', `sort`, NULL)) VIRTUAL COMMENT '未删除时的显示顺序（用于唯一约束，已删除记录为NULL）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_tenant_name` (`tenant_id`, `active_name`) USING BTREE,
  UNIQUE KEY `uk_tenant_sort` (`tenant_id`, `active_sort`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学生标签表';

-- 5. 学生-标签关联表（物理删除维护，先删后插）
DROP TABLE IF EXISTS `edu_student_tag_rel`;
CREATE TABLE `edu_student_tag_rel` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `student_id` bigint NOT NULL COMMENT '学生编号',
  `tag_id` bigint NOT NULL COMMENT '标签编号',
  `creator` varchar(64) NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_student_id` (`student_id`) USING BTREE,
  KEY `idx_tag_id` (`tag_id`) USING BTREE,
  UNIQUE KEY `uk_tenant_student_tag` (`tenant_id`, `student_id`, `tag_id`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学生-标签关联表';

-- 6. 学生学籍变更记录表
--    approval_status 对齐 BPM 语义；process_instance_id 为接入 BPM 后回写的实例编号
DROP TABLE IF EXISTS `edu_student_change_record`;
CREATE TABLE `edu_student_change_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `student_id` bigint NOT NULL COMMENT '学生编号',
  `change_type` tinyint NOT NULL COMMENT '变更类型（字典 edu_student_change_type，1学籍状态变更 2调班 3复学）',
  `from_value` varchar(255) NULL DEFAULT NULL COMMENT '变更前值（状态字典值或班级编号）',
  `to_value` varchar(255) NULL DEFAULT NULL COMMENT '变更后值（状态字典值或班级编号）',
  `reason` varchar(500) NULL DEFAULT NULL COMMENT '变更原因',
  `approval_status` tinyint NOT NULL DEFAULT 0 COMMENT '审批状态（字典 edu_approval_status，0草稿 1审批中 2通过 3驳回）',
  `process_instance_id` varchar(64) NULL DEFAULT NULL COMMENT 'BPM 流程实例编号（预留，后续接入 BPM 时使用）',
  `approver_id` bigint NULL DEFAULT NULL COMMENT '审批人编号',
  `approve_time` datetime NULL DEFAULT NULL COMMENT '审批时间',
  `approve_remark` varchar(500) NULL DEFAULT NULL COMMENT '审批意见',
  `creator` varchar(64) NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_student_id` (`student_id`) USING BTREE,
  KEY `idx_approval_status` (`approval_status`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学生学籍变更记录表（轻量记录，后续可替换为 BPM）';

-- 7. 学期表
--    唯一键为「学年 + 学期」；is_current 同租户唯一，由 Service 层按起止日期自动维护；archived 归档后只读
DROP TABLE IF EXISTS `edu_semester`;
CREATE TABLE `edu_semester` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '学期编号',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `name` varchar(64) NOT NULL COMMENT '学期名称（如 2025-2026学年第一学期）',
  `school_year` varchar(32) NOT NULL COMMENT '学年（如 2025-2026）',
  `term` tinyint NOT NULL COMMENT '学期（字典 edu_semester_term，1第一学期 2第二学期）',
  `start_date` date NULL DEFAULT NULL COMMENT '开始日期',
  `end_date` date NULL DEFAULT NULL COMMENT '结束日期',
  `is_current` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否当前学期',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0未开始 1进行中 2已结束，由起止日期动态推导，见 SemesterService）',
  `archived` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否归档（归档后只读）',
  `remark` varchar(500) NULL DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `active_school_year_term` varchar(64) GENERATED ALWAYS AS (IF(`deleted` = b'0', CONCAT(`school_year`, '-', `term`), NULL)) VIRTUAL COMMENT '未删除时的「学年-学期」组合（用于唯一约束，已删除记录为NULL）',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  UNIQUE KEY `uk_tenant_school_year_term` (`tenant_id`, `active_school_year_term`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学期表（模块四 学期管理）';

-- 8. 学期校历 / 时间节点表，由 type 区分节点类型
DROP TABLE IF EXISTS `edu_semester_calendar`;
CREATE TABLE `edu_semester_calendar` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `semester_id` bigint NOT NULL COMMENT '学期编号',
  `name` varchar(128) NOT NULL COMMENT '节点名称',
  `type` tinyint NOT NULL COMMENT '节点类型（字典 edu_semester_calendar_type）',
  `start_date` date NULL DEFAULT NULL COMMENT '开始日期',
  `end_date` date NULL DEFAULT NULL COMMENT '结束日期',
  `week_no` int NULL DEFAULT NULL COMMENT '教学周次',
  `color` varchar(32) NULL DEFAULT NULL COMMENT '展示颜色（为空时前端按 type 的字典颜色兜底）',
  `remark` varchar(500) NULL DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_semester_id` (`semester_id`) USING BTREE,
  KEY `idx_semester_date` (`semester_id`, `start_date`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学期校历/时间节点表';

-- 9. 变更日志表（通用审计表，以 biz_module 区分模块）
--    一次性事件写一行、字段级修改每字段写一行；before/after 存中文文字；只追加不修改不物理删除。
DROP TABLE IF EXISTS `edu_change_log`;
CREATE TABLE `edu_change_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `biz_module` varchar(64) NOT NULL COMMENT '业务模块（如：学生管理）',
  `biz_id` bigint NULL DEFAULT NULL COMMENT '业务主键（如：学生编号）',
  `biz_no` varchar(64) NULL DEFAULT NULL COMMENT '业务编号（如：学号），用于检索',
  `biz_name` varchar(64) NULL DEFAULT NULL COMMENT '业务名称（如：学生姓名），用于展示',
  `action` tinyint NOT NULL COMMENT '操作类型（字典 edu_change_log_action，1新增 2修改 3删除 4学籍状态变更 5调班 6复学 7导入）',
  `field_name` varchar(64) NULL DEFAULT NULL COMMENT '变更字段（英文名，非字段级记录时为空）',
  `field_label` varchar(64) NULL DEFAULT NULL COMMENT '变更字段中文名',
  `before_value` varchar(512) NULL DEFAULT NULL COMMENT '变更前值（文字）',
  `after_value` varchar(512) NULL DEFAULT NULL COMMENT '变更后值（文字）',
  `operator_id` bigint NULL DEFAULT NULL COMMENT '操作人编号（系统关联用户名 / 帐号编号）',
  `operator_name` varchar(64) NULL DEFAULT NULL COMMENT '操作人姓名',
  `remark` varchar(500) NULL DEFAULT NULL COMMENT '备注 / 摘要',
  `creator` varchar(64) NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_biz` (`biz_module`, `biz_id`) USING BTREE,
  KEY `idx_biz_no` (`biz_no`) USING BTREE,
  KEY `idx_operator_id` (`operator_id`) USING BTREE,
  KEY `idx_create_time` (`create_time`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '变更日志表（档案变更历史，通用，本期只接入模块三 学生管理）';

-- 专业表 dict_major（为 edu_student.major 提供下拉数据源）
-- 注意：年级 / 班级的数据源就是 edu_grade / edu_class，不要再建 dict_grade / dict_class。
DROP TABLE IF EXISTS `dict_major`;
CREATE TABLE `dict_major` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '专业编号',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `grade_id` bigint NULL DEFAULT NULL COMMENT '关联年级编号（模块五 edu_grade；为空表示不限年级的通用专业）',
  `code` varchar(32) NULL DEFAULT NULL COMMENT '专业编码',
  `name` varchar(128) NOT NULL COMMENT '专业名称',
  `dept_id` bigint NULL DEFAULT NULL COMMENT '所属院系编号（system_dept）',
  `sort` int NOT NULL DEFAULT 0 COMMENT '显示顺序',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0启用 1停用）',
  `remark` varchar(500) NULL DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_grade_id` (`grade_id`) USING BTREE,
  KEY `idx_dept_id` (`dept_id`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '专业表（模块五 年级班级管理，与 edu_grade/edu_class 同模块）';

-- 专业基础数据（幂等）：仅为让「专业」下拉有可选值
INSERT INTO dict_major (grade_id, code, name, dept_id, sort, status, remark, tenant_id, creator, create_time, updater, update_time, deleted)
SELECT NULL, d.code, d.name, NULL, d.sort_no, 0, '专业基础数据（模块五 年级班级管理）', 1, 'admin', NOW(), 'admin', NOW(), b'0'
FROM (
    SELECT 'M01' AS code, '软件工程' AS name, 1 AS sort_no
    UNION ALL SELECT 'M02', '计算机科学与技术', 2
    UNION ALL SELECT 'M03', '大数据技术', 3
    UNION ALL SELECT 'M04', '人工智能技术应用', 4
    UNION ALL SELECT 'M05', '电子信息工程', 5
) d
WHERE NOT EXISTS (SELECT 1 FROM dict_major x WHERE x.code = d.code AND x.tenant_id = 1 AND x.deleted = b'0');

-- 字典（页面下拉、标签展示依赖；幂等可重复执行）
INSERT INTO system_dict_type (name, type, status, remark, creator, create_time, updater, update_time, deleted)
SELECT d.dict_name, d.dict_type, 0, d.remark, 'admin', NOW(), 'admin', NOW(), b'0'
FROM (
    SELECT '学生状态' AS dict_name, 'edu_student_status' AS dict_type, '学生学籍状态（在读/休学/退学/毕业/复学）' AS remark
    UNION ALL SELECT '学生变更类型', 'edu_student_change_type', '学生学籍变更记录类型'
    UNION ALL SELECT '审批状态', 'edu_approval_status', '轻量审批状态（语义对齐 BPM）'
    UNION ALL SELECT '学期', 'edu_semester_term', '学期（第一学期 / 第二学期）'
    UNION ALL SELECT '学期状态', 'edu_semester_status', '学期状态（由起止日期自动推导）'
    UNION ALL SELECT '校历节点类型', 'edu_semester_calendar_type', '学期校历 / 时间节点类型'
    UNION ALL SELECT '政治面貌', 'edu_political_status', '学生政治面貌'
    UNION ALL SELECT '学制', 'edu_education_system', '学生学制（二年制 / 四年制 / 五年制）'
    UNION ALL SELECT '变更操作类型', 'edu_change_log_action', '档案变更历史的操作类型（新增/修改/删除/学籍状态变更/调班/复学/导入）'
) d
WHERE NOT EXISTS (SELECT 1 FROM system_dict_type t WHERE t.type = d.dict_type AND t.deleted = b'0');

INSERT INTO system_dict_data (sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted)
SELECT d.sort_no, d.label, d.dict_value, d.dict_type, 0, d.color_type, '', d.label, 'admin', NOW(), 'admin', NOW(), b'0'
FROM (
    SELECT 1 AS sort_no, '在读' AS label, '0' AS dict_value, 'edu_student_status' AS dict_type, 'primary' AS color_type
    UNION ALL SELECT 2, '休学', '1', 'edu_student_status', 'warning'
    UNION ALL SELECT 3, '退学', '2', 'edu_student_status', 'danger'
    UNION ALL SELECT 4, '毕业', '3', 'edu_student_status', 'info'
    UNION ALL SELECT 5, '复学', '4', 'edu_student_status', 'success'
    UNION ALL SELECT 1, '学籍状态变更', '1', 'edu_student_change_type', 'primary'
    UNION ALL SELECT 2, '调班', '2', 'edu_student_change_type', 'warning'
    UNION ALL SELECT 3, '复学', '3', 'edu_student_change_type', 'success'
    UNION ALL SELECT 0, '草稿', '0', 'edu_approval_status', 'info'
    UNION ALL SELECT 1, '审批中', '1', 'edu_approval_status', 'primary'
    UNION ALL SELECT 2, '通过', '2', 'edu_approval_status', 'success'
    UNION ALL SELECT 3, '驳回', '3', 'edu_approval_status', 'danger'
    UNION ALL SELECT 1, '第一学期', '1', 'edu_semester_term', 'primary'
    UNION ALL SELECT 2, '第二学期', '2', 'edu_semester_term', 'success'
    UNION ALL SELECT 0, '未开始', '0', 'edu_semester_status', 'warning'
    UNION ALL SELECT 1, '进行中', '1', 'edu_semester_status', 'success'
    UNION ALL SELECT 2, '已结束', '2', 'edu_semester_status', 'info'
    UNION ALL SELECT 1, '开学日', '1', 'edu_semester_calendar_type', 'primary'
    UNION ALL SELECT 2, '注册截止', '2', 'edu_semester_calendar_type', 'warning'
    UNION ALL SELECT 3, '放假日', '3', 'edu_semester_calendar_type', 'info'
    UNION ALL SELECT 4, '考试周', '4', 'edu_semester_calendar_type', 'danger'
    UNION ALL SELECT 5, '节假日', '5', 'edu_semester_calendar_type', 'success'
    UNION ALL SELECT 6, '教学周', '6', 'edu_semester_calendar_type', 'primary'
    UNION ALL SELECT 7, '活动', '7', 'edu_semester_calendar_type', 'info'
    UNION ALL SELECT 1, '中共党员', '1', 'edu_political_status', 'danger'
    UNION ALL SELECT 2, '中共预备党员', '2', 'edu_political_status', 'danger'
    UNION ALL SELECT 3, '共青团员', '3', 'edu_political_status', 'primary'
    UNION ALL SELECT 4, '民主党派', '4', 'edu_political_status', 'warning'
    UNION ALL SELECT 5, '群众', '5', 'edu_political_status', 'info'
    UNION ALL SELECT 1, '二年制', '1', 'edu_education_system', 'info'
    UNION ALL SELECT 3, '四年制', '3', 'edu_education_system', 'success'
    UNION ALL SELECT 4, '五年制', '4', 'edu_education_system', 'warning'
    UNION ALL SELECT 1, '新增', '1', 'edu_change_log_action', 'success'
    UNION ALL SELECT 2, '修改', '2', 'edu_change_log_action', 'primary'
    UNION ALL SELECT 3, '删除', '3', 'edu_change_log_action', 'danger'
    UNION ALL SELECT 4, '学籍状态变更', '4', 'edu_change_log_action', 'warning'
    UNION ALL SELECT 5, '调班', '5', 'edu_change_log_action', 'warning'
    UNION ALL SELECT 6, '复学', '6', 'edu_change_log_action', 'success'
    UNION ALL SELECT 7, '导入', '7', 'edu_change_log_action', 'info'
) d
WHERE NOT EXISTS (SELECT 1 FROM system_dict_data x WHERE x.dict_type = d.dict_type AND x.value = d.dict_value AND x.deleted = b'0');

-- 清理历史字典（幂等）：移除「培养层次」字典、下架学制「三年制」
DELETE FROM system_dict_data WHERE dict_type = 'edu_training_level';
DELETE FROM system_dict_type WHERE type = 'edu_training_level';
UPDATE system_dict_data SET deleted = b'1', update_time = NOW()
WHERE dict_type = 'edu_education_system' AND value = '2' AND deleted = b'0';

-- 菜单与按钮权限：学生管理 / 学期管理 / 变更日志（幂等，父节点统一用 @eduDirId）
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status)
SELECT '学生管理', '', 2, 3, @eduDirId, 'student', 'ep:user-filled', 'edu/student/index', 'EduStudent', 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM system_menu m WHERE m.component_name = 'EduStudent' AND m.deleted = b'0');
SET @studentMenuId := (SELECT id FROM system_menu WHERE component_name = 'EduStudent' AND deleted = b'0' ORDER BY id LIMIT 1);

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status)
SELECT d.menu_name, d.permission, 3, d.sort_no, @studentMenuId, '', '', '', 0
FROM (
    SELECT '学生查询' AS menu_name, 'edu:student:query' AS permission, 1 AS sort_no
    UNION ALL SELECT '学生创建', 'edu:student:create', 2
    UNION ALL SELECT '学生更新', 'edu:student:update', 3
    UNION ALL SELECT '学生删除', 'edu:student:delete', 4
    UNION ALL SELECT '学生导出', 'edu:student:export', 5
    UNION ALL SELECT '学生导入', 'edu:student:import', 6
    UNION ALL SELECT '学生标签查询', 'edu:student:tag:query', 7
    UNION ALL SELECT '学生标签创建', 'edu:student:tag:create', 8
    UNION ALL SELECT '学生标签更新', 'edu:student:tag:update', 9
    UNION ALL SELECT '学生标签删除', 'edu:student:tag:delete', 10
) d
WHERE NOT EXISTS (SELECT 1 FROM system_menu m WHERE m.permission = d.permission AND m.deleted = b'0');

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status)
SELECT '学期管理', '', 2, 4, @eduDirId, 'semester', 'ep:calendar', 'edu/semester/index', 'EduSemester', 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM system_menu m WHERE m.component_name = 'EduSemester' AND m.deleted = b'0');
SET @semesterMenuId := (SELECT id FROM system_menu WHERE component_name = 'EduSemester' AND deleted = b'0' ORDER BY id LIMIT 1);

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status)
SELECT d.menu_name, d.permission, 3, d.sort_no, @semesterMenuId, '', '', '', 0
FROM (
    SELECT '学期查询' AS menu_name, 'edu:semester:query' AS permission, 1 AS sort_no
    UNION ALL SELECT '学期创建', 'edu:semester:create', 2
    UNION ALL SELECT '学期更新', 'edu:semester:update', 3
    UNION ALL SELECT '学期删除', 'edu:semester:delete', 4
    UNION ALL SELECT '校历查询', 'edu:semester:calendar:query', 5
    UNION ALL SELECT '校历创建', 'edu:semester:calendar:create', 6
    UNION ALL SELECT '校历更新', 'edu:semester:calendar:update', 7
    UNION ALL SELECT '校历删除', 'edu:semester:calendar:delete', 8
) d
WHERE NOT EXISTS (SELECT 1 FROM system_menu m WHERE m.permission = d.permission AND m.deleted = b'0');

-- 二级菜单：变更日志（与「学生管理」平级挂在 @eduDirId 下，避免二级套二级导致路由为空）
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status)
SELECT '变更日志', '', 2, 5, @eduDirId, 'changelog', 'ep:document', 'edu/changelog/index', 'EduChangeLog', 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM system_menu m WHERE m.component_name = 'EduChangeLog' AND m.deleted = b'0');
SET @changeLogMenuId := (SELECT id FROM system_menu WHERE component_name = 'EduChangeLog' AND deleted = b'0' ORDER BY id LIMIT 1);

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status)
SELECT d.menu_name, d.permission, 3, d.sort_no, @changeLogMenuId, '', '', '', 0
FROM (
    SELECT '变更日志查询' AS menu_name, 'edu:change-log:query' AS permission, 1 AS sort_no
    UNION ALL SELECT '变更日志导出', 'edu:change-log:export', 2
) d
WHERE NOT EXISTS (SELECT 1 FROM system_menu m WHERE m.permission = d.permission AND m.deleted = b'0');

-- 角色与授权（复用框架 system_role / system_role_menu）
-- 角色 code 为稳定契约，勿随意改动：管理员 data_scope=1、辅导员/教师=4、学生类=5；角色均建在租户 1。
INSERT INTO system_role (name, code, sort, data_scope, status, type, remark, tenant_id, creator, create_time, updater, update_time, deleted)
SELECT d.role_name, d.role_code, d.sort_no, d.data_scope, 0, 2, d.remark, 1, 'admin', NOW(), 'admin', NOW(), b'0'
FROM (
    SELECT '学院管理员' AS role_name, 'edu_admin' AS role_code, 10 AS sort_no, 1 AS data_scope, '学院管理系统管理员（矩阵：全部模块 读写 + 权限分配）' AS remark
    UNION ALL SELECT '学院辅导员', 'edu_counselor', 11, 4, '辅导员（模块三/四 读写；矩阵：仅本班数据）'
    UNION ALL SELECT '学院教师', 'edu_teacher', 12, 4, '教师（模块三/四 只读；矩阵：仅本人授课班级数据）'
    UNION ALL SELECT '学院学生', 'edu_student', 13, 5, '学生（模块三 仅查看本人、模块四 只读；矩阵：仅本人数据）'
    UNION ALL SELECT '学院学生-学委', 'edu_student_committee', 14, 5, '学习委员（在学生基础上扩展学习管理读写，待学习管理模块上线后追加权限）'
    UNION ALL SELECT '学院学生-纪委', 'edu_student_discipline', 15, 5, '纪律委员（在学生基础上扩展考勤管理读写，待考勤管理模块上线后追加权限）'
) d
WHERE NOT EXISTS (SELECT 1 FROM system_role r WHERE r.code = d.role_code AND r.tenant_id = 1 AND r.deleted = b'0');

SET @roleEduAdminId      := (SELECT id FROM system_role WHERE code = 'edu_admin' AND tenant_id = 1 AND deleted = b'0' ORDER BY id LIMIT 1);
SET @roleEduCounselorId  := (SELECT id FROM system_role WHERE code = 'edu_counselor' AND tenant_id = 1 AND deleted = b'0' ORDER BY id LIMIT 1);
SET @roleEduTeacherId    := (SELECT id FROM system_role WHERE code = 'edu_teacher' AND tenant_id = 1 AND deleted = b'0' ORDER BY id LIMIT 1);
SET @roleEduStudentId    := (SELECT id FROM system_role WHERE code = 'edu_student' AND tenant_id = 1 AND deleted = b'0' ORDER BY id LIMIT 1);
SET @roleEduCommitteeId  := (SELECT id FROM system_role WHERE code = 'edu_student_committee' AND tenant_id = 1 AND deleted = b'0' ORDER BY id LIMIT 1);
SET @roleEduDisciplineId := (SELECT id FROM system_role WHERE code = 'edu_student_discipline' AND tenant_id = 1 AND deleted = b'0' ORDER BY id LIMIT 1);

-- 幂等同步角色数据范围（修正老库中已存在的角色）
UPDATE system_role r
JOIN (
    SELECT 'edu_admin' AS role_code, 1 AS data_scope
    UNION ALL SELECT 'edu_counselor', 4
    UNION ALL SELECT 'edu_teacher', 4
    UNION ALL SELECT 'edu_student', 5
    UNION ALL SELECT 'edu_student_committee', 5
    UNION ALL SELECT 'edu_student_discipline', 5
) d ON d.role_code = r.code
SET r.data_scope = d.data_scope, r.updater = 'admin', r.update_time = NOW()
WHERE r.tenant_id = 1 AND r.deleted = b'0' AND (r.data_scope IS NULL OR r.data_scope <> d.data_scope);

-- 先清空这些角色的旧授权，再重新授权（幂等）
DELETE rm FROM system_role_menu rm
WHERE rm.role_id IN (@roleEduAdminId, @roleEduCounselorId, @roleEduTeacherId,
                     @roleEduStudentId, @roleEduCommitteeId, @roleEduDisciplineId);

-- 授权原则：仅授权学生管理 / 学期管理 / 变更日志；辅导员 / 教师菜单不在此授权；变更日志因无数据范围列仅授权管理员

-- 学院管理员：学生管理 + 学期管理 + 变更日志 全部读写
INSERT INTO system_role_menu (role_id, menu_id, tenant_id, creator, create_time, updater, update_time, deleted)
SELECT @roleEduAdminId, m.id, 1, 'admin', NOW(), 'admin', NOW(), b'0'
FROM system_menu m
WHERE m.deleted = b'0'
  AND (m.id = @eduDirId
       OR m.component_name IN ('EduStudent', 'EduSemester', 'EduChangeLog')
       OR m.permission LIKE 'edu:student:%'
       OR m.permission LIKE 'edu:semester:%'
       OR m.permission LIKE 'edu:change-log:%');

-- 辅导员：学生 + 学期 读写
INSERT INTO system_role_menu (role_id, menu_id, tenant_id, creator, create_time, updater, update_time, deleted)
SELECT @roleEduCounselorId, m.id, 1, 'admin', NOW(), 'admin', NOW(), b'0'
FROM system_menu m
WHERE m.deleted = b'0'
  AND (m.id = @eduDirId
       OR m.component_name IN ('EduStudent', 'EduSemester')
       OR m.permission LIKE 'edu:student:%'
       OR m.permission LIKE 'edu:semester:%');

-- 教师：只读（学生 查询 / 导出 / 标签查询，学期 查询 / 校历查询）
INSERT INTO system_role_menu (role_id, menu_id, tenant_id, creator, create_time, updater, update_time, deleted)
SELECT @roleEduTeacherId, m.id, 1, 'admin', NOW(), 'admin', NOW(), b'0'
FROM system_menu m
WHERE m.deleted = b'0'
  AND (m.id = @eduDirId
       OR m.component_name IN ('EduStudent', 'EduSemester')
       OR m.permission IN ('edu:student:query', 'edu:student:export', 'edu:student:tag:query',
                           'edu:semester:query', 'edu:semester:calendar:query'));

-- 学生 / 学委 / 纪委：学生 仅查询、学期 仅查询
INSERT INTO system_role_menu (role_id, menu_id, tenant_id, creator, create_time, updater, update_time, deleted)
SELECT r.role_id, m.id, 1, 'admin', NOW(), 'admin', NOW(), b'0'
FROM (SELECT @roleEduStudentId AS role_id
      UNION ALL SELECT @roleEduCommitteeId
      UNION ALL SELECT @roleEduDisciplineId) r
CROSS JOIN system_menu m
WHERE m.deleted = b'0'
  AND (m.id = @eduDirId
       OR m.component_name IN ('EduStudent', 'EduSemester')
       OR m.permission IN ('edu:student:query', 'edu:semester:query', 'edu:semester:calendar:query'));

-- 数据播种（不写死业务数据：年级取默认兜底，班级由既有辅导员带班数据派生）
-- 顶级院系「能工智人」（所属院系下拉的数据源；注意用字为「智」）
INSERT INTO system_dept (name, parent_id, sort, leader_user_id, phone, email, status, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT '能工智人', 0, 1, NULL, NULL, NULL, 0, 'admin', NOW(), 'admin', NOW(), b'0', 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM system_dept d WHERE d.name = '能工智人' AND d.tenant_id = 1 AND d.deleted = b'0');

-- 默认年级（供班级/学生挂靠）
INSERT INTO edu_grade (name, code, enrollment_year, dept_id, major, counselor_id, sort, status, remark, tenant_id, creator, create_time, updater, update_time, deleted)
SELECT '未分年级', NULL, NULL, NULL, NULL, NULL, 0, 0, '系统默认年级（模块五 年级班级管理 最小实现，后续由年级管理维护）', 1, 'admin', NOW(), 'admin', NOW(), b'0'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM edu_grade g WHERE g.name = '未分年级' AND g.tenant_id = 1 AND g.deleted = b'0');

-- 既有辅导员带班数据 → edu_class（已存在同名班级则跳过）
-- edu_counselor_class 未指定字符集，这里显式 COLLATE，避免 Illegal mix of collations 报错。
INSERT INTO edu_class (grade_id, name, class_no, dept_id, counselor_id, head_teacher_id, sort, status, remark, tenant_id, creator, create_time, updater, update_time, deleted)
SELECT g.id, cc.class_name, NULL, c.dept_id, cc.counselor_id, NULL, 0, 0, '由辅导员带班表(edu_counselor_class)播种，可在年级班级管理中调整', 1, 'admin', NOW(), 'admin', NOW(), b'0'
FROM (SELECT cc.class_name COLLATE utf8mb4_unicode_ci AS class_name, MIN(cc.counselor_id) AS counselor_id
      FROM edu_counselor_class cc WHERE cc.deleted = b'0' GROUP BY cc.class_name) cc
JOIN edu_grade g ON g.name = '未分年级' AND g.tenant_id = 1 AND g.deleted = b'0'
LEFT JOIN edu_counselor c ON c.id = cc.counselor_id AND c.deleted = b'0'
WHERE NOT EXISTS (SELECT 1 FROM edu_class x WHERE x.name = cc.class_name AND x.tenant_id = 1 AND x.deleted = b'0');

-- 4.1 备份将被删除的旧列
DROP TABLE IF EXISTS `edu_student_parent_bak_20261017`;
CREATE TABLE `edu_student_parent_bak_20261017` (
  `id` bigint NOT NULL COMMENT '学生编号',
  `father_name` varchar(64) NULL DEFAULT NULL COMMENT '父亲姓名（迁移前）',
  `father_mobile` varchar(20) NULL DEFAULT NULL COMMENT '父亲电话（迁移前）',
  `mother_name` varchar(64) NULL DEFAULT NULL COMMENT '母亲姓名（迁移前）',
  `mother_mobile` varchar(20) NULL DEFAULT NULL COMMENT '母亲电话（迁移前）',
  `training_level` tinyint NULL DEFAULT NULL COMMENT '培养层次（迁移前）',
  `qq` varchar(32) NULL DEFAULT NULL COMMENT 'QQ（迁移前）',
  `wechat` varchar(64) NULL DEFAULT NULL COMMENT '微信（迁移前）',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学生旧字段备份表（2026-10 迭代回滚用）';
-- 旧列不存在时跳过备份
SET @hasCol := (SELECT COUNT(*) FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'edu_student' AND COLUMN_NAME = 'father_name');
SET @sql := IF(@hasCol > 0,
    CONCAT('INSERT INTO `edu_student_parent_bak_20261017` (id, father_name, father_mobile, mother_name, mother_mobile, training_level, qq, wechat) ',
           'SELECT id, father_name, father_mobile, mother_name, mother_mobile, training_level, qq, wechat FROM `edu_student`'),
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 4.2 新增列
SET @hasCol := (SELECT COUNT(*) FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'edu_student' AND COLUMN_NAME = 'guardian_name');
SET @sql := IF(@hasCol = 0,
    'ALTER TABLE `edu_student` ADD COLUMN `guardian_name` varchar(64) NULL DEFAULT NULL COMMENT ''监护人姓名（由父/母姓名合并，优先父亲）'' AFTER `user_id`',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @hasCol := (SELECT COUNT(*) FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'edu_student' AND COLUMN_NAME = 'guardian_mobile');
SET @sql := IF(@hasCol = 0,
    'ALTER TABLE `edu_student` ADD COLUMN `guardian_mobile` varchar(20) NULL DEFAULT NULL COMMENT ''监护人手机号（由父/母电话合并，优先父亲）'' AFTER `guardian_name`',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @hasCol := (SELECT COUNT(*) FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'edu_student' AND COLUMN_NAME = 'major_id');
SET @sql := IF(@hasCol = 0,
    'ALTER TABLE `edu_student` ADD COLUMN `major_id` bigint NULL DEFAULT NULL COMMENT ''专业编号（占位表 dict_major，字典中心模块待接入）'' AFTER `class_id`, ADD KEY `idx_major_id` (`major_id`)',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 4.3 父母 → 监护人回填（优先父亲）
-- 旧列已删时直接引用会抛 Unknown column 并中断整份脚本，故用动态 SQL 做列存在性守卫。
SET @hasCol := (SELECT COUNT(*) FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'edu_student' AND COLUMN_NAME = 'father_name');
SET @sql := IF(@hasCol > 0,
    'UPDATE `edu_student`
        SET `guardian_name`   = COALESCE(NULLIF(TRIM(`father_name`), ''''), NULLIF(TRIM(`mother_name`), '''')),
            `guardian_mobile` = COALESCE(NULLIF(TRIM(`father_mobile`), ''''), NULLIF(TRIM(`mother_mobile`), ''''))
      WHERE (`guardian_name` IS NULL OR `guardian_mobile` IS NULL)
        AND (`father_name` IS NOT NULL OR `father_mobile` IS NOT NULL
             OR `mother_name` IS NOT NULL OR `mother_mobile` IS NOT NULL)',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 4.4 删除旧列（仅当列仍存在时执行）
SET @hasCol := (SELECT COUNT(*) FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'edu_student' AND COLUMN_NAME = 'father_name');
SET @sql := IF(@hasCol > 0,
    'ALTER TABLE `edu_student` DROP COLUMN `father_name`, DROP COLUMN `father_mobile`, DROP COLUMN `mother_name`, DROP COLUMN `mother_mobile`',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @hasCol := (SELECT COUNT(*) FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'edu_student' AND COLUMN_NAME = 'training_level');
SET @sql := IF(@hasCol > 0, 'ALTER TABLE `edu_student` DROP COLUMN `training_level`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @hasCol := (SELECT COUNT(*) FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'edu_student' AND COLUMN_NAME = 'qq');
SET @sql := IF(@hasCol > 0, 'ALTER TABLE `edu_student` DROP COLUMN `qq`, DROP COLUMN `wechat`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 4.5 edu_student_tag 结构升级（排序唯一约束 + 颜色长度）
-- a) 先加生成列，暂不加唯一键，避免存量重复 sort 导致加键失败
SET @hasCol := (SELECT COUNT(*) FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'edu_student_tag' AND COLUMN_NAME = 'active_sort');
SET @sql := IF(@hasCol = 0,
    'ALTER TABLE `edu_student_tag` ADD COLUMN `active_sort` int GENERATED ALWAYS AS (IF(`deleted` = b''0'', `sort`, NULL)) VIRTUAL COMMENT ''未删除时的显示顺序（唯一约束用）''',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- b) 存量序号归一化为 0..n-1（按 status, sort, id 重排，幂等）
--    先算出目标顺序再写回；先落负数区再写终值，避免唯一键中途冲突。
DROP TEMPORARY TABLE IF EXISTS `tmp_edu_tag_sort`;
CREATE TEMPORARY TABLE `tmp_edu_tag_sort` (
  `id` bigint NOT NULL COMMENT '标签编号',
  `new_sort` int NOT NULL COMMENT '重排后的显示顺序',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB;
INSERT INTO `tmp_edu_tag_sort` (`id`, `new_sort`)
SELECT `id`, ROW_NUMBER() OVER (PARTITION BY `tenant_id` ORDER BY `status` ASC, `sort` ASC, `id` ASC) - 1
FROM `edu_student_tag` WHERE `deleted` = b'0';
UPDATE `edu_student_tag` t JOIN `tmp_edu_tag_sort` s ON s.id = t.id SET t.sort = -s.new_sort - 1;
UPDATE `edu_student_tag` t JOIN `tmp_edu_tag_sort` s ON s.id = t.id SET t.sort = s.new_sort;
DROP TEMPORARY TABLE `tmp_edu_tag_sort`;

-- c) 加排序唯一约束（幂等）
SET @hasIdx := (SELECT COUNT(*) FROM information_schema.STATISTICS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'edu_student_tag' AND INDEX_NAME = 'uk_tenant_sort');
SET @sql := IF(@hasIdx = 0, 'ALTER TABLE `edu_student_tag` ADD UNIQUE KEY `uk_tenant_sort` (`tenant_id`, `active_sort`)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- d) 颜色列长度收敛为 varchar(16)
SET @colLen := (SELECT COALESCE(CHARACTER_MAXIMUM_LENGTH, 0) FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'edu_student_tag' AND COLUMN_NAME = 'color');
SET @sql := IF(@colLen > 16, 'ALTER TABLE `edu_student_tag` MODIFY COLUMN `color` varchar(16) NOT NULL DEFAULT ''#409EFF'' COMMENT ''标签颜色（固定 16 色，由后端 TagColorEnum 校验）''', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- BPM：学生学籍变更审批
-- 流程定义 key = edu-student-change，需在 BPM 后台新建并发布该流程模型；未发布时自动降级为「轻量审批」。
-- 注意：yudao 的 bpm_* 业务表不在本脚本内，需另行导入标准脚本，否则 BPM 后台无法发布模型。

-- 数据订正：清理「学生-标签」孤儿关联（幂等）
DELETE rel
FROM edu_student_tag_rel rel
LEFT JOIN edu_student_tag tag
       ON tag.id = rel.tag_id
      AND tag.tenant_id = rel.tenant_id
      AND tag.deleted = b'0'
WHERE tag.id IS NULL;
