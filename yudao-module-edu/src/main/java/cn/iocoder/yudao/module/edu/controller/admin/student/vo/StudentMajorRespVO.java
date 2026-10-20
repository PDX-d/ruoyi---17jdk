package cn.iocoder.yudao.module.edu.controller.admin.student.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理后台 - 学生专业下拉 Response VO
 *
 * 数据源归属基础数据字典模块，当前取占位表 dict_major。
 * TODO: 字典中心模块待接入
 */
@Schema(description = "管理后台 - 学生专业下拉 Response VO")
@Data
public class StudentMajorRespVO {

    @Schema(description = "专业编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "专业名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "软件工程")
    private String name;

    @Schema(description = "专业编码", example = "M01")
    private String code;

    @Schema(description = "关联年级编号", example = "1")
    private Long gradeId;

}
