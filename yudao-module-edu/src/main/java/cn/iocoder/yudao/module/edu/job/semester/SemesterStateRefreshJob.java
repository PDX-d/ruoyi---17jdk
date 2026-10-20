package cn.iocoder.yudao.module.edu.job.semester;

import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.tenant.core.service.TenantFrameworkService;
import cn.iocoder.yudao.module.edu.service.semester.SemesterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 学期状态自动刷新任务：每天 00:05 重算各租户学期的「状态」与「当前学期」标记
 *
 * 定时任务无租户上下文，需遍历租户逐个切换；不修改归档标记，避免覆盖人工纠偏。
 */
@Slf4j
@Component
public class SemesterStateRefreshJob {

    @Resource
    private SemesterService semesterService;

    @Resource
    private TenantFrameworkService tenantFrameworkService;

    /**
     * 每天 00:05 执行：重算所有租户下学期的状态与当前学期标记
     */
    @Scheduled(cron = "0 5 0 * * ?")
    public void execute() {
        List<Long> tenantIds = tenantFrameworkService.getTenantIds();
        log.info("[execute][学期状态自动刷新开始，租户数({})]", tenantIds.size());
        for (Long tenantId : tenantIds) {
            // 单个租户失败不影响其它租户
            try {
                TenantContextHolder.setTenantId(tenantId);
                semesterService.refreshSemesterStates();
            } catch (Exception e) {
                log.error("[execute][租户({}) 的学期状态刷新失败]", tenantId, e);
            } finally {
                TenantContextHolder.clear();
            }
        }
        log.info("[execute][学期状态自动刷新结束]");
    }

}
