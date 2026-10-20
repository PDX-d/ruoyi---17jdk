package cn.iocoder.yudao.module.edu.dal.dataobject.student;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 学生-标签关联 DO
 *
 * 关联使用「物理删除」维护（先物理删后批量插），以规避逻辑删除在唯一键上的重复冲突。
 */
@TableName("edu_student_tag_rel")
@KeySequence("edu_student_tag_rel_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentTagRelDO extends TenantBaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 学生编号
     *
     * 关联 {@link StudentDO#getId()}
     */
    private Long studentId;
    /**
     * 标签编号
     *
     * 关联 {@link StudentTagDO#getId()}
     */
    private Long tagId;

}
