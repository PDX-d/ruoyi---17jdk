package cn.iocoder.yudao.module.edu.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * EDU 错误码常量
 *
 * EDU 学院管理系统，使用 1-090-000-000 段
 *
 * @author 芋道源码
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

}
