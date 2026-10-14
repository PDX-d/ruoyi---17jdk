package cn.iocoder.yudao.module.timetable.controller.admin.vo;

import lombok.Data;
import javax.validation.constraints.*;

@Data
public class ClassroomSaveReqVO {
    @Positive
    private Long id;
    @NotBlank @Size(max = 64)
    private String code;
    @NotBlank @Size(max = 128)
    private String name;
    @NotBlank @Size(max = 255)
    private String location;
    @NotNull @Min(1)
    private Integer capacity;
    @NotNull @Min(0) @Max(1)
    private Integer status;
}
