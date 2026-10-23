package cn.iocoder.yudao.module.exam.service;

import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.exam.dal.mysql.ExamScheduleGuardMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import javax.annotation.Resource;
import static cn.iocoder.yudao.module.exam.support.ExamException.require;

@Component
public class ExamGuard {
    @Resource private ExamScheduleGuardMapper mapper;

    public void lock() {
        require(TransactionSynchronizationManager.isActualTransactionActive()
                && !TransactionSynchronizationManager.isCurrentTransactionReadOnly()
                && Integer.valueOf(TransactionDefinition.ISOLATION_READ_COMMITTED)
                .equals(TransactionSynchronizationManager.getCurrentTransactionIsolationLevel()),
                "考务写事务必须使用READ_COMMITTED");
        Long tenantId = TenantContextHolder.getRequiredTenantId();
        mapper.ensure(tenantId);
        require(tenantId.equals(mapper.lock(tenantId)), "无法取得租户排考锁");
    }
}
