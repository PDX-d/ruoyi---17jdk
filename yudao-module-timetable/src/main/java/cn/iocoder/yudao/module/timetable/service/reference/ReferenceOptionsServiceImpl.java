package cn.iocoder.yudao.module.timetable.service.reference;

import cn.iocoder.yudao.module.timetable.api.reference.ReferenceData;
import cn.iocoder.yudao.module.timetable.controller.admin.vo.ReferenceOptionsRespVO;
import cn.iocoder.yudao.module.timetable.controller.admin.vo.ReferenceOptionsRespVO.CourseOption;
import cn.iocoder.yudao.module.timetable.controller.admin.vo.ReferenceOptionsRespVO.NamedOption;
import cn.iocoder.yudao.module.timetable.controller.admin.vo.ReferenceOptionsRespVO.SemesterOption;
import cn.iocoder.yudao.module.timetable.service.TimetableAccess;
import cn.iocoder.yudao.module.timetable.support.TimetableException;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReferenceOptionsServiceImpl implements ReferenceOptionsService {
    @Resource private TimetableReferences references;
    @Resource private TimetableAccess access;

    @Override
    public ReferenceOptionsRespVO getOptions() {
        access.permission("timetable:lesson:query");
        ReferenceData.Catalog catalog = references.get().getCatalog();
        TimetableException.require(catalog != null, "课程表公共列表尚未接入，请配置真实选项适配器或显式开启本地模拟模式");
        ReferenceData.Actor actor = new ReferenceData.Actor();
        boolean admin = access.isAdmin();
        if (admin) {
            actor.setType("ADMIN");
        } else {
            BeanUtils.copyProperties(access.actor(), actor);
        }
        List<CourseOption> courses = courses(catalog, actor, admin);
        Set<Long> teacherIds = admin ? catalog.getTeachers().keySet() : courses.stream()
                .flatMap(course -> course.getTeacherIds().stream()).collect(Collectors.toSet());
        if ("TEACHER".equals(actor.getType())) {
            teacherIds = Collections.singleton(actor.getTeacherId());
        }
        Set<Long> classIds = admin || "TEACHER".equals(actor.getType()) ? catalog.getClasses().keySet() : actor.getClassIds();
        if ("TEACHER".equals(actor.getType())) {
            classIds = courses.stream().flatMap(course -> course.getClassIds().stream()).collect(Collectors.toSet());
        }
        ReferenceOptionsRespVO response = new ReferenceOptionsRespVO();
        response.setMock(catalog.isMock());
        response.setCurrentSemesterId(catalog.getCurrentSemesterId());
        response.setActor(actor);
        response.setSemesters(semesters(catalog));
        response.setCourses(courses);
        response.setTeachers(names(catalog.getTeachers(), teacherIds));
        response.setClasses(names(catalog.getClasses(), classIds));
        return response;
    }

    private List<SemesterOption> semesters(ReferenceData.Catalog catalog) {
        List<SemesterOption> semesters = catalog.getSemesters().stream().map(semester -> {
            TimetableException.require(semester.getId() != null && semester.getId() > 0 && semester.getName() != null
                    && semester.getStartDate() != null && semester.getEndDate() != null
                    && !semester.getEndDate().isBefore(semester.getStartDate()), "公共学期列表配置不完整");
            SemesterOption option = new SemesterOption();
            BeanUtils.copyProperties(semester, option);
            return option;
        }).sorted(Comparator.comparing(SemesterOption::getId)).collect(Collectors.toList());
        TimetableException.require(semesters.stream().anyMatch(semester -> Objects.equals(semester.getId(), catalog.getCurrentSemesterId())),
                "当前学期未包含在公共学期列表中");
        return semesters;
    }

    private List<CourseOption> courses(ReferenceData.Catalog catalog, ReferenceData.Actor actor, boolean admin) {
        List<CourseOption> options = new ArrayList<>();
        for (ReferenceData.Course course : catalog.getCourses()) {
            TimetableException.require(course.getId() != null && course.getId() > 0 && course.getName() != null
                    && course.getTeacherIds() != null && course.getClassIds() != null
                    && catalog.getTeachers().keySet().containsAll(course.getTeacherIds())
                    && catalog.getClasses().keySet().containsAll(course.getClassIds()), "公共课程列表或教师班级关联配置不完整");
            boolean teacher = "TEACHER".equals(actor.getType());
            if (!admin && (teacher ? !course.getTeacherIds().contains(actor.getTeacherId())
                    : Collections.disjoint(course.getClassIds(), actor.getClassIds()))) {
                continue;
            }
            CourseOption option = new CourseOption();
            option.setId(course.getId());
            option.setName(course.getName());
            option.setTeacherIds(course.getTeacherIds().stream().filter(id -> !teacher || id.equals(actor.getTeacherId()))
                    .collect(Collectors.toCollection(TreeSet::new)));
            option.setClassIds(course.getClassIds().stream().filter(id -> admin || teacher || actor.getClassIds().contains(id))
                    .collect(Collectors.toCollection(TreeSet::new)));
            options.add(option);
        }
        options.sort(Comparator.comparing(CourseOption::getId));
        return options;
    }

    private List<NamedOption> names(Map<Long, String> names, Set<Long> allowedIds) {
        TimetableException.require(names.keySet().containsAll(allowedIds), "本人身份关联的教师或班级不在公共列表中");
        TimetableException.require(allowedIds.stream().allMatch(id -> id != null && id > 0 && names.get(id) != null),
                "公共教师或班级选项配置不完整");
        return allowedIds.stream().sorted().map(id -> {
            NamedOption option = new NamedOption();
            option.setId(id);
            option.setName(names.get(id));
            return option;
        }).collect(Collectors.toList());
    }
}
