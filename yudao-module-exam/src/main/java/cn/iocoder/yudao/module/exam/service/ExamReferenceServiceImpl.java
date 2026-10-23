package cn.iocoder.yudao.module.exam.service;

import cn.iocoder.yudao.module.exam.api.reference.ExamReferenceData.*;
import cn.iocoder.yudao.module.exam.controller.admin.vo.ExamVO.*;
import cn.iocoder.yudao.module.exam.dal.dataobject.ExamSessionDO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;
import static cn.iocoder.yudao.module.exam.support.ExamException.require;

@Service
public class ExamReferenceServiceImpl implements ExamReferenceService {
    @Resource private ExamAccess access;
    @Resource private ExamReferences references;

    @Override
    public ReferenceOptionsResp options() {
        ExamAccess.Scope scope = access.staff("exam:reference:query");
        Catalog catalog = references.catalog();
        Map<Long, Set<Long>> courseClasses = new LinkedHashMap<>();
        Set<Long> semesterIds = new HashSet<>();
        for (Semester semester : catalog.getSemesters()) {
            for (Course course : catalog.getCourses()) {
                require(course.getClassIds() != null, "公共课程班级信息缺失");
                ExamSessionDO target = target(semester.getId(), course.getId());
                for (Long classId : course.getClassIds()) {
                    if (scope.canRead(target, classId)) {
                        courseClasses.computeIfAbsent(course.getId(), id -> new LinkedHashSet<>()).add(classId);
                        semesterIds.add(semester.getId());
                    }
                }
            }
        }
        Set<Long> classIds = courseClasses.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
        List<SemesterOption> semesters = catalog.getSemesters().stream()
                .filter(row -> scope.isAdmin() || semesterIds.contains(row.getId())).map(row -> {
                    SemesterOption option = new SemesterOption();
                    BeanUtils.copyProperties(row, option);
                    return option;
                }).collect(Collectors.toList());
        List<CourseOption> courses = catalog.getCourses().stream().filter(row -> courseClasses.containsKey(row.getId()))
                .map(row -> {
                    CourseOption option = new CourseOption();
                    option.setId(row.getId());
                    option.setName(row.getName());
                    option.setClassIds(new ArrayList<>(courseClasses.get(row.getId())));
                    return option;
                }).collect(Collectors.toList());
        List<Option> classes = catalog.getClasses().entrySet().stream().filter(row -> classIds.contains(row.getKey()))
                .sorted(Map.Entry.comparingByKey()).map(row -> {
                    Option option = new Option();
                    option.setId(row.getKey());
                    option.setName(row.getValue());
                    return option;
                }).collect(Collectors.toList());
        ReferenceOptionsResp result = new ReferenceOptionsResp();
        result.setMock(catalog.isMock());
        result.setCurrentSemesterId(semesterIds.contains(catalog.getCurrentSemesterId()) || scope.isAdmin()
                ? catalog.getCurrentSemesterId() : null);
        result.setSemesters(semesters);
        result.setCourses(courses);
        result.setClasses(classes);
        return result;
    }

    @Override
    public List<StudentResp> students(Long semesterId, Long courseId, Long classId) {
        ExamAccess.Scope scope = access.staff("exam:reference:query");
        ExamRules.id(classId);
        Catalog catalog = references.catalog();
        references.semester(catalog, semesterId);
        require(catalog.getCourses().stream().anyMatch(course -> Objects.equals(courseId, course.getId())
                && course.getClassIds() != null && course.getClassIds().contains(classId)), "课程班级关系不存在");
        require(scope.canRead(target(semesterId, courseId), classId), "班级不在可访问范围");
        return references.students(Collections.singleton(classId)).stream().map(row -> {
            StudentResp result = new StudentResp();
            BeanUtils.copyProperties(row, result);
            return result;
        }).collect(Collectors.toList());
    }

    private ExamSessionDO target(Long semesterId, Long courseId) {
        ExamSessionDO session = new ExamSessionDO();
        session.setSemesterId(semesterId);
        session.setCourseId(courseId);
        return session;
    }
}
