package cn.iocoder.yudao.module.edu.enums.changelog;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 变更日志操作类型枚举
 *
 * 对应字典 edu_change_log_action
 */
@Getter
@AllArgsConstructor
public enum ChangeLogActionEnum {

    CREATE(1, "新增"),
    UPDATE(2, "修改"),
    DELETE(3, "删除"),
    STATUS_CHANGE(4, "学籍状态变更"),
    CLASS_CHANGE(5, "调班"),
    RESUME(6, "复学"),
    IMPORT(7, "导入"),
    ;

    /**
     * 操作类型值
     */
    private final Integer action;
    /**
     * 操作类型名
     */
    private final String name;

}
