package cn.iocoder.yudao.module.timetable.service;

import cn.iocoder.yudao.module.timetable.api.reference.ReferenceData;
import cn.iocoder.yudao.module.timetable.api.reference.TimetableReferenceGateway;
import cn.iocoder.yudao.module.timetable.controller.admin.vo.LessonSaveReqVO;
import cn.iocoder.yudao.module.timetable.dal.dataobject.ClassroomDO;
import cn.iocoder.yudao.module.timetable.service.reference.TimetableReferences;
import cn.iocoder.yudao.module.timetable.support.TimetableException;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;

@Component
public class LessonRules {
    @Resource private TimetableReferences references;
    @Resource private ClassroomService classrooms;

    public ReferenceData.Semester semester(Long id, boolean writable) {
        ReferenceData.Semester semester = id == null ? references.get().getCurrentSemester()
                : references.get().getSemester(id);
        TimetableException.require(semester != null && semester.getId() != null
                && semester.getId() > 0 && (id == null || id.equals(semester.getId()))
                && semester.getStartDate() != null && semester.getEndDate() != null
                && !semester.getEndDate().isBefore(semester.getStartDate()), "学期不存在或日期配置不完整");
        if (writable) {
            TimetableException.require(!semester.isArchived(), "归档学期只读");
        }
        return semester;
    }

    public void range(ReferenceData.Semester semester, LocalDate start, LocalDate end) {
        TimetableException.require(start != null && end != null && !end.isBefore(start), "请提供有效的明确日期范围");
        TimetableException.require(!start.isBefore(semester.getStartDate())
                && !end.isAfter(semester.getEndDate()), "日期范围必须位于指定学期内");
    }

    public void validate(LessonSaveReqVO request) {
        ReferenceData.Semester semester = semester(request.getSemesterId(), true);
        LocalDateTime start = request.getStartTime();
        LocalDateTime end = request.getEndTime();
        TimetableException.require(start != null && end != null && start.isBefore(end), "课次开始时间必须早于结束时间");
        TimetableException.require(start.getNano() % 1000 == 0 && end.getNano() % 1000 == 0, "课次时间最多保留微秒精度");
        TimetableException.require(!start.isBefore(semester.getStartDate().atStartOfDay())
                && !end.isAfter(semester.getEndDate().plusDays(1).atStartOfDay()), "课次时间必须位于学期范围内");
        TimetableException.require(request.getClassIds() != null && !request.getClassIds().isEmpty()
                && request.getClassIds().size() <= 100
                && request.getClassIds().stream().allMatch(id -> id != null && id > 0)
                && new HashSet<>(request.getClassIds()).size() == request.getClassIds().size(), "班级列表不能为空、重复或包含无效 ID");
        TimetableReferenceGateway gateway = references.get();
        ReferenceData.Course course = gateway.getCourse(request.getCourseId());
        TimetableException.require(course != null && request.getCourseId().equals(course.getId())
                && course.getTeacherIds() != null && course.getClassIds() != null,
                "课程不存在或未配置教师、班级关联");
        TimetableException.require(gateway.getTeacherName(request.getTeacherId()) != null
                && course.getTeacherIds().contains(request.getTeacherId()), "教师不存在或不属于课程允许教师");
        TimetableException.require(course.getClassIds().containsAll(request.getClassIds())
                && request.getClassIds().stream().allMatch(id -> gateway.getClassName(id) != null), "班级不存在或不属于课程允许班级");
        ClassroomDO classroom = classrooms.find(request.getClassroomId());
        TimetableException.require(classroom.getStatus() == 0, "教室已停用，不能用于新排课");
    }
}
