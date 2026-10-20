package cn.iocoder.yudao.module.edu.controller.admin.student.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 管理后台 - 学生标签新增/修改 Request VO
 *
 * 不接收 sort：序号完全由后端维护。color 固定为 16 色，校验见 TagColorEnum。
 */
@Schema(description = "管理后台 - 学生标签新增/修改 Request VO")
@Data
public class StudentTagSaveReqVO {

    @Schema(description = "标签编号", example = "1")
    private Long id;

    @Schema(description = "标签名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "优秀学生")
    @NotBlank(message = "标签名称不能为空")
    @Size(max = 64, message = "标签名称长度不能超过 64 个字符")
    private String name;

    @Schema(description = "标签颜色（固定 16 色，见 TagColorEnum）", requiredMode = Schema.RequiredMode.REQUIRED, example = "#409EFF")
    @NotBlank(message = "标签颜色不能为空")
    @Size(max = 16, message = "标签颜色长度不能超过 16 个字符")
    private String color;

    @Schema(description = "状态（0启用 1停用）", example = "0")
    private Integer status;

    @Schema(description = "备注", example = "备注")
    private String remark;

}
