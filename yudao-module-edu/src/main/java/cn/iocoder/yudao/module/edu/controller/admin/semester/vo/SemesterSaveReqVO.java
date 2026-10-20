package cn.iocoder.yudao.module.edu.controller.admin.semester.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;

/**
 * 管理后台 - 学期新增/修改 Request VO
 *
 * name / schoolYear / isCurrent / status / archived 均由后端按日期推导，不接收前端传值；
 * 学年由起止年份推导，强约束「结束年份 = 起始年份 + 1」。
 */
@Schema(description = "管理后台 - 学期新增/修改 Request VO")
@Data
public class SemesterSaveReqVO {

    @Schema(description = "学期编号", example = "1")
    private Long id;

    @Schema(description = "学年起始年份", requiredMode = Schema.RequiredMode.REQUIRED, example = "2025")
    @NotNull(message = "学年起始年份不能为空")
    private Integer schoolYearStart;

    @Schema(description = "学年结束年份（必须等于起始年份 + 1）", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026")
    @NotNull(message = "学年结束年份不能为空")
    private Integer schoolYearEnd;

    @Schema(description = "学期", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "学期不能为空")
    private Integer term;

    @Schema(description = "开始日期", requiredMode = Schema.RequiredMode.REQUIRED, example = "2025-09-01")
    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;

    @Schema(description = "结束日期", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-01-15")
    @NotNull(message = "结束日期不能为空")
    private LocalDate endDate;

    @Schema(description = "备注", example = "备注")
    @Size(max = 500, message = "备注长度不能超过 500 个字符")
    private String remark;

    // 说明：name / schoolYear / isCurrent / status / archived 均由后端推导，不接收前端传值。

    /**
     * 校验：学年结束年份必须等于起始年份 + 1
     */
    @AssertTrue(message = "学年年份不合法：结束年份必须等于起始年份 + 1")
    public boolean isSchoolYearValid() {
        if (schoolYearStart == null || schoolYearEnd == null) {
            // 交由 @NotNull 报错
            return true;
        }
        return schoolYearEnd == schoolYearStart + 1;
    }

    /**
     * 校验：结束日期必须晚于开始日期（不允许小于等于）
     */
    @AssertTrue(message = "结束日期必须晚于开始日期")
    public boolean isDateRangeValid() {
        if (startDate == null || endDate == null) {
            return true;
        }
        return endDate.isAfter(startDate);
    }

}
