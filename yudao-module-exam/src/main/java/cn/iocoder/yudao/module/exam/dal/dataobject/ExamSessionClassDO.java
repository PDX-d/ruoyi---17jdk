package cn.iocoder.yudao.module.exam.dal.dataobject;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exam_session_class")
public class ExamSessionClassDO extends TenantBaseDO {
    private Long id;
    private Long sessionId;
    private Long classId;
}

