package cn.iocoder.yudao.module.edu.service.student;

import cn.iocoder.yudao.module.edu.controller.admin.student.vo.StudentChangeApproveReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.StudentChangeRecordSaveReqVO;

/**
 * 学生变更审批 Service 接口
 *
 * 学生变更（学籍状态变更 / 复学 / 调班）按 yudao 标准工作流（BPM）处理：
 * 提交时发起流程实例，审批结束后由 BPM 状态事件回调 handleApprovalResult 回填结果；
 * 未启用 BPM 时自动降级为「轻量审批」（approveChangeRecord 手工通过/驳回）。
 */
public interface StudentApprovalService {

    /**
     * 学生学籍变更审批的流程定义 Key
     *
     * 需在「BPM 后台 → 流程模型」创建同名模型并发布。
     */
    String PROCESS_DEFINITION_KEY = "edu-student-change";

    /**
     * 提交学生变更申请
     *
     * 创建变更记录（审批状态置为「审批中」），并在事务提交后发起 BPM 流程实例。
     *
     * @param createReqVO 变更申请信息
     * @return 变更记录编号
     */
    Long submitChangeRecord(StudentChangeRecordSaveReqVO createReqVO);

    /**
     * 审批学生变更申请（轻量审批 / 降级路径）
     *
     * 审批通过时，将变更落地到学生主表（状态变更改 status、复学改回在读、调班改 class_id）。
     *
     * @param approveReqVO 审批信息
     */
    void approveChangeRecord(StudentChangeApproveReqVO approveReqVO);

    /**
     * 处理 BPM 流程实例的审批结果（由 {@code EduStudentChangeStatusListener} 回调）
     *
     * @param recordId 变更记录编号（流程的业务标识）
     * @param bpmStatus BPM 流程实例状态（见 {@code BpmProcessInstanceStatusEnum}：1审批中 2通过 3不通过 4已取消）
     */
    void handleApprovalResult(Long recordId, Integer bpmStatus);

}
