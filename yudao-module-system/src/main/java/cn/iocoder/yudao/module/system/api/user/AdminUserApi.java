package cn.iocoder.yudao.module.system.api.user;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserCreateReqDTO;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Admin 用户 API 接口
 *
 * @author 芋道源码
 */
public interface AdminUserApi {

    /**
     * 创建用户
     *
     * 【说明，给后续开发同学】
     * 本方法是为「模块三 学生管理 - 创建学生时自动建号」新增的最小跨模块能力，
     * 避免 edu 模块直接依赖 system 模块的 Service / Controller VO。
     * 惯例：跨模块只允许通过 Api 接口调用，不得直接 import 其它模块的 Mapper / DO / Service。
     *
     * @param createReqDTO 用户创建信息
     * @return 用户编号
     */
    Long createUser(AdminUserCreateReqDTO createReqDTO);

    /**
     * 通过用户 ID 查询用户
     *
     * @param id 用户ID
     * @return 用户对象信息
     */
    AdminUserRespDTO getUser(Long id);

    /**
     * 通过手机号查询用户
     *
     * @param mobile 手机号
     * @return 用户对象信息
     */
    AdminUserRespDTO getUserByMobile(String mobile);

    /**
     * 通过用户 ID 查询用户下属
     *
     * @param id 用户编号
     * @return 用户下属用户列表
     */
    List<AdminUserRespDTO> getUserListBySubordinate(Long id);

    /**
     * 通过用户 ID 查询用户们
     *
     * @param ids 用户 ID 们
     * @return 用户对象信息
     */
    List<AdminUserRespDTO> getUserList(Collection<Long> ids);

    /**
     * 获得指定部门的用户数组
     *
     * @param deptIds 部门数组
     * @return 用户数组
     */
    List<AdminUserRespDTO> getUserListByDeptIds(Collection<Long> deptIds);

    /**
     * 获得指定岗位的用户数组
     *
     * @param postIds 岗位数组
     * @return 用户数组
     */
    List<AdminUserRespDTO> getUserListByPostIds(Collection<Long> postIds);

    /**
     * 根据昵称模糊搜索用户
     *
     * @param nickname 昵称关键词
     * @return 用户列表
     */
    List<AdminUserRespDTO> getUserListByNickname(String nickname);

    /**
     * 获得指定状态的用户列表
     *
     * @param status 用户状态
     * @return 用户列表
     */
    List<AdminUserRespDTO> getUserListByStatus(Integer status);

    /**
     * 获得用户 Map
     *
     * @param ids 用户编号数组
     * @return 用户 Map
     */
    default Map<Long, AdminUserRespDTO> getUserMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        List<AdminUserRespDTO> users = getUserList(ids);
        return CollectionUtils.convertMap(users, AdminUserRespDTO::getId);
    }

    /**
     * 校验用户是否有效。如下情况，视为无效：
     * 1. 用户编号不存在
     * 2. 用户被禁用
     *
     * @param id 用户编号
     * @return 有效的用户对象信息
     */
    AdminUserRespDTO validateUser(Long id);

    /**
     * 校验用户们是否有效。如下情况，视为无效：
     * 1. 用户编号不存在
     * 2. 用户被禁用
     *
     * @param ids 用户编号数组
     */
    void validateUserList(Collection<Long> ids);

}
