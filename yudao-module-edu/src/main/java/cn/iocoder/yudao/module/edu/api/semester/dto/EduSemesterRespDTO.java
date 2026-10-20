package cn.iocoder.yudao.module.edu.api.semester.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 学期 Response DTO
 *
 * 供其它模块通过 {@link cn.iocoder.yudao.module.edu.api.semester.EduSemesterApi} 查询学期信息。
 */
@Data
public class EduSemesterRespDTO {

    /**
     * 学期编号
     */
    private Long id;
    /**
     * 学期名称（如 2025-2026学年第一学期）
     */
    private String name;
    /**
     * 学年（如 2025-2026）
     */
    private String schoolYear;
    /**
     * 学期
     *
     * 1 - 第一学期；2 - 第二学期
     */
    private Integer term;
    /**
     * 开始日期
     */
    private LocalDate startDate;
    /**
     * 结束日期
     */
    private LocalDate endDate;
    /**
     * 是否当前学期
     */
    private Boolean isCurrent;
    /**
     * 状态
     *
     * 0 - 未开始；1 - 进行中；2 - 已结束
     */
    private Integer status;

}
