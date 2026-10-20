package cn.iocoder.yudao.module.edu.enums.student;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 学生状态枚举
 *
 * 对应字典 edu_student_status
 */
@Getter
@AllArgsConstructor
public enum StudentStatusEnum {

    READING(0, "在读"),
    SUSPENDED(1, "休学"),
    QUIT(2, "退学"),
    GRADUATED(3, "毕业"),
    RECOVERED(4, "复学"),
    ;

    /**
     * 状态值
     */
    private final Integer status;
    /**
     * 状态名
     */
    private final String name;

}
