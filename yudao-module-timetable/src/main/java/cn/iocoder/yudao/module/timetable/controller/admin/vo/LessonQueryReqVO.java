package cn.iocoder.yudao.module.timetable.controller.admin.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import javax.validation.constraints.*;

@Data
@EqualsAndHashCode(callSuper = true)
public class LessonQueryReqVO extends PageParam {
    @Positive private Long semesterId;
    @Pattern(regexp = "SELF|CLASS|CLASSROOM")
    private String dimension = "SELF";
    @Positive private Long targetId;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;
    @Min(0) @Max(1) private Integer status;
    @Pattern(regexp = "png|pdf")
    private String format = "pdf";
}
