package cn.iocoder.yudao.module.edu.dal.dataobject.changelog;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 变更日志 DO（档案变更历史）
 *
 * 1. 通用表：以 bizModule 区分业务模块，各模块复用本表；
 * 2. 记录粒度：新增 / 删除 / 导入等事件写一行摘要；字段级修改每个变更字段写一行；
 * 3. beforeValue / afterValue 存「文字」而非编号（由字典 / 各 Mapper 反查名称）；
 * 4. 本表只追加、不更新、不物理删除。
 */
@TableName("edu_change_log")
@KeySequence("edu_change_log_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeLogDO extends TenantBaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 业务模块（如：学生管理）
     */
    private String bizModule;
    /**
     * 业务主键（如：学生编号）
     */
    private Long bizId;
    /**
     * 业务编号（如：学号），用于检索
     */
    private String bizNo;
    /**
     * 业务名称（如：学生姓名），用于展示
     */
    private String bizName;
    /**
     * 操作类型（字典 edu_change_log_action）
     *
     * 见 {@link cn.iocoder.yudao.module.edu.enums.changelog.ChangeLogActionEnum}
     */
    private Integer action;
    /**
     * 变更字段（英文名，非字段级记录时为空）
     */
    private String fieldName;
    /**
     * 变更字段中文名
     */
    private String fieldLabel;
    /**
     * 变更前值（文字）
     */
    private String beforeValue;
    /**
     * 变更后值（文字）
     */
    private String afterValue;
    /**
     * 操作人编号
     */
    private Long operatorId;
    /**
     * 操作人姓名
     */
    private String operatorName;
    /**
     * 备注 / 摘要
     */
    private String remark;

}
