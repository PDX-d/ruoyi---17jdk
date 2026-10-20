package cn.iocoder.yudao.module.edu.enums.student;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 审批状态枚举
 *
 * 语义对齐 BPM：0草稿 1审批中 2通过 3驳回
 */
@Getter
@AllArgsConstructor
public enum ApprovalStatusEnum {

    DRAFT(0, "草稿"),
    PROCESSING(1, "审批中"),
    APPROVED(2, "通过"),
    REJECTED(3, "驳回"),
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
