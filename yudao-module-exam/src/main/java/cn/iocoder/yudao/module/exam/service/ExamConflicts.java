package cn.iocoder.yudao.module.exam.service;

import cn.iocoder.yudao.module.exam.dal.dataobject.*;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;
import static cn.iocoder.yudao.module.exam.support.ExamException.require;
import static cn.iocoder.yudao.module.exam.enums.ErrorCodeConstants.EXAM_CONFLICT;

@Component
public class ExamConflicts {
    @Resource private ExamStore store;

    public void check(ExamSessionDO session, Collection<Long> classIds, Collection<Long> studentIds) {
        require(session.getStartTime() != null && session.getEndTime() != null
                && session.getStartTime().isBefore(session.getEndTime()), "考试时间无效");
        require(session.getVenueCapacity() != null && studentIds.size() <= session.getVenueCapacity(), "考场容量不足");
        List<ExamSessionDO> overlapping = store.sessions.selectList(ExamStore.<ExamSessionDO>query()
                .ne(session.getId() != null, "id", session.getId())
                .lt("start_time", session.getEndTime()).gt("end_time", session.getStartTime()));
        if (overlapping.isEmpty()) {
            return;
        }
        require(overlapping.stream().noneMatch(row -> Objects.equals(row.getVenueId(), session.getVenueId())),
                EXAM_CONFLICT, "考场与其他考试时间冲突");
        List<Long> sessionIds = overlapping.stream().map(ExamSessionDO::getId).collect(Collectors.toList());
        require(store.sessionClasses.selectCount(ExamStore.<ExamSessionClassDO>query()
                .in("session_id", sessionIds).in("class_id", classIds)) == 0, EXAM_CONFLICT, "班级与其他考试时间冲突");
        if (!studentIds.isEmpty()) {
            require(store.participants.selectCount(ExamStore.<ExamParticipantDO>query()
                    .in("session_id", sessionIds).in("student_id", studentIds)) == 0,
                    EXAM_CONFLICT, "学生与其他考试时间冲突");
        }
    }
}
