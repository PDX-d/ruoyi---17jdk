package cn.iocoder.yudao.module.timetable.controller.admin.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ConflictRespVO {
    private String type;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long lessonId;
    private Long teacherId;
    private Long classroomId;
    private Long classId;
}
