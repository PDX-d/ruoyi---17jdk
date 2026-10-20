package cn.iocoder.yudao.module.edu.dal.dataobject.grade;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 班级 DO
 *
 * 仅保留年级、班级名称、所属院系、班主任等最小字段，满足学生表单的班级下拉与展示。
 */
@TableName("edu_class")
@KeySequence("edu_class_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassDO extends TenantBaseDO {

    /**
     * 班级编号
     */
    @TableId
    private Long id;
    /**
     * 年级编号
     *
     * 关联 {@link GradeDO#getId()}
     */
    private Long gradeId;
    /**
     * 班级名称
     */
    private String name;
    /**
     * 所属院系编号
     */
    private Long deptId;
    /**
     * 班主任（教师）编号（预留，暂不做校验与展示）
     */
    private Long headTeacherId;
    /**
     * 备注
     */
    private String remark;

}
