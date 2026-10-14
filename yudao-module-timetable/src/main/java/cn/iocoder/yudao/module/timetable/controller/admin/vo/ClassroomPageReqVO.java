package cn.iocoder.yudao.module.timetable.controller.admin.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.validation.constraints.*;

@Data
@EqualsAndHashCode(callSuper = true)
public class ClassroomPageReqVO extends PageParam {
    @Size(max = 128)
    private String name;
    @Min(0) @Max(1)
    private Integer status;
}
