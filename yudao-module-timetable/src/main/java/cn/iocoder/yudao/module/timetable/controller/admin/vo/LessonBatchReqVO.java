package cn.iocoder.yudao.module.timetable.controller.admin.vo;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import javax.validation.constraints.*;

@Data
public class LessonBatchReqVO {
    @NotNull @Positive private Long semesterId;
    @NotNull @Positive private Long courseId;
    @NotNull @Positive private Long teacherId;
    @NotNull @Positive private Long classroomId;
    @NotEmpty @Size(max = 100) private List<@NotNull @Positive Long> classIds;
    @NotNull private LocalDate startDate;
    @NotNull private LocalDate endDate;
    @NotEmpty @Size(max = 7) private Set<@NotNull @Min(1) @Max(7) Integer> weekdays;
    @NotNull private LocalTime startTime;
    @NotNull private LocalTime endTime;
}
