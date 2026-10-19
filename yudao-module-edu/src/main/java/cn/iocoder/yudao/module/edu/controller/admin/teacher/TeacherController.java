package cn.iocoder.yudao.module.edu.controller.admin.teacher;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.edu.controller.admin.teacher.vo.TeacherPageReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.teacher.vo.TeacherRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.teacher.vo.TeacherSaveReqVO;
import cn.iocoder.yudao.module.edu.dal.dataobject.teacher.TeacherDO;
import cn.iocoder.yudao.module.edu.service.teacher.TeacherService;
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
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 教师管理")
@RestController
@RequestMapping("/edu/teacher")
@Validated
public class TeacherController {

    @Resource
    private TeacherService teacherService;

    @Resource
    private DeptApi deptApi;

    @PostMapping("/create")
    @Operation(summary = "创建教师")
    @PreAuthorize("@ss.hasPermission('edu:teacher:create')")
    public CommonResult<Long> createTeacher(@Valid @RequestBody TeacherSaveReqVO createReqVO) {
        return success(teacherService.createTeacher(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新教师")
    @PreAuthorize("@ss.hasPermission('edu:teacher:update')")
    public CommonResult<Boolean> updateTeacher(@Valid @RequestBody TeacherSaveReqVO updateReqVO) {
        teacherService.updateTeacher(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除教师")
    @Parameter(name = "id", description = "教师编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('edu:teacher:delete')")
    public CommonResult<Boolean> deleteTeacher(@RequestParam("id") Long id) {
        teacherService.deleteTeacher(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得教师")
    @Parameter(name = "id", description = "教师编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('edu:teacher:query')")
    public CommonResult<TeacherRespVO> getTeacher(@RequestParam("id") Long id) {
        TeacherDO teacher = teacherService.getTeacher(id);
        if (teacher == null) {
            return success(null);
        }
        return success(BeanUtils.toBean(teacher, TeacherRespVO.class, this::fillDeptName));
    }

    @GetMapping("/page")
    @Operation(summary = "获得教师分页")
    @PreAuthorize("@ss.hasPermission('edu:teacher:query')")
    public CommonResult<PageResult<TeacherRespVO>> getTeacherPage(@Valid TeacherPageReqVO pageReqVO) {
        PageResult<TeacherDO> pageResult = teacherService.getTeacherPage(pageReqVO);
        return success(buildRespVOPage(pageResult));
    }

    private PageResult<TeacherRespVO> buildRespVOPage(PageResult<TeacherDO> pageResult) {
        if (CollUtil.isEmpty(pageResult.getList())) {
            return BeanUtils.toBean(pageResult, TeacherRespVO.class);
        }
        // 1. 批量查询院系名称
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(
                CollectionUtils.convertSet(pageResult.getList(), TeacherDO::getDeptId));
        // 2. 拼接返回
        return BeanUtils.toBean(pageResult, TeacherRespVO.class, respVO -> {
            DeptRespDTO dept = deptMap.get(respVO.getDeptId());
            if (dept != null) {
                respVO.setDeptName(dept.getName());
            }
        });
    }

    private void fillDeptName(TeacherRespVO respVO) {
        DeptRespDTO dept = respVO.getDeptId() != null ? deptApi.getDept(respVO.getDeptId()) : null;
        if (dept != null) {
            respVO.setDeptName(dept.getName());
        }
    }

}
