package cn.iocoder.yudao.module.exam.dal.dataobject;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exam_session")
public class ExamSessionDO extends TenantBaseDO {
    private Long id;
    private String name;
    private Long semesterId;
    private Long courseId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long venueId;
    private String venueName;
    private String venueAddress;
    private Integer venueCapacity;
    private BigDecimal venueLatitude;
    private BigDecimal venueLongitude;
    private BigDecimal radiusMeters;
    private Long sessionVersion;
    private Boolean scoreConfigLocked;
}

