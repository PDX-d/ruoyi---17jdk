package cn.iocoder.yudao.module.timetable.service;

import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.timetable.dal.mysql.ScheduleGuardMapper;
import cn.iocoder.yudao.module.timetable.support.TimetableException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.TransactionDefinition;
import javax.annotation.Resource;

@Component
public class ScheduleGuard {
    @Resource private ScheduleGuardMapper mapper;

    public void lock() {
        TimetableException.require(TransactionSynchronizationManager.isActualTransactionActive(), "排课操作必须在事务中执行");
        TimetableException.require(Integer.valueOf(TransactionDefinition.ISOLATION_READ_COMMITTED)
                .equals(TransactionSynchronizationManager.getCurrentTransactionIsolationLevel())
                && !TransactionSynchronizationManager.isCurrentTransactionReadOnly(),
                "排课写事务必须使用 READ_COMMITTED；禁止加入旧快照或只读事务");
        Long tenantId = TenantContextHolder.getRequiredTenantId();
        mapper.ensure(tenantId);
        TimetableException.require(tenantId.equals(mapper.lock(tenantId)), "无法取得租户排课锁");
    }
}
