package cn.iocoder.yudao.module.edu.dal.dataobject.semester;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDate;

/**
 * 学期 DO
 *
 * 1. 学期以「学年 + 学期」维度唯一（见 edu_semester 的唯一键）。
 * 2. isCurrent 标记「当前学期」，业务上最多一条为 true；使用包装类型 Boolean 避免 null 判断歧义。
 */
@TableName("edu_semester")
@KeySequence("edu_semester_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SemesterDO extends TenantBaseDO {

    /**
     * 学期编号
     */
    @TableId
    private Long id;
    /**
     * 学期名称（如 能工智人学院2025-2026学年第一学期）
     *
     * 由后端按「学院名称 + 学年 + 学期」自动拼装，不接收前端传值。
     */
    private String name;
    /**
     * 学年（如 2025-2026）
     *
     * 由「学年起始年份 + 结束年份」自动拼装（结束年份 = 起始年份 + 1）。
     */
    private String schoolYear;
    /**
     * 学年起始年份（非持久化字段，由 schoolYear 解析得到，供表单双年份下拉回显）
     */
    @TableField(exist = false)
    private Integer schoolYearStart;
    /**
     * 学年结束年份（非持久化字段，由 schoolYear 解析得到，供表单双年份下拉回显）
     */
    @TableField(exist = false)
    private Integer schoolYearEnd;
    /**
     * 学期
     *
     * 枚举 {@link cn.iocoder.yudao.module.edu.enums.semester.SemesterTermEnum}
     * 1 - 第一学期；2 - 第二学期
     */
    private Integer term;
    /**
     * 开始日期
     */
    private LocalDate startDate;
    /**
     * 结束日期
     */
    private LocalDate endDate;
    /**
     * 是否当前学期
     *
     * 业务上最多只允许一条记录为 true，由 Service 层保证互斥
     */
    private Boolean isCurrent;
    /**
     * 状态
     *
     * 枚举 {@link cn.iocoder.yudao.module.edu.enums.semester.SemesterStatusEnum}
     * 0 - 未开始；1 - 进行中；2 - 已结束
     */
    private Integer status;
    /**
     * 是否归档
     */
    private Boolean archived;
    /**
     * 备注
     */
    private String remark;

}
