package cn.iocoder.yudao.module.edu.service.changelog;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.edu.controller.admin.changelog.vo.ChangeLogPageReqVO;
import cn.iocoder.yudao.module.edu.dal.dataobject.changelog.ChangeLogDO;
import cn.iocoder.yudao.module.edu.dal.mysql.changelog.ChangeLogMapper;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * 变更日志 Service 实现类
 *
 * 操作人信息统一在此填充：取当前登录人编号，再通过 system 模块 AdminUserApi 反查昵称；
 * 登录人为空（如 BPM 事件回调线程）时不填操作人。
 */
@Service
@Validated
public class ChangeLogServiceImpl implements ChangeLogService {

    @Resource
    private ChangeLogMapper changeLogMapper;

    @Resource
    private AdminUserApi adminUserApi;

    @Override
    public void createChangeLog(ChangeLogDO changeLog) {
        fillOperator(changeLog);
        changeLogMapper.insert(changeLog);
    }

    @Override
    public void createChangeLogBatch(Collection<ChangeLogDO> changeLogs) {
        if (CollUtil.isEmpty(changeLogs)) {
            return;
        }
        List<ChangeLogDO> list = new ArrayList<>(changeLogs);
        // 批量场景下操作人相同，一次性反查避免重复调用
        AdminUserRespDTO operator = getLoginUser();
        list.forEach(changeLog -> {
            if (changeLog.getOperatorId() == null && operator != null) {
                changeLog.setOperatorId(operator.getId());
                changeLog.setOperatorName(operator.getNickname());
            }
        });
        changeLogMapper.insertBatch(list);
    }

    @Override
    public PageResult<ChangeLogDO> getChangeLogPage(ChangeLogPageReqVO pageReqVO) {
        return changeLogMapper.selectPage(pageReqVO);
    }

    /**
     * 填充操作人（仅当调用方未显式指定、且当前存在登录人时）
     */
    private void fillOperator(ChangeLogDO changeLog) {
        if (changeLog.getOperatorId() != null) {
            return;
        }
        AdminUserRespDTO operator = getLoginUser();
        if (operator != null) {
            changeLog.setOperatorId(operator.getId());
            changeLog.setOperatorName(operator.getNickname());
        }
    }

    /**
     * 获得当前登录用户（未登录返回 null，例如 BPM 事件回调线程）
     */
    private AdminUserRespDTO getLoginUser() {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        if (userId == null) {
            return null;
        }
        AdminUserRespDTO user = adminUserApi.getUser(userId);
        return Objects.nonNull(user) ? user : null;
    }

}
