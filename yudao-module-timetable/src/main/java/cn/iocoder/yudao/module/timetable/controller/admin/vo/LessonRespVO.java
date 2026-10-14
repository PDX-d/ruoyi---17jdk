package cn.iocoder.yudao.module.timetable.controller.admin.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class LessonRespVO {
    private Long id;
    private Long semesterId;
    private Long courseId;
    private String courseName;
    private Long teacherId;
    private String teacherName;
    private Long classroomId;
    private String classroomName;
    private List<Long> classIds;
    private List<String> classNames;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    private Long version;
    private String color;
}
