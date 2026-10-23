package cn.iocoder.yudao.module.exam.service;

import cn.iocoder.yudao.module.exam.api.reference.ExamReferenceData.*;
import cn.iocoder.yudao.module.exam.api.reference.ExamReferenceGateway;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import static cn.iocoder.yudao.module.exam.enums.ErrorCodeConstants.EXAM_REFERENCE_UNAVAILABLE;

@Component
public class ExamReferences {
    @Resource private ObjectProvider<ExamReferenceGateway> gateways;

    private static void require(boolean value, String message) {
        cn.iocoder.yudao.module.exam.support.ExamException.require(value, EXAM_REFERENCE_UNAVAILABLE, message);
    }

    public ExamReferenceGateway gateway() {
        ExamRules.timezone();
        ExamReferenceGateway gateway = gateways.getIfAvailable();
        require(gateway != null, "考务公共数据适配器未配置");
        return gateway;
    }

    public Catalog catalog() {
        Catalog catalog = gateway().snapshot();
        require(catalog != null && catalog.getSemesters() != null && catalog.getCourses() != null
                && catalog.getClasses() != null && catalog.getTeachingPairs() != null, "公共数据不完整");
        require(catalog.getSemesters().stream().allMatch(row -> row != null && positive(row.getId())
                && row.getName() != null), "公共学期数据不完整");
        require(catalog.getCourses().stream().allMatch(row -> row != null && positive(row.getId())
                && row.getName() != null && row.getClassIds() != null
                && row.getClassIds().stream().allMatch(ExamReferences::positive)), "公共课程数据不完整");
        require(catalog.getClasses().entrySet().stream().allMatch(row -> positive(row.getKey()) && row.getValue() != null),
                "公共班级数据不完整");
        require(catalog.getTeachingPairs().stream().allMatch(row -> row != null && positive(row.getSemesterId())
                && positive(row.getTeacherId()) && positive(row.getCourseId()) && positive(row.getClassId())),
                "精准授课关系数据不完整");
        return catalog;
    }

    private static boolean positive(Long id) {
        return id != null && id > 0;
    }

    public Semester semester(Catalog catalog, Long id) {
        ExamRules.id(id);
        return catalog.getSemesters().stream().filter(Objects::nonNull)
                .filter(row -> id.equals(row.getId())).findFirst()
                .orElseThrow(() -> cn.iocoder.yudao.module.exam.support.ExamException.error("学期不存在"));
    }

    public void writable(Long semesterId) {
        cn.iocoder.yudao.module.exam.support.ExamException.require(!semester(catalog(), semesterId).isArchived(),
                cn.iocoder.yudao.module.exam.enums.ErrorCodeConstants.EXAM_FORBIDDEN, "归档学期禁止写入");
    }

    public void timeRange(Long semesterId, LocalDateTime start, LocalDateTime end) {
        Semester semester = semester(catalog(), semesterId);
        require(semester.getStartDate() != null && semester.getEndDate() != null
                && !semester.getStartDate().isAfter(semester.getEndDate()), "公共学期起止日期缺失或无效");
        cn.iocoder.yudao.module.exam.support.ExamException.require(start != null && end != null
                && start.isBefore(end), "考试时间无效");
        cn.iocoder.yudao.module.exam.support.ExamException.require(
                !start.isBefore(semester.getStartDate().atStartOfDay())
                        && !end.isAfter(semester.getEndDate().plusDays(1).atStartOfDay()),
                "考试时间必须位于所选学期内");
    }

    public void selection(Long semesterId, Long courseId, Collection<Long> classIds) {
        Catalog catalog = catalog();
        cn.iocoder.yudao.module.exam.support.ExamException.require(!semester(catalog, semesterId).isArchived(),
                cn.iocoder.yudao.module.exam.enums.ErrorCodeConstants.EXAM_FORBIDDEN, "归档学期禁止写入");
        Course course = catalog.getCourses().stream().filter(Objects::nonNull)
                .filter(row -> Objects.equals(courseId, row.getId())).findFirst()
                .orElseThrow(() -> cn.iocoder.yudao.module.exam.support.ExamException.error("课程不存在"));
        require(classIds != null && !classIds.isEmpty() && course.getClassIds() != null
                && new HashSet<>(classIds).size() == classIds.size(), "考试班级无效或重复");
        for (Long classId : classIds) {
            ExamRules.id(classId);
            require(catalog.getClasses().containsKey(classId) && course.getClassIds().contains(classId),
                    "班级不存在或不属于课程");
        }
    }

    public List<Student> students(Collection<Long> classIds) {
        List<Student> result = new ArrayList<>();
        Set<Long> ids = new HashSet<>();
        for (Long classId : new TreeSet<>(classIds)) {
            List<Student> students = gateway().students(classId);
            require(students != null, "公共学生名册不可用");
            for (Student student : students) {
                require(student != null && student.getId() != null && student.getId() > 0
                        && classId.equals(student.getClassId()) && student.getName() != null
                        && !student.getName().trim().isEmpty() && student.getStudentNo() != null
                        && !student.getStudentNo().trim().isEmpty(), "公共学生姓名、学号或班级信息不完整");
                require(ids.add(student.getId()), "公共名册存在重复学生");
                result.add(student);
            }
        }
        return result;
    }
}
