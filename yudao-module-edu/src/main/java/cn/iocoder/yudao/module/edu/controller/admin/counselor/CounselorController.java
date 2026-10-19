package cn.iocoder.yudao.module.edu.controller.admin.counselor;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.edu.controller.admin.counselor.vo.CounselorPageReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.counselor.vo.CounselorRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.counselor.vo.CounselorSaveReqVO;
import cn.iocoder.yudao.module.edu.dal.dataobject.counselor.CounselorDO;
import cn.iocoder.yudao.module.edu.service.counselor.CounselorService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 辅导员管理")
@RestController
@RequestMapping("/edu/counselor")
@Validated
public class CounselorController {

    @Resource
    private CounselorService counselorService;

    @Resource
    private DeptApi deptApi;

    @PostMapping("/create")
    @Operation(summary = "创建辅导员")
    @PreAuthorize("@ss.hasPermission('edu:counselor:create')")
    public CommonResult<Long> createCounselor(@Valid @RequestBody CounselorSaveReqVO createReqVO) {
        return success(counselorService.createCounselor(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新辅导员")
    @PreAuthorize("@ss.hasPermission('edu:counselor:update')")
    public CommonResult<Boolean> updateCounselor(@Valid @RequestBody CounselorSaveReqVO updateReqVO) {
        counselorService.updateCounselor(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除辅导员")
    @Parameter(name = "id", description = "辅导员编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('edu:counselor:delete')")
    public CommonResult<Boolean> deleteCounselor(@RequestParam("id") Long id) {
        counselorService.deleteCounselor(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得辅导员")
    @Parameter(name = "id", description = "辅导员编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('edu:counselor:query')")
    public CommonResult<CounselorRespVO> getCounselor(@RequestParam("id") Long id) {
        CounselorDO counselor = counselorService.getCounselor(id);
        if (counselor == null) {
            return success(null);
        }
        return success(BeanUtils.toBean(counselor, CounselorRespVO.class, this::fillDeptAndClasses));
    }

    @GetMapping("/page")
    @Operation(summary = "获得辅导员分页")
    @PreAuthorize("@ss.hasPermission('edu:counselor:query')")
    public CommonResult<PageResult<CounselorRespVO>> getCounselorPage(@Valid CounselorPageReqVO pageReqVO) {
        PageResult<CounselorDO> pageResult = counselorService.getCounselorPage(pageReqVO);
        return success(buildRespVOPage(pageResult));
    }

    private PageResult<CounselorRespVO> buildRespVOPage(PageResult<CounselorDO> pageResult) {
        if (CollUtil.isEmpty(pageResult.getList())) {
            return BeanUtils.toBean(pageResult, CounselorRespVO.class);
        }
        // 1. 批量查询院系名称、所带班级
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(
                CollectionUtils.convertSet(pageResult.getList(), CounselorDO::getDeptId));
        Map<Long, List<String>> classNamesMap = counselorService.getCounselorClassNamesMap(
                CollectionUtils.convertSet(pageResult.getList(), CounselorDO::getId));
        // 2. 拼接返回
        return BeanUtils.toBean(pageResult, CounselorRespVO.class, respVO -> {
            DeptRespDTO dept = deptMap.get(respVO.getDeptId());
            if (dept != null) {
                respVO.setDeptName(dept.getName());
            }
            respVO.setClassNames(classNamesMap.getOrDefault(respVO.getId(), Collections.emptyList()));
        });
    }

    private void fillDeptAndClasses(CounselorRespVO respVO) {
        DeptRespDTO dept = respVO.getDeptId() != null ? deptApi.getDept(respVO.getDeptId()) : null;
        if (dept != null) {
            respVO.setDeptName(dept.getName());
        }
        respVO.setClassNames(counselorService.getCounselorClassNames(respVO.getId()));
    }

}
