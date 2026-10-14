package cn.iocoder.yudao.module.timetable.service;

import cn.iocoder.yudao.module.timetable.api.reference.ReferenceData;
import cn.iocoder.yudao.module.timetable.api.reference.TimetableReferenceGateway;
import cn.iocoder.yudao.module.timetable.controller.admin.vo.LessonRespVO;
import cn.iocoder.yudao.module.timetable.dal.dataobject.LessonDO;
import cn.iocoder.yudao.module.timetable.dal.dataobject.ClassroomDO;
import cn.iocoder.yudao.module.timetable.service.reference.TimetableReferences;
import cn.iocoder.yudao.module.timetable.support.TimetableException;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class LessonViews {
    @Resource private TimetableReferences references;
    @Resource private LessonRelations relations;
    @Resource private ClassroomService classrooms;

    public LessonRespVO response(LessonDO lesson) {
        return responses(Collections.singletonList(lesson)).get(0);
    }

    public List<LessonRespVO> responses(List<LessonDO> lessons) {
        if (lessons.isEmpty()) {
            return Collections.emptyList();
        }
        TimetableReferenceGateway gateway = references.get();
        Map<Long, ClassroomDO> classroomMap = classrooms.findMap(lessons.stream()
                .map(LessonDO::getClassroomId).collect(Collectors.toList()));
        Map<Long, List<Long>> classIdsMap = relations.getClassIdsMap(lessons.stream()
                .map(LessonDO::getId).collect(Collectors.toList()));
        Map<Long, ReferenceData.Course> courses = new HashMap<>();
        Map<Long, String> teachers = new HashMap<>();
        Map<Long, String> classNames = new HashMap<>();
        List<LessonRespVO> results = new ArrayList<>(lessons.size());
        for (LessonDO lesson : lessons) {
            ReferenceData.Course course = courses.computeIfAbsent(lesson.getCourseId(), gateway::getCourse);
            TimetableException.require(course != null, "课次课程公共数据缺失");
            LessonRespVO result = new LessonRespVO();
            BeanUtils.copyProperties(lesson, result);
            result.setCourseName(course.getName());
            result.setTeacherName(teachers.computeIfAbsent(lesson.getTeacherId(), gateway::getTeacherName));
            ClassroomDO classroom = classroomMap.get(lesson.getClassroomId());
            TimetableException.require(classroom != null, "教室不存在");
            result.setClassroomName(classroom.getName());
            result.setClassIds(classIdsMap.getOrDefault(lesson.getId(), Collections.emptyList()));
            result.setClassNames(result.getClassIds().stream()
                    .map(id -> classNames.computeIfAbsent(id, gateway::getClassName)).collect(Collectors.toList()));
            TimetableException.require(result.getTeacherName() != null
                    && result.getClassNames().stream().allMatch(java.util.Objects::nonNull), "教师或班级公共数据缺失");
            float hue = Math.floorMod(Long.hashCode(lesson.getCourseId()) * 137, 360) / 360F;
            result.setColor(String.format("#%06X", Color.HSBtoRGB(hue, 0.5F, 0.82F) & 0xFFFFFF));
            results.add(result);
        }
        return results;
    }
}
