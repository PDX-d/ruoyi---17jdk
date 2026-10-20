package cn.iocoder.yudao.module.edu.controller.admin.student.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 管理后台 - 学生变更审批 Request VO
 */
@Schema(description = "管理后台 - 学生变更审批 Request VO")
@Data
public class StudentChangeApproveReqVO {

    @Schema(description = "变更记录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "变更记录编号不能为空")
    private Long id;

    @Schema(description = "审批结果（2通过 3驳回）", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "审批结果不能为空")
    private Integer approvalStatus;

    @Schema(description = "审批意见", example = "同意")
    private String approveRemark;

}
