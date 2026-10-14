package cn.iocoder.yudao.module.timetable.controller.admin.vo;

import lombok.Data;

@Data
public class ClassroomRespVO {
    private Long id;
    private String code;
    private String name;
    private String location;
    private Integer capacity;
    private Integer status;
}
