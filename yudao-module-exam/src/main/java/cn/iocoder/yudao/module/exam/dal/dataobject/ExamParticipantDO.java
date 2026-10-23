package cn.iocoder.yudao.module.exam.dal.dataobject;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exam_participant")
public class ExamParticipantDO extends TenantBaseDO {
    private Long id;
    private Long sessionId;
    private Long studentId;
    private String studentNo;
    private String studentName;
    private Long classId;
    private String className;
    private LocalDateTime signTime;
    private BigDecimal signLatitude;
    private BigDecimal signLongitude;
    private BigDecimal signAccuracy;
    private LocalDateTime positionTime;
    private BigDecimal finalScore;
    private Long rowVersion;
}

