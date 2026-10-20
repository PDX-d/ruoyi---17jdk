package cn.iocoder.yudao.module.edu.dal.dataobject.grade;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 年级 DO
 *
 * 仅保留年级名称、所属院系、入学年份、年级辅导员等最小字段，满足学生表单的年级下拉与展示。
 */
@TableName("edu_grade")
@KeySequence("edu_grade_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeDO extends TenantBaseDO {

    /**
     * 年级编号
     */
    @TableId
    private Long id;
    /**
     * 年级名称（如 2023级）
     */
    private String name;
    /**
     * 所属院系编号
     */
    private Long deptId;
    /**
     * 入学年份（如 2023）
     */
    private Integer enrollmentYear;
    /**
     * 年级辅导员编号（预留，暂不做校验与展示）
     */
    private Long counselorId;
    /**
     * 备注
     */
    private String remark;

}
