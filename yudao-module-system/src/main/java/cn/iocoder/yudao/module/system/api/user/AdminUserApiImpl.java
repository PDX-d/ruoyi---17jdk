package cn.iocoder.yudao.module.system.api.user;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.datapermission.core.annotation.DataPermission;
import cn.iocoder.yudao.framework.datapermission.core.util.DataPermissionUtils;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserCreateReqDTO;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import cn.iocoder.yudao.module.system.controller.admin.user.vo.user.UserSaveReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.dept.DeptDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.service.dept.DeptService;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

/**
 * Admin 用户 API 实现类
 *
 * @author 芋道源码
 */
@Service
public class AdminUserApiImpl implements AdminUserApi {

    @Resource
    private AdminUserService userService;
    @Resource
    private DeptService deptService;

    @Override
    public Long createUser(AdminUserCreateReqDTO createReqDTO) {
        // 【说明】将跨模块 DTO 转换为 system 模块内部 UserSaveReqVO 后，复用 AdminUserService.createUser，
        // 从而复用其「账号唯一校验 / 密码加密 / 账号数量限制 / 操作日志」等既有逻辑。
        // 注意：UserSaveReqVO 对 username 有 4-30 位数字/字母、password 有 4-16 位的校验，
        // 若后续学生学号长度超出范围（例如超过 16 位），需要在此处或上游做适配。
        UserSaveReqVO reqVO = new UserSaveReqVO();
        reqVO.setUsername(createReqDTO.getUsername());
        reqVO.setNickname(createReqDTO.getNickname());
        reqVO.setPassword(createReqDTO.getPassword());
        reqVO.setDeptId(createReqDTO.getDeptId());
        reqVO.setMobile(createReqDTO.getMobile());
        reqVO.setEmail(createReqDTO.getEmail());
        reqVO.setSex(createReqDTO.getSex());
        reqVO.setRemark(createReqDTO.getRemark());
        return userService.createUser(reqVO);
    }

    @Override
    @DataPermission(enable = false) // 忽略数据权限，避免因为过滤，导致无法查询用户。类似：https://github.com/YunaiV/ruoyi-vue-pro/issues/1051
    public AdminUserRespDTO getUser(Long id) {
        AdminUserDO user = userService.getUser(id);
        return BeanUtils.toBean(user, AdminUserRespDTO.class);
    }

    @Override
    @DataPermission(enable = false) // 忽略数据权限，指定手机号的 API 查询用于跨模块数据拼接
    public AdminUserRespDTO getUserByMobile(String mobile) {
        AdminUserDO user = userService.getUserByMobile(mobile);
        return BeanUtils.toBean(user, AdminUserRespDTO.class);
    }

    @Override
    public List<AdminUserRespDTO> getUserListBySubordinate(Long id) {
        // 1.1 获取用户负责的部门
        List<DeptDO> depts = deptService.getDeptListByLeaderUserId(id);
        if (CollUtil.isEmpty(depts)) {
            return Collections.emptyList();
        }
        // 1.2 获取所有子部门
        Set<Long> deptIds = convertSet(depts, DeptDO::getId);
        List<DeptDO> childDeptList = deptService.getChildDeptList(deptIds);
        if (CollUtil.isNotEmpty(childDeptList)) {
            deptIds.addAll(convertSet(childDeptList, DeptDO::getId));
        }

        // 2. 获取部门对应的用户信息
        List<AdminUserDO> users = userService.getUserListByDeptIds(deptIds);
        users.removeIf(item -> ObjUtil.equal(item.getId(), id)); // 排除自己
        return BeanUtils.toBean(users, AdminUserRespDTO.class);
    }

    @Override
    public List<AdminUserRespDTO> getUserList(Collection<Long> ids) {
        return DataPermissionUtils.executeIgnore(() -> { // 禁用数据权限。原因是，一般基于指定 id 的 API 查询，都是数据拼接为主
            List<AdminUserDO> users = userService.getUserList(ids);
            return BeanUtils.toBean(users, AdminUserRespDTO.class);
        });
    }

    @Override
    public List<AdminUserRespDTO> getUserListByDeptIds(Collection<Long> deptIds) {
        List<AdminUserDO> users = userService.getUserListByDeptIds(deptIds);
        return BeanUtils.toBean(users, AdminUserRespDTO.class);
    }

    @Override
    public List<AdminUserRespDTO> getUserListByPostIds(Collection<Long> postIds) {
        List<AdminUserDO> users = userService.getUserListByPostIds(postIds);
        return BeanUtils.toBean(users, AdminUserRespDTO.class);
    }

    @Override
    public List<AdminUserRespDTO> getUserListByNickname(String nickname) {
        List<AdminUserDO> users = userService.getUserListByNickname(nickname);
        return BeanUtils.toBean(users, AdminUserRespDTO.class);
    }

    @Override
    public List<AdminUserRespDTO> getUserListByStatus(Integer status) {
        List<AdminUserDO> users = userService.getUserListByStatus(status);
        return BeanUtils.toBean(users, AdminUserRespDTO.class);
    }

    @Override
    public AdminUserRespDTO validateUser(Long id) {
        AdminUserDO user = userService.validateUser(id);
        return BeanUtils.toBean(user, AdminUserRespDTO.class);
    }

    @Override
    public void validateUserList(Collection<Long> ids) {
        userService.validateUserList(ids);
    }

}
