package cn.iocoder.yudao.module.edu.controller.admin.semester.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 学期 Response VO")
@Data
public class SemesterRespVO {

    @Schema(description = "学期编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "学期名称", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "能工智人学院2025-2026学年第一学期")
    private String name;

    @Schema(description = "学年", requiredMode = Schema.RequiredMode.REQUIRED, example = "2025-2026")
    private String schoolYear;

    @Schema(description = "学年起始年份（由 schoolYear 推导，供表单双年份下拉回显）", example = "2025")
    private Integer schoolYearStart;

    @Schema(description = "学年结束年份（由 schoolYear 推导，供表单双年份下拉回显）", example = "2026")
    private Integer schoolYearEnd;

    @Schema(description = "学期", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer term;

    @Schema(description = "开始日期", example = "2025-09-01")
    private LocalDate startDate;

    @Schema(description = "结束日期", example = "2026-01-15")
    private LocalDate endDate;

    @Schema(description = "是否当前学期", example = "false")
    private Boolean isCurrent;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

    @Schema(description = "是否归档", example = "false")
    private Boolean archived;

    @Schema(description = "备注", example = "备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
