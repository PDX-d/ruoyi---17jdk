package cn.iocoder.yudao.module.system.api.dept;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.dal.dataobject.dept.DeptDO;
import cn.iocoder.yudao.module.system.service.dept.DeptService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 部门 API 实现类
 *
 * @author 芋道源码
 */
@Service
public class DeptApiImpl implements DeptApi {

    @Resource
    private DeptService deptService;

    @Override
    public DeptRespDTO getDept(Long id) {
        DeptDO dept = deptService.getDept(id);
        return BeanUtils.toBean(dept, DeptRespDTO.class);
    }

    @Override
    public List<DeptRespDTO> getDeptList(Collection<Long> ids) {
        List<DeptDO> depts = deptService.getDeptList(ids);
        return BeanUtils.toBean(depts, DeptRespDTO.class);
    }

    @Override
    public List<DeptRespDTO> getDeptListByName(Collection<String> names) {
        List<DeptDO> depts = deptService.getDeptListByName(names);
        return BeanUtils.toBean(depts, DeptRespDTO.class);
    }

    @Override
    public List<DeptRespDTO> getRootDeptList() {
        // 注意：getChildDeptList 返回的是「根部门下的全部子孙部门」，因此这里再按 parentId 过滤出「直属根部门的部门」，
        // 即真正的顶级部门（学院）。
        List<DeptDO> allDepts = deptService.getChildDeptList(
                Collections.singleton(DeptDO.PARENT_ID_ROOT));
        List<DeptDO> rootDepts = allDepts.stream()
                .filter(dept -> DeptDO.PARENT_ID_ROOT.equals(dept.getParentId()))
                .collect(Collectors.toList());
        return BeanUtils.toBean(rootDepts, DeptRespDTO.class);
    }

    @Override
    public void validateDeptList(Collection<Long> ids) {
        deptService.validateDeptList(ids);
    }

    @Override
    public List<DeptRespDTO> getChildDeptList(Collection<Long> ids) {
        List<DeptDO> childDepts = deptService.getChildDeptList(ids);
        return BeanUtils.toBean(childDepts, DeptRespDTO.class);
    }

    @Override
    public List<DeptRespDTO> getParentDeptList(Long id) {
        List<DeptDO> parentDepts = deptService.getParentDeptList(id);
        return BeanUtils.toBean(parentDepts, DeptRespDTO.class);
    }

}
