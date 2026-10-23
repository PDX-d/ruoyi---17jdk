package cn.iocoder.yudao.module.exam.dal.dataobject;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exam_score_detail")
public class ExamScoreDetailDO extends TenantBaseDO {
    private Long id;
    private Long participantId;
    private Long itemId;
    private BigDecimal score;
}

