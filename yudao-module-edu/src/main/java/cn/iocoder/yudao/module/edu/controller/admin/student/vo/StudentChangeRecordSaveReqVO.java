package cn.iocoder.yudao.module.edu.controller.admin.student.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 管理后台 - 学生变更记录新增 Request VO
 */
@Schema(description = "管理后台 - 学生变更记录新增 Request VO")
@Data
public class StudentChangeRecordSaveReqVO {

    @Schema(description = "学生编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "学生编号不能为空")
    private Long studentId;

    @Schema(description = "变更类型（1状态变更 2调班）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "变更类型不能为空")
    private Integer changeType;

    @Schema(description = "变更前值（状态字典值或班级编号）", example = "0")
    private String fromValue;

    @Schema(description = "变更后值（状态字典值或班级编号）", example = "1")
    private String toValue;

    @Schema(description = "变更原因", example = "因病休学")
    private String reason;

}
