package cn.iocoder.yudao.module.edu.dal.dataobject.dict;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 专业 DO（表 dict_major）
 *
 * 为学生档案提供「专业」下拉数据源，支持按年级联动筛选。
 * TODO: 字典中心模块待接入（届时把本 DO / Mapper / 下拉接口切到字典中心并删除本表）。
 */
@TableName("dict_major")
@KeySequence("dict_major_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DictMajorDO extends TenantBaseDO {

    /**
     * 专业编号
     */
    @TableId
    private Long id;
    /**
     * 关联年级编号（为空表示不限年级的通用专业）
     */
    private Long gradeId;
    /**
     * 专业编码
     */
    private String code;
    /**
     * 专业名称
     */
    private String name;
    /**
     * 所属院系编号
     */
    private Long deptId;
    /**
     * 显示顺序
     */
    private Integer sort;
    /**
     * 状态（0启用 1停用）
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;

}
