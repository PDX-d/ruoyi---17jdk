package cn.iocoder.yudao.module.exam.support;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.module.exam.enums.ErrorCodeConstants;

public final class ExamException {
    private ExamException() {
    }

    public static ServiceException invalid(String message) {
        return error(message);
    }

    public static ServiceException error(String message) {
        return error(ErrorCodeConstants.EXAM_INVALID, message);
    }

    public static ServiceException error(ErrorCode code, String message) {
        return new ServiceException(code.getCode(), message);
    }

    public static void require(boolean condition, String message) {
        if (!condition) {
            throw invalid(message);
        }
    }

    public static void require(boolean condition, ErrorCode code, String message) {
        if (!condition) {
            throw error(code, message);
        }
    }
}
