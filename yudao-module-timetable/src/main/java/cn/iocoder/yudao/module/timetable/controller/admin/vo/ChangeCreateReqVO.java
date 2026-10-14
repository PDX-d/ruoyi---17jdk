package cn.iocoder.yudao.module.timetable.controller.admin.vo;

import lombok.Data;
import java.time.LocalDateTime;
import javax.validation.constraints.*;

@Data
public class ChangeCreateReqVO {
    @NotNull @Positive private Long lessonId;
    @NotBlank @Pattern(regexp = "CHANGE|CANCEL") private String type;
    @NotBlank @Size(max = 1000) private String reason;
    private LocalDateTime targetStartTime;
    private LocalDateTime targetEndTime;
    @Positive private Long targetClassroomId;
}
