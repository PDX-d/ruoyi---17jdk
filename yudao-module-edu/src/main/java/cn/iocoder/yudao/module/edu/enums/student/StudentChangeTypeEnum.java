package cn.iocoder.yudao.module.edu.enums.student;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 学生变更类型枚举
 *
 * 对应字典 edu_student_change_type
 */
@Getter
@AllArgsConstructor
public enum StudentChangeTypeEnum {

    STATUS(1, "学籍状态变更"),
    CLASS(2, "调班"),
    RESUME(3, "复学"),
    ;

    /**
     * 类型值
     */
    private final Integer type;
    /**
     * 类型名
     */
    private final String name;

}
