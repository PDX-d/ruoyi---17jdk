package cn.iocoder.yudao.module.edu.dal.dataobject.student;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 学生变更记录 DO
 *
 * approval_status 语义对齐 BPM（0草稿 1审批中 2通过 3驳回）；审批逻辑收敛在 StudentApprovalService，
 * 本表已预留 process_instance_id，接入 BPM 时无需大改表结构。
 */
@TableName("edu_student_change_record")
@KeySequence("edu_student_change_record_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentChangeRecordDO extends TenantBaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 学生编号
     *
     * 关联 {@link StudentDO#getId()}
     */
    private Long studentId;
    /**
     * 变更类型（1状态变更 2调班）
     */
    private Integer changeType;
    /**
     * 变更前值（状态字典值或班级编号）
     */
    private String fromValue;
    /**
     * 变更后值（状态字典值或班级编号）
     */
    private String toValue;
    /**
     * 变更原因
     */
    private String reason;
    /**
     * 审批状态（0草稿 1审批中 2通过 3驳回）
     */
    private Integer approvalStatus;
    /**
     * BPM 流程实例编号
     */
    private String processInstanceId;
    /**
     * 审批人编号
     */
    private Long approverId;
    /**
     * 审批时间
     */
    private LocalDateTime approveTime;
    /**
     * 审批意见
     */
    private String approveRemark;

}
