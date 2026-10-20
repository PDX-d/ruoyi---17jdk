package cn.iocoder.yudao.module.edu.controller.admin.semester.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Schema(description = "管理后台 - 学期校历节点新增/修改 Request VO")
@Data
public class SemesterCalendarSaveReqVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "学期编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "学期编号不能为空")
    private Long semesterId;

    @Schema(description = "节点名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "开学日")
    @NotBlank(message = "节点名称不能为空")
    @Size(max = 128, message = "节点名称长度不能超过 128 个字符")
    private String name;

    @Schema(description = "节点类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "节点类型不能为空")
    private Integer type;

    @Schema(description = "开始日期", example = "2025-09-01")
    private LocalDate startDate;

    @Schema(description = "结束日期", example = "2025-09-01")
    private LocalDate endDate;

    @Schema(description = "教学周次", example = "1")
    private Integer weekNo;

    @Schema(description = "展示颜色", example = "#409EFF")
    @Size(max = 32, message = "展示颜色长度不能超过 32 个字符")
    private String color;

    @Schema(description = "备注", example = "备注")
    @Size(max = 500, message = "备注长度不能超过 500 个字符")
    private String remark;

}
