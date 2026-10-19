package cn.iocoder.yudao.module.timetable.dal.dataobject;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@TableName("timetable_change_request")
@Data
@EqualsAndHashCode(callSuper = true)
public class ChangeRequestDO extends TenantBaseDO {
    private Long id;
    private Long lessonId;
    private Long originalVersion;
    private LocalDateTime originalStartTime;
    private LocalDateTime originalEndTime;
    private Long originalClassroomId;
    private String type;
    private LocalDateTime targetStartTime;
    private LocalDateTime targetEndTime;
    private Long targetClassroomId;
    private String reason;
    private Long applicantId;
    private Integer status;
    private Long reviewerId;
    private LocalDateTime reviewTime;
    private String reviewReason;
}
