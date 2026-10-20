package cn.iocoder.yudao.module.edu.controller.admin.student.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理后台 - 学生变更记录 Response VO
 */
@Schema(description = "管理后台 - 学生变更记录 Response VO")
@Data
public class StudentChangeRecordRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "学生编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long studentId;

    @Schema(description = "学生姓名", example = "张三")
    private String studentName;

    @Schema(description = "变更类型（1状态变更 2调班）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer changeType;

    @Schema(description = "变更前值", example = "0")
    private String fromValue;

    @Schema(description = "变更后值", example = "1")
    private String toValue;

    @Schema(description = "变更原因", example = "因病休学")
    private String reason;

    @Schema(description = "审批状态（0草稿 1审批中 2通过 3驳回）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer approvalStatus;

    @Schema(description = "BPM 流程实例编号", example = "1024")
    private String processInstanceId;

    @Schema(description = "审批人编号", example = "1")
    private Long approverId;

    @Schema(description = "审批时间")
    private LocalDateTime approveTime;

    @Schema(description = "审批意见", example = "同意")
    private String approveRemark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
