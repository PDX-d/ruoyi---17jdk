package cn.iocoder.yudao.module.timetable.dal.dataobject;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@TableName("timetable_lesson_class")
@Data
@EqualsAndHashCode(callSuper = true)
public class LessonClassDO extends TenantBaseDO {
    private Long id;
    private Long lessonId;
    private Long classId;
}
