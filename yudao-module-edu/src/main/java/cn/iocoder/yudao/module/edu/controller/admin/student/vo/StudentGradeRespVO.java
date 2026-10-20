package cn.iocoder.yudao.module.edu.controller.admin.student.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理后台 - 学生年级下拉 Response VO
 */
@Schema(description = "管理后台 - 学生年级下拉 Response VO")
@Data
public class StudentGradeRespVO {

    @Schema(description = "年级编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "年级名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "2023级")
    private String name;

}
