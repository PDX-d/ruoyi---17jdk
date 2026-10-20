package cn.iocoder.yudao.module.system.api.permission;

import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.system.api.permission.dto.RoleRespDTO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 角色 API 接口
 *
 * @author 芋道源码
 */
public interface RoleApi {

    /**
     * 校验角色们是否有效。如下情况，视为无效：
     * 1. 角色编号不存在
     * 2. 角色被禁用
     *
     * @param ids 角色编号数组
     */
    void validRoleList(Collection<Long> ids);

    /**
     * 获得角色信息
     *
     * @param id 角色编号
     * @return 角色信息
     */
    RoleRespDTO getRole(Long id);

    /**
     * 按角色编码获得角色编号
     *
     * 【说明，给后续开发同学】
     * 本方法是模块三「学生自动建号后分配基础角色」所需的最小跨模块能力：
     * edu 模块只知道角色编码（如 edu_student），需要拿到角色编号才能分配。
     *
     * @param code 角色编码
     * @return 角色编号；不存在时返回 null
     */
    Long getRoleIdByCode(String code);

    /**
     * 获得角色信息数组
     *
     * @param ids 角色编号数组
     * @return 角色信息数组
     */
    List<RoleRespDTO> getRoleList(Collection<Long> ids);

    /**
     * 获得指定编号的角色 Map
     *
     * @param ids 角色编号数组
     * @return 角色 Map
     */
    default Map<Long, RoleRespDTO> getRoleMap(Collection<Long> ids) {
        List<RoleRespDTO> list = getRoleList(ids);
        return CollectionUtils.convertMap(list, RoleRespDTO::getId);
    }

}
