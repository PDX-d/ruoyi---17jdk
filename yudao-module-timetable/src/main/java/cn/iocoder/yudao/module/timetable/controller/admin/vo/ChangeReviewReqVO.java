package cn.iocoder.yudao.module.timetable.controller.admin.vo;

import lombok.Data;
import javax.validation.constraints.*;

@Data
public class ChangeReviewReqVO {
    @NotNull @Positive private Long id;
    @NotBlank @Size(max = 1000) private String reason;
}
