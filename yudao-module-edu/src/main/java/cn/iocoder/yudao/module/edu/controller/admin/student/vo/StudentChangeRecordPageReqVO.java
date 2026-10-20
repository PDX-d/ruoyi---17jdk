package cn.iocoder.yudao.module.edu.controller.admin.student.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 管理后台 - 学生变更记录分页 Request VO
 */
@Schema(description = "管理后台 - 学生变更记录分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class StudentChangeRecordPageReqVO extends PageParam {

    @Schema(description = "学生编号", example = "1")
    private Long studentId;

    @Schema(description = "审批状态（0草稿 1审批中 2通过 3驳回）", example = "1")
    private Integer approvalStatus;

    @Schema(description = "变更类型（1状态变更 2调班）", example = "1")
    private Integer changeType;

}
