package cn.iocoder.yudao.module.edu.enums.semester;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

/**
 * 学期状态枚举
 *
 * 0 - 未开始；1 - 进行中；2 - 已结束
 */
@Getter
@AllArgsConstructor
public enum SemesterStatusEnum {

    NOT_STARTED(0, "未开始"),
    IN_PROGRESS(1, "进行中"),
    FINISHED(2, "已结束");

    /**
     * 状态值
     */
    private final Integer status;
    /**
     * 状态名
     */
    private final String name;

    /**
     * 依据起止日期动态计算学期状态
     *
     * @param startDate 开始日期，可为空
     * @param endDate   结束日期，可为空
     * @param today     参照日期
     * @return 学期状态值
     */
    public static Integer calculate(LocalDate startDate, LocalDate endDate, LocalDate today) {
        if (startDate == null && endDate == null) {
            // 未维护起止日期时，视为「未开始」，由使用方自行维护
            return NOT_STARTED.getStatus();
        }
        if (startDate != null && today.isBefore(startDate)) {
            return NOT_STARTED.getStatus();
        }
        if (endDate != null && today.isAfter(endDate)) {
            return FINISHED.getStatus();
        }
        return IN_PROGRESS.getStatus();
    }

}
