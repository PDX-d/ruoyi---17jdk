package cn.iocoder.yudao.module.timetable.service;

import cn.iocoder.yudao.module.system.api.notify.NotifyMessageSendApi;
import cn.iocoder.yudao.module.system.api.notify.dto.NotifySendSingleToUserReqDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import cn.iocoder.yudao.module.timetable.api.reference.ReferenceData;
import cn.iocoder.yudao.module.timetable.controller.admin.vo.LessonRespVO;
import cn.iocoder.yudao.module.timetable.service.reference.TimetableReferences;
import cn.iocoder.yudao.module.timetable.support.TimetableException;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.*;

@Component
public class TimetableNotifier {
    @Resource private NotifyMessageSendApi messages;
    @Resource private AdminUserApi users;
    @Resource private TimetableReferences references;

    public void changed(LessonRespVO lesson, String type) {
        Set<Long> accounts = new LinkedHashSet<>();
        for (Long classId : lesson.getClassIds()) {
            List<ReferenceData.Student> students = references.get().getClassStudents(classId);
            TimetableException.require(students != null, "班级学生数据尚未接入，无法发送通知");
            for (ReferenceData.Student student : students) {
                TimetableException.require(student != null && student.getId() != null && student.getId() > 0
                        && student.getAccountId() != null && student.getAccountId() > 0,
                        "学生通知账号未显式绑定，禁止将学生 ID 当作账号 ID");
                accounts.add(student.getAccountId());
            }
        }
        users.validateUserList(accounts);
        Map<Long, AdminUserRespDTO> actualUsers = users.getUserMap(accounts);
        for (Long accountId : accounts) {
            AdminUserRespDTO user = actualUsers.get(accountId);
            TimetableException.require(user != null, "学生通知账号不存在");
            Map<String, Object> params = new HashMap<>();
            params.put("course", lesson.getCourseName());
            params.put("time", lesson.getStartTime() + " 至 " + lesson.getEndTime());
            params.put("classroom", lesson.getClassroomName());
            params.put("change", "CANCEL".equals(type) ? "停课" : "调课");
            NotifySendSingleToUserReqDTO message = new NotifySendSingleToUserReqDTO();
            message.setUserId(accountId);
            message.setTemplateCode("timetable_lesson_changed");
            message.setTemplateParams(params);
            TimetableException.require(messages.sendSingleMessageToAdmin(message) != null,
                    "课表站内信未入库，请检查通知模板是否启用");
        }
    }
}
