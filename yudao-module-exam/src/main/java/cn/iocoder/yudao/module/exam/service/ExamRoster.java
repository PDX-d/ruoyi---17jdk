package cn.iocoder.yudao.module.exam.service;

import cn.iocoder.yudao.module.exam.api.reference.ExamReferenceData.Student;
import cn.iocoder.yudao.module.exam.dal.dataobject.*;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;
import static cn.iocoder.yudao.module.exam.support.ExamException.require;

@Component
public class ExamRoster {
    @Resource private ExamStore store;
    @Resource private ExamReferences references;

    public void replace(Long sessionId, Collection<Long> classes, List<Student> students) {
        List<ExamParticipantDO> old = store.participants(sessionId);
        for (ExamParticipantDO participant : old) {
            store.participants.lock(ExamStore.tenant(), participant.getId());
        }
        if (!old.isEmpty()) {
            store.details.delete(ExamStore.<ExamScoreDetailDO>query().in("participant_id",
                    old.stream().map(ExamParticipantDO::getId).collect(Collectors.toList())));
        }
        store.participants.delete(ExamStore.<ExamParticipantDO>query().eq("session_id", sessionId));
        store.sessionClasses.delete(ExamStore.<ExamSessionClassDO>query().eq("session_id", sessionId));
        for (Long classId : new TreeSet<>(classes)) {
            ExamSessionClassDO link = new ExamSessionClassDO();
            link.setTenantId(ExamStore.tenant());
            link.setSessionId(sessionId);
            link.setClassId(classId);
            store.sessionClasses.insert(link);
        }
        Map<Long, String> names = references.catalog().getClasses();
        students.sort(Comparator.comparing(Student::getId));
        for (Student student : students) {
            require(names.get(student.getClassId()) != null, "公共班级名称缺失");
            ExamParticipantDO participant = new ExamParticipantDO();
            participant.setTenantId(ExamStore.tenant());
            participant.setSessionId(sessionId);
            participant.setStudentId(student.getId());
            participant.setStudentNo(student.getStudentNo());
            participant.setStudentName(student.getName());
            participant.setClassId(student.getClassId());
            participant.setClassName(names.get(student.getClassId()));
            participant.setRowVersion(0L);
            store.participants.insert(participant);
        }
    }

    public void delete(Long sessionId) {
        List<ExamParticipantDO> participants = store.participants(sessionId);
        for (ExamParticipantDO participant : participants) {
            store.participants.lock(ExamStore.tenant(), participant.getId());
        }
        if (!participants.isEmpty()) {
            store.details.delete(ExamStore.<ExamScoreDetailDO>query().in("participant_id",
                    participants.stream().map(ExamParticipantDO::getId).collect(Collectors.toList())));
        }
        store.items.delete(ExamStore.<ExamScoreItemDO>query().eq("session_id", sessionId));
        store.participants.delete(ExamStore.<ExamParticipantDO>query().eq("session_id", sessionId));
        store.sessionClasses.delete(ExamStore.<ExamSessionClassDO>query().eq("session_id", sessionId));
    }
}
