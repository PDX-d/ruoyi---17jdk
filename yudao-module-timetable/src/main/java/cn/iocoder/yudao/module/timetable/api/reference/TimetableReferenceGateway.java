package cn.iocoder.yudao.module.timetable.api.reference;

import java.util.List;

public interface TimetableReferenceGateway {
    default ReferenceData.Catalog getCatalog() {
        return null;
    }

    ReferenceData.Semester getSemester(Long id);
    ReferenceData.Semester getCurrentSemester();
    ReferenceData.Course getCourse(Long id);
    String getTeacherName(Long id);
    String getClassName(Long id);
    ReferenceData.Actor getActor(Long accountId);
    List<ReferenceData.Student> getClassStudents(Long classId);
}
