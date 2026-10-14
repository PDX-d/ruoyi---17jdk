package cn.iocoder.yudao.module.timetable.controller.admin.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.validation.constraints.*;

@Data
@EqualsAndHashCode(callSuper = true)
public class ChangePageReqVO extends PageParam {
    @Positive private Long lessonId;
    @Min(0) @Max(2) private Integer status;
}
