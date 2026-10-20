package cn.iocoder.yudao.module.edu.controller.admin.semester.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 学期校历节点 Response VO")
@Data
public class SemesterCalendarRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "学期编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long semesterId;

    @Schema(description = "节点名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "开学日")
    private String name;

    @Schema(description = "节点类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer type;

    @Schema(description = "开始日期", example = "2025-09-01")
    private LocalDate startDate;

    @Schema(description = "结束日期", example = "2025-09-01")
    private LocalDate endDate;

    @Schema(description = "教学周次", example = "1")
    private Integer weekNo;

    @Schema(description = "展示颜色", example = "#409EFF")
    private String color;

    @Schema(description = "备注", example = "备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
