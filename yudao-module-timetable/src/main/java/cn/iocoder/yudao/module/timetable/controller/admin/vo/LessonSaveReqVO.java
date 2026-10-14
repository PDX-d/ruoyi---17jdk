package cn.iocoder.yudao.module.timetable.controller.admin.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import javax.validation.constraints.*;

@Data
public class LessonSaveReqVO {
    @Positive
    private Long id;
    @Positive
    private Long version;
    @NotNull @Positive
    private Long semesterId;
    @NotNull @Positive
    private Long courseId;
    @NotNull @Positive
    private Long teacherId;
    @NotNull @Positive
    private Long classroomId;
    @NotEmpty @Size(max = 100)
    private List<@NotNull @Positive Long> classIds;
    @NotNull
    private LocalDateTime startTime;
    @NotNull
    private LocalDateTime endTime;
}
