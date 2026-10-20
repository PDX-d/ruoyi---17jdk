package cn.iocoder.yudao.module.system.api.permission;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.system.api.permission.dto.RoleRespDTO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.RoleDO;
import cn.iocoder.yudao.module.system.service.permission.RoleService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

/**
 * 角色 API 实现类
 *
 * @author 芋道源码
 */
@Service
public class RoleApiImpl implements RoleApi {

    @Resource
    private RoleService roleService;

    @Override
    public void validRoleList(Collection<Long> ids) {
        roleService.validateRoleList(ids);
    }

    @Override
    public RoleRespDTO getRole(Long id) {
        RoleDO role = roleService.getRole(id);
        return BeanUtils.toBean(role, RoleRespDTO.class);
    }

    @Override
    public Long getRoleIdByCode(String code) {
        // 角色数量有限，直接遍历；如需高频调用可后续在 RoleService 增加按编码查询
        return roleService.getRoleList().stream()
                .filter(role -> role.getCode() != null && role.getCode().equals(code))
                .map(RoleDO::getId)
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<RoleRespDTO> getRoleList(Collection<Long> ids) {
        List<RoleDO> list = roleService.getRoleList(ids);
        return BeanUtils.toBean(list, RoleRespDTO.class);
    }
}
