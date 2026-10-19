package cn.iocoder.yudao.module.timetable.dal.dataobject;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@TableName("timetable_classroom")
@Data
@EqualsAndHashCode(callSuper = true)
public class ClassroomDO extends TenantBaseDO {
    private Long id;
    private String code;
    private String name;
    private String location;
    private Integer capacity;
    private Integer status;
}
