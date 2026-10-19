package cn.iocoder.yudao.module.timetable.controller.admin.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChangeRespVO {
    private Long id;
    private Long lessonId;
    private Long originalVersion;
    private LocalDateTime originalStartTime;
    private LocalDateTime originalEndTime;
    private Long originalClassroomId;
    private String type;
    private LocalDateTime targetStartTime;
    private LocalDateTime targetEndTime;
    private Long targetClassroomId;
    private String reason;
    private Long applicantId;
    private Integer status;
    private Long reviewerId;
    private LocalDateTime reviewTime;
    private String reviewReason;
    private LocalDateTime createTime;
}
