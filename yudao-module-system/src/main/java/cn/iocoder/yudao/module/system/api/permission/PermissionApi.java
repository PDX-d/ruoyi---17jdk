package cn.iocoder.yudao.module.system.api.permission;

import cn.iocoder.yudao.framework.common.biz.system.permission.PermissionCommonApi;

import java.util.Collection;
import java.util.Set;

/**
 * 权限 API 接口
 *
 * @author 芋道源码
 */
public interface PermissionApi extends PermissionCommonApi {

    /**
     * 获得拥有多个角色的用户编号集合
     *
     * @param roleIds 角色编号集合
     * @return 用户编号集合
     */
    Set<Long> getUserRoleIdListByRoleIds(Collection<Long> roleIds);

    /**
     * 给用户分配角色
     *
     * 【说明，给后续开发同学】
     * 本方法是模块三「学生自动建号后分配基础角色」所需的最小跨模块能力：
     * 建号成功后，为账号分配 edu_student 角色（传 roleIds 集合，覆盖式分配）。
     *
     * @param userId  用户编号
     * @param roleIds 角色编号集合
     */
    void assignUserRole(Long userId, Collection<Long> roleIds);

}
