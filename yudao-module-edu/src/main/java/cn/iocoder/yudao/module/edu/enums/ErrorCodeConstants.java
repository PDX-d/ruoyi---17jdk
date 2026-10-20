package cn.iocoder.yudao.module.edu.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * EDU 错误码常量
 *
 * EDU 学院管理系统，使用 1-090-000-000 段
 */
public interface ErrorCodeConstants {

    // ========== 导员管理 1-090-001-000 ==========
    ErrorCode COUNSELOR_NOT_EXISTS = new ErrorCode(1_090_001_000, "辅导员不存在");
    ErrorCode COUNSELOR_JOB_NO_DUPLICATE = new ErrorCode(1_090_001_001, "辅导员工号已存在");
    ErrorCode COUNSELOR_CLASS_NOT_TRANSFERRED = new ErrorCode(1_090_001_002, "该辅导员名下仍有班级未移交，无法删除");
    ErrorCode COUNSELOR_CLASS_ALREADY_ASSIGNED = new ErrorCode(1_090_001_003, "班级【{}】已由其他辅导员担任主辅导员，请先移交");
    ErrorCode COUNSELOR_CLASS_DUPLICATE = new ErrorCode(1_090_001_004, "所带班级已被分配，请刷新后重试");

    // ========== 教师管理 1-090-002-000 ==========
    ErrorCode TEACHER_NOT_EXISTS = new ErrorCode(1_090_002_000, "教师不存在");
    ErrorCode TEACHER_JOB_NO_DUPLICATE = new ErrorCode(1_090_002_001, "教师工号已存在");

    // ========== 学生管理 1-090-003-000 ==========
    ErrorCode STUDENT_NOT_EXISTS = new ErrorCode(1_090_003_000, "学生不存在");
    ErrorCode STUDENT_NO_DUPLICATE = new ErrorCode(1_090_003_001, "学号已存在");
    ErrorCode STUDENT_USER_CREATE_FAILED = new ErrorCode(1_090_003_002, "学生账号创建失败");
    ErrorCode STUDENT_GRADE_NOT_EXISTS = new ErrorCode(1_090_003_003, "年级不存在");
    ErrorCode STUDENT_CLASS_NOT_EXISTS = new ErrorCode(1_090_003_004, "班级不存在");
    ErrorCode STUDENT_MAJOR_NOT_EXISTS = new ErrorCode(1_090_003_005, "专业不存在");
    ErrorCode STUDENT_EDUCATION_SYSTEM_INVALID = new ErrorCode(1_090_003_006, "学制不合法（可选值：1二年制 / 3四年制 / 4五年制）");
    ErrorCode STUDENT_BIRTH_DATE_FUTURE = new ErrorCode(1_090_003_007, "出生日期不能晚于今天");
    ErrorCode STUDENT_TAG_NOT_EXISTS = new ErrorCode(1_090_003_010, "学生标签不存在");
    ErrorCode STUDENT_TAG_NAME_DUPLICATE = new ErrorCode(1_090_003_011, "学生标签名称已存在");
    ErrorCode STUDENT_TAG_COLOR_INVALID = new ErrorCode(1_090_003_012, "学生标签颜色不合法，只能从固定 16 色中选择");
    ErrorCode STUDENT_TAG_SORT_INVALID = new ErrorCode(1_090_003_013, "学生标签排序数据不合法，请刷新页面后重试");
    ErrorCode STUDENT_CHANGE_RECORD_NOT_EXISTS = new ErrorCode(1_090_003_020, "学生变更记录不存在");
    ErrorCode STUDENT_CHANGE_RECORD_ALREADY_HANDLED = new ErrorCode(1_090_003_021, "学生变更记录已审批，无法重复操作");
    ErrorCode STUDENT_CHANGE_VALUE_INVALID = new ErrorCode(1_090_003_022, "变更的目标值不合法，请重新填写");
    ErrorCode STUDENT_IMPORT_LIST_EMPTY = new ErrorCode(1_090_003_030, "导入学生数据不能为空");
    ErrorCode STUDENT_IMPORT_GRADE_NAME_NOT_EXISTS = new ErrorCode(1_090_003_031, "年级【{}】不存在，请填写系统内已有的年级名称");
    ErrorCode STUDENT_IMPORT_CLASS_NAME_NOT_EXISTS = new ErrorCode(1_090_003_032, "班级【{}】不存在，请填写系统内已有的班级名称");
    ErrorCode STUDENT_IMPORT_MAJOR_NAME_NOT_EXISTS = new ErrorCode(1_090_003_033, "专业【{}】不存在，请填写系统内已有的专业名称");
    ErrorCode STUDENT_IMPORT_DEPT_NAME_NOT_EXISTS = new ErrorCode(1_090_003_034, "院系【{}】不存在，请填写系统内已有的院系名称");

    // ========== 学期管理 1-090-004-000 ==========
    ErrorCode SEMESTER_NOT_EXISTS = new ErrorCode(1_090_004_000, "学期不存在");
    ErrorCode SEMESTER_SCHOOL_YEAR_TERM_DUPLICATE = new ErrorCode(1_090_004_001, "该学年学期已存在");
    ErrorCode SEMESTER_CALENDAR_NOT_EXISTS = new ErrorCode(1_090_004_002, "校历节点不存在");
    ErrorCode SEMESTER_ARCHIVED = new ErrorCode(1_090_004_003, "该学期已归档，不允许修改或删除");
    ErrorCode SEMESTER_CURRENT_CANNOT_ARCHIVE = new ErrorCode(1_090_004_004, "当前学期不允许归档，请先切换当前学期");
    ErrorCode SEMESTER_CALENDAR_DATE_OUT_OF_RANGE = new ErrorCode(1_090_004_005, "校历节点日期必须在该学期的起止日期范围内（{} 至 {}）");
    ErrorCode SEMESTER_CALENDAR_DATE_INVALID = new ErrorCode(1_090_004_006, "校历节点结束日期必须晚于或等于开始日期");
    ErrorCode SEMESTER_CALENDAR_COLOR_INVALID = new ErrorCode(1_090_004_007, "校历节点颜色不合法，只能从固定 16 色中选择");

}
