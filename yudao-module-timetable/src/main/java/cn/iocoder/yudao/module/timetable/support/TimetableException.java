package cn.iocoder.yudao.module.timetable.support;

import cn.iocoder.yudao.framework.common.exception.ServiceException;

public final class TimetableException {
    private TimetableException() {
    }

    public static ServiceException invalid(String message) {
        return new ServiceException(1_030_000_001, message);
    }

    public static void require(boolean condition, String message) {
        if (!condition) {
            throw invalid(message);
        }
    }
}
