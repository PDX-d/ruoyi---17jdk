package cn.iocoder.yudao.module.exam.dal.dataobject;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exam_venue")
public class ExamVenueDO extends TenantBaseDO {
    private Long id;
    private String name;
    private String address;
    private Integer capacity;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal radiusMeters;
    private Integer status;
    private Long version;
}

