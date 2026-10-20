package cn.iocoder.yudao.module.system.api.dept;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 部门 API 接口
 *
 * @author 芋道源码
 */
public interface DeptApi {

    /**
     * 获得部门信息
     *
     * @param id 部门编号
     * @return 部门信息
     */
    DeptRespDTO getDept(Long id);

    /**
     * 获得部门信息数组
     *
     * @param ids 部门编号数组
     * @return 部门信息数组
     */
    List<DeptRespDTO> getDeptList(Collection<Long> ids);

    /**
     * 按部门名称批量查询部门信息（供外部模块「名称 → 编号」反查使用，如学生 Excel 导入）
     *
     * @param names 部门名称数组
     * @return 部门信息数组
     */
    List<DeptRespDTO> getDeptListByName(Collection<String> names);

    /**
     * 按部门名称批量查询部门信息 Map（key 为部门名称）
     *
     * 若存在同名部门，仅保留其中一个（Excel 导入场景按名称反查编号，重名时以第一个为准）。
     *
     * @param names 部门名称数组
     * @return 部门 Map，key 为部门名称
     */
    default Map<String, DeptRespDTO> getDeptMapByName(Collection<String> names) {
        if (CollUtil.isEmpty(names)) {
            return Collections.emptyMap();
        }
        List<DeptRespDTO> list = getDeptListByName(names);
        return CollectionUtils.convertMap(list, DeptRespDTO::getName, dto -> dto);
    }

    /**
     * 获得顶级部门列表（parentId 为根部门的部门）
     *
     * 用于「学院名称」等场景：本系统为单学院部署，顶级部门即学院。
     *
     * @return 顶级部门列表
     */
    List<DeptRespDTO> getRootDeptList();

    /**
     * 校验部门们是否有效。如下情况，视为无效：
     * 1. 部门编号不存在
     * 2. 部门被禁用
     *
     * @param ids 角色编号数组
     */
    void validateDeptList(Collection<Long> ids);

    /**
     * 获得指定编号的部门 Map
     *
     * @param ids 部门编号数组
     * @return 部门 Map
     */
    default Map<Long, DeptRespDTO> getDeptMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        List<DeptRespDTO> list = getDeptList(ids);
        return CollectionUtils.convertMap(list, DeptRespDTO::getId);
    }

    /**
     * 获得指定部门的所有子部门
     *
     * @param id 部门编号
     * @return 子部门列表
     */
    default List<DeptRespDTO> getChildDeptList(Long id) {
        return getChildDeptList(Collections.singleton(id));
    }

    /**
     * 获得指定部门的所有子部门
     *
     * @param ids 部门编号数组
     * @return 子部门列表
     */
    List<DeptRespDTO> getChildDeptList(Collection<Long> ids);

    /**
     * 获得指定部门的所有父部门
     *
     * 按直属父部门到根部门的顺序返回，不包含指定部门自身。
     *
     * @param id 部门编号
     * @return 父部门列表
     */
    List<DeptRespDTO> getParentDeptList(Long id);

}
