package cn.iocoder.yudao.module.edu.dal.dataobject.counselor;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 辅导员带班 DO
 *
 * 辅导员与班级的关联关系：一个辅导员可带多个班级，一个班级只能有一个主辅导员
 *
 * @author 芋道源码
 */
@TableName("edu_counselor_class")
@KeySequence("edu_counselor_class_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CounselorClassDO extends TenantBaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 辅导员编号
     *
     * 关联 {@link CounselorDO#getId()}
     */
    private Long counselorId;
    /**
     * 班级名称
     */
    private String className;

}
