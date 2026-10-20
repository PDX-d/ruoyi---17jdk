package cn.iocoder.yudao.module.edu.service.student.listener;

import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEventListener;
import cn.iocoder.yudao.module.edu.service.student.StudentApprovalService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 学生学籍变更审批结果监听器：监听 BPM 流程状态事件，把审批结果回填到变更记录
 */
@Component
public class EduStudentChangeStatusListener extends BpmProcessInstanceStatusEventListener {

    @Resource
    private StudentApprovalService studentApprovalService;

    @Override
    protected String getProcessDefinitionKey() {
        return StudentApprovalService.PROCESS_DEFINITION_KEY;
    }

    @Override
    protected void onEvent(BpmProcessInstanceStatusEvent event) {
        // 事件回填线程可能没有登录 / 租户上下文，此处只按业务标识与状态处理
        studentApprovalService.handleApprovalResult(Long.valueOf(event.getBusinessKey()), event.getStatus());
    }

}
