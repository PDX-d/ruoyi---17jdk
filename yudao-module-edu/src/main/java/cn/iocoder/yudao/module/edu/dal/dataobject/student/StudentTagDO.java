package cn.iocoder.yudao.module.edu.dal.dataobject.student;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 学生标签 DO
 */
@TableName("edu_student_tag")
@KeySequence("edu_student_tag_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentTagDO extends TenantBaseDO {

    /**
     * 标签编号
     */
    @TableId
    private Long id;
    /**
     * 标签名称
     */
    private String name;
    /**
     * 标签颜色（用于前端展示）
     */
    private String color;
    /**
     * 显示顺序
     */
    private Integer sort;
    /**
     * 状态
     *
     * 0 - 开启；1 - 关闭
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;

}
