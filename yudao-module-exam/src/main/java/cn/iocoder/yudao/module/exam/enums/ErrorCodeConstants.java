package cn.iocoder.yudao.module.exam.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

public interface ErrorCodeConstants {
    ErrorCode EXAM_INVALID = new ErrorCode(1_031_000_001, "考务操作不满足约束");
    ErrorCode EXAM_FORBIDDEN = new ErrorCode(1_031_000_002, "无权执行考务操作");
    ErrorCode EXAM_CONFLICT = new ErrorCode(1_031_000_003, "考务数据或版本已变化");
    ErrorCode EXAM_REFERENCE_UNAVAILABLE = new ErrorCode(1_031_000_004, "考务公共数据不可用");
}
