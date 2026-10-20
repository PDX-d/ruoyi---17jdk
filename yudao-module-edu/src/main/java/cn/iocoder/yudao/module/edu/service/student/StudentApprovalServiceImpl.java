package cn.iocoder.yudao.module.edu.service.student;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.bpm.api.task.BpmProcessInstanceApi;
import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.StudentChangeApproveReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.StudentChangeRecordSaveReqVO;
import cn.iocoder.yudao.module.edu.dal.dataobject.student.StudentChangeRecordDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.student.StudentDO;
import cn.iocoder.yudao.module.edu.dal.mysql.grade.ClassMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.student.StudentChangeRecordMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.student.StudentMapper;
import cn.iocoder.yudao.module.edu.enums.changelog.ChangeLogActionEnum;
import cn.iocoder.yudao.module.edu.enums.student.ApprovalStatusEnum;
import cn.iocoder.yudao.module.edu.enums.student.StudentChangeTypeEnum;
import cn.iocoder.yudao.module.edu.enums.student.StudentStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_CHANGE_RECORD_ALREADY_HANDLED;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_CHANGE_RECORD_NOT_EXISTS;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_CHANGE_VALUE_INVALID;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_CLASS_NOT_EXISTS;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_NOT_EXISTS;

/**
 * 学生变更审批 Service 实现类
 *
 * 1. 提交变更申请：落库变更记录（审批中）后，事务提交时通过 BpmProcessInstanceApi 发起流程实例，
 *    businessKey = 变更记录编号，并回写 processInstanceId；
 * 2. 审批结束：BPM 状态事件回调 {@link #handleApprovalResult(Long, Integer)}，回填结果并在通过时落地变更；
 * 3. 降级：未启用 BPM 时发起流程失败，自动降级为轻量审批，由 approveChangeRecord 手工通过/驳回。
 */
@Slf4j
@Service
@Validated
public class StudentApprovalServiceImpl implements StudentApprovalService {

    @Resource
    private StudentChangeRecordMapper studentChangeRecordMapper;

    @Resource
    private StudentMapper studentMapper;

    @Resource
    private ClassMapper classMapper;

    @Resource
    private StudentChangeLogRecorder studentChangeLogRecorder;

    /**
     * BPM 流程实例 Api
     *
     * 用 {@link ObjectProvider} 注入：BPM 模块未启用时取不到 Bean，此时自动降级为轻量审批。
     */
    @Resource
    private ObjectProvider<BpmProcessInstanceApi> processInstanceApiProvider;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitChangeRecord(StudentChangeRecordSaveReqVO createReqVO) {
        // 1. 校验学生存在
        if (studentMapper.selectById(createReqVO.getStudentId()) == null) {
            throw exception(STUDENT_NOT_EXISTS);
        }
        // 2. 创建变更记录，审批状态置为「审批中」
        StudentChangeRecordDO record = BeanUtils.toBean(createReqVO, StudentChangeRecordDO.class);
        record.setApprovalStatus(ApprovalStatusEnum.PROCESSING.getStatus());
        studentChangeRecordMapper.insert(record);
        // 3. 事务提交后发起 BPM 流程实例
        //    放在 afterCommit：① 流程发起失败不应影响变更记录落库（可降级为轻量审批）；
        //    ② 避免流程发起抛异常把本事务标记为 rollback-only。
        Long recordId = record.getId();
        Long startUserId = SecurityFrameworkUtils.getLoginUserId();
        registerStartProcessAfterCommit(recordId, record, startUserId);
        return recordId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveChangeRecord(StudentChangeApproveReqVO approveReqVO) {
        // 1. 校验变更记录存在
        StudentChangeRecordDO record = studentChangeRecordMapper.selectById(approveReqVO.getId());
        if (record == null) {
            throw exception(STUDENT_CHANGE_RECORD_NOT_EXISTS);
        }
        // 2. 校验当前处于「审批中」，已处理的不可重复审批
        if (!Objects.equals(record.getApprovalStatus(), ApprovalStatusEnum.PROCESSING.getStatus())) {
            throw exception(STUDENT_CHANGE_RECORD_ALREADY_HANDLED);
        }
        // 3. 审批通过时，把变更落地到学生主表（含变更日志）
        if (Objects.equals(approveReqVO.getApprovalStatus(), ApprovalStatusEnum.APPROVED.getStatus())) {
            applyChangeToStudent(record);
        }
        // 4. 回写审批结果、审批人、审批时间、审批意见
        StudentChangeRecordDO updateObj = new StudentChangeRecordDO();
        updateObj.setId(record.getId());
        updateObj.setApprovalStatus(approveReqVO.getApprovalStatus());
        updateObj.setApproveRemark(approveReqVO.getApproveRemark());
        updateObj.setApproverId(SecurityFrameworkUtils.getLoginUserId());
        updateObj.setApproveTime(LocalDateTime.now());
        studentChangeRecordMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleApprovalResult(Long recordId, Integer bpmStatus) {
        // 1. 校验变更记录存在
        StudentChangeRecordDO record = studentChangeRecordMapper.selectById(recordId);
        if (record == null) {
            log.warn("[handleApprovalResult][变更记录({})不存在，忽略 BPM 结果回调]", recordId);
            return;
        }
        // 2. 仅「审批中」的记录需要处理：避免重复回调，或记录已被轻量审批处理过
        if (!Objects.equals(record.getApprovalStatus(), ApprovalStatusEnum.PROCESSING.getStatus())) {
            return;
        }
        // 3. 流程仍在审批中，无需处理
        if (Objects.equals(bpmStatus, BpmProcessInstanceStatusEnum.RUNNING.getStatus())) {
            return;
        }
        // 4. 审批通过时，把变更落地到学生主表（含变更日志）；驳回 / 取消只回写状态
        boolean approved = Objects.equals(bpmStatus, BpmProcessInstanceStatusEnum.APPROVE.getStatus());
        if (approved) {
            applyChangeToStudent(record);
        }
        StudentChangeRecordDO updateObj = new StudentChangeRecordDO();
        updateObj.setId(record.getId());
        updateObj.setApprovalStatus(approved ? ApprovalStatusEnum.APPROVED.getStatus()
                : ApprovalStatusEnum.REJECTED.getStatus());
        updateObj.setApproveTime(LocalDateTime.now());
        updateObj.setApproveRemark(approved ? "BPM 流程审批通过" : "BPM 流程审批不通过");
        studentChangeRecordMapper.updateById(updateObj);
    }

    /**
     * 注册「事务提交后发起 BPM 流程实例」的回调
     */
    private void registerStartProcessAfterCommit(Long recordId, StudentChangeRecordDO record, Long startUserId) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            startProcessInstance(recordId, record, startUserId);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

            @Override
            public void afterCommit() {
                startProcessInstance(recordId, record, startUserId);
            }

        });
    }

    /**
     * 发起 BPM 流程实例，并回写流程实例编号
     *
     * 失败（BPM 未启用 / 流程模型未发布 / 未配置审批人）时降级为轻量审批，不抛异常。
     */
    private void startProcessInstance(Long recordId, StudentChangeRecordDO record, Long startUserId) {
        BpmProcessInstanceApi processInstanceApi = processInstanceApiProvider.getIfAvailable();
        if (processInstanceApi == null || startUserId == null) {
            log.warn("[startProcessInstance][变更记录({})未发起 BPM 流程（BPM 未启用），降级为轻量审批]", recordId);
            return;
        }
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("studentId", record.getStudentId());
            variables.put("changeType", record.getChangeType());
            variables.put("fromValue", record.getFromValue());
            variables.put("toValue", record.getToValue());
            variables.put("reason", record.getReason());
            String processInstanceId = processInstanceApi.createProcessInstance(startUserId,
                    new BpmProcessInstanceCreateReqDTO().setProcessDefinitionKey(PROCESS_DEFINITION_KEY)
                            .setBusinessKey(String.valueOf(recordId)).setVariables(variables));
            StudentChangeRecordDO updateObj = new StudentChangeRecordDO();
            updateObj.setId(recordId);
            updateObj.setProcessInstanceId(processInstanceId);
            studentChangeRecordMapper.updateById(updateObj);
        } catch (Exception e) {
            log.warn("[startProcessInstance][变更记录({})发起 BPM 流程失败，降级为轻量审批：{}]", recordId, e.getMessage());
        }
    }

    /**
     * 将审批通过的变更落地到学生主表，并写入档案变更日志
     *
     * 1. 学籍状态变更（change_type=1）：把 student.status 改为 toValue；
     * 2. 复学（change_type=3）：把 student.status 置为「在读」；
     * 3. 调班（change_type=2）：把 student.class_id 改为 toValue。
     */
    private void applyChangeToStudent(StudentChangeRecordDO record) {
        StudentDO student = studentMapper.selectById(record.getStudentId());
        if (student == null) {
            throw exception(STUDENT_NOT_EXISTS);
        }
        // 留存变更前快照，供变更日志 diff（BeanUtils 深拷贝同类型对象）
        StudentDO before = BeanUtils.toBean(student, StudentDO.class);
        Integer changeType = record.getChangeType();
        if (Objects.equals(changeType, StudentChangeTypeEnum.STATUS.getType())) {
            student.setStatus(parseStatus(record.getToValue()));
        } else if (Objects.equals(changeType, StudentChangeTypeEnum.RESUME.getType())) {
            // 复学：学籍回到「在读」
            student.setStatus(StudentStatusEnum.READING.getStatus());
        } else if (Objects.equals(changeType, StudentChangeTypeEnum.CLASS.getType())) {
            // TODO 调班：后端逻辑保留、前端入口已隐藏。前端放开入口前无需改动此处
            Long classId = parseClassId(record.getToValue());
            if (classMapper.selectById(classId) == null) {
                throw exception(STUDENT_CLASS_NOT_EXISTS);
            }
            student.setClassId(classId);
        }
        studentMapper.updateById(student);
        // 写入档案变更日志（学籍状态变更 / 复学 / 调班）
        studentChangeLogRecorder.recordApprovedChange(before, student, resolveChangeLogAction(changeType));
    }

    /**
     * 变更类型 → 变更日志操作类型
     */
    private Integer resolveChangeLogAction(Integer changeType) {
        if (Objects.equals(changeType, StudentChangeTypeEnum.CLASS.getType())) {
            return ChangeLogActionEnum.CLASS_CHANGE.getAction();
        }
        if (Objects.equals(changeType, StudentChangeTypeEnum.RESUME.getType())) {
            return ChangeLogActionEnum.RESUME.getAction();
        }
        return ChangeLogActionEnum.STATUS_CHANGE.getAction();
    }

    /**
     * 解析目标学籍状态（非法值直接抛出业务异常，避免 NumberFormatException 暴露为 500）
     */
    private Integer parseStatus(String toValue) {
        try {
            return Integer.valueOf(toValue);
        } catch (NumberFormatException e) {
            throw exception(STUDENT_CHANGE_VALUE_INVALID);
        }
    }

    /**
     * 解析目标班级编号（非法值直接抛出业务异常）
     */
    private Long parseClassId(String toValue) {
        try {
            return Long.valueOf(toValue);
        } catch (NumberFormatException e) {
            throw exception(STUDENT_CHANGE_VALUE_INVALID);
        }
    }

}
