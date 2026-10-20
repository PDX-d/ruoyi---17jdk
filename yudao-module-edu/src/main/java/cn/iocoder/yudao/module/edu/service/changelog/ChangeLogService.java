package cn.iocoder.yudao.module.edu.service.changelog;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.edu.controller.admin.changelog.vo.ChangeLogPageReqVO;
import cn.iocoder.yudao.module.edu.dal.dataobject.changelog.ChangeLogDO;

import java.util.Collection;

/**
 * 变更日志 Service 接口
 *
 * 通用「档案变更历史」记录服务，各业务模块复用同一张表与同一套接口。
 */
public interface ChangeLogService {

    /**
     * 记录一条变更日志（操作人由本方法自动填充当前登录人，无需调用方传入）
     *
     * @param changeLog 变更日志
     */
    void createChangeLog(ChangeLogDO changeLog);

    /**
     * 批量记录变更日志（用于字段级 diff 一次写入多行）
     *
     * @param changeLogs 变更日志列表
     */
    void createChangeLogBatch(Collection<ChangeLogDO> changeLogs);

    /**
     * 获得变更日志分页
     *
     * @param pageReqVO 分页查询
     * @return 变更日志分页
     */
    PageResult<ChangeLogDO> getChangeLogPage(ChangeLogPageReqVO pageReqVO);

}
