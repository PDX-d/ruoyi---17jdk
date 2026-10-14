package cn.iocoder.yudao.module.timetable.dal.dataobject;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@TableName("timetable_lesson")
@Data
@EqualsAndHashCode(callSuper = true)
public class LessonDO extends TenantBaseDO {
    private Long id;
    private Long semesterId;
    private Long courseId;
    private Long teacherId;
    private Long classroomId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    private Long version;
}
