package cn.iocoder.yudao.module.edu.dal.dataobject.semester;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDate;

/**
 * 学期校历/时间节点 DO
 *
 * 「时间节点」与「校历」合并为一张表，通过 type 区分开学日/注册截止/放假日/考试周/节假日/教学周/活动。
 */
@TableName("edu_semester_calendar")
@KeySequence("edu_semester_calendar_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SemesterCalendarDO extends TenantBaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 学期编号
     *
     * 关联 {@link SemesterDO#getId()}
     */
    private Long semesterId;
    /**
     * 节点名称
     */
    private String name;
    /**
     * 节点类型
     *
     * 1 - 开学日；2 - 注册截止；3 - 放假日；4 - 考试周；5 - 节假日；6 - 教学周；7 - 活动
     */
    private Integer type;
    /**
     * 开始日期
     */
    private LocalDate startDate;
    /**
     * 结束日期
     */
    private LocalDate endDate;
    /**
     * 教学周次
     */
    private Integer weekNo;
    /**
     * 展示颜色
     */
    private String color;
    /**
     * 备注
     */
    private String remark;

}
