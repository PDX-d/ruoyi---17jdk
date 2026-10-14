-- ========================================================================
-- 学院管理系统（edu 模块）
-- 说明：本脚本包含「辅导员管理」「教师管理」两个模块的建表 DDL 与菜单权限 SQL
-- 数据库：MySQL 8.0+
-- ========================================================================

-- ------------------------------------------------------------------------
-- 1. 辅导员表
-- ------------------------------------------------------------------------
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

-- ------------------------------------------------------------------------
-- 2. 辅导员带班表（一个辅导员可带多个班级，一个班级只能有一个主辅导员）
-- ------------------------------------------------------------------------
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

-- ------------------------------------------------------------------------
-- 3. 教师表
-- ------------------------------------------------------------------------
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

-- ========================================================================
-- 菜单权限 SQL
-- ========================================================================

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
