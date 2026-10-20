package cn.iocoder.yudao.module.edu.controller.admin.student.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理后台 - 学生标签 Response VO
 */
@Schema(description = "管理后台 - 学生标签 Response VO")
@Data
public class StudentTagRespVO {

    @Schema(description = "标签编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "标签名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "优秀学生")
    private String name;

    @Schema(description = "标签颜色", example = "#409EFF")
    private String color;

    @Schema(description = "显示顺序", example = "0")
    private Integer sort;

    @Schema(description = "状态（0开启 1关闭）", example = "0")
    private Integer status;

    @Schema(description = "备注", example = "备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
