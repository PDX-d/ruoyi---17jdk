package cn.iocoder.yudao.module.edu.controller.admin.student.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理后台 - 学生班级下拉 Response VO
 */
@Schema(description = "管理后台 - 学生班级下拉 Response VO")
@Data
public class StudentClassRespVO {

    @Schema(description = "班级编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "年级编号", example = "1")
    private Long gradeId;

    @Schema(description = "班级名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "软件工程2301班")
    private String name;

}
