package cn.iocoder.yudao.module.edu.dal.dataobject.teacher;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 教师 DO
 *
 * @author 芋道源码
 */
@TableName("edu_teacher")
@KeySequence("edu_teacher_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherDO extends TenantBaseDO {

    /**
     * 教师编号
     */
    @TableId
    private Long id;
    /**
     * 工号
     */
    private String jobNo;
    /**
     * 姓名
     */
    private String name;
    /**
     * 性别
     */
    private Integer gender;
    /**
     * 手机号
     */
    private String mobile;
    /**
     * 职称
     */
    private String title;
    /**
     * 学历
     */
    private String education;
    /**
     * 所属院系编号
     */
    private Long deptId;
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
