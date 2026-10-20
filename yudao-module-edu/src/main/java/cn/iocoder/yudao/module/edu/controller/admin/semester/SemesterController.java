package cn.iocoder.yudao.module.edu.controller.admin.semester;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.edu.controller.admin.semester.vo.SemesterCalendarRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.semester.vo.SemesterCalendarSaveReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.semester.vo.SemesterPageReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.semester.vo.SemesterRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.semester.vo.SemesterSaveReqVO;
import cn.iocoder.yudao.module.edu.dal.dataobject.semester.SemesterDO;
import cn.iocoder.yudao.module.edu.service.semester.SemesterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 学期管理 Controller（含学期校历节点接口）
 */
@Tag(name = "管理后台 - 学期管理")
@RestController
@RequestMapping("/edu/semester")
@Validated
public class SemesterController {

    @Resource
    private SemesterService semesterService;

    @PostMapping("/create")
    @Operation(summary = "创建学期")
    @PreAuthorize("@ss.hasPermission('edu:semester:create')")
    public CommonResult<Long> createSemester(@Valid @RequestBody SemesterSaveReqVO createReqVO) {
        return success(semesterService.createSemester(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新学期")
    @PreAuthorize("@ss.hasPermission('edu:semester:update')")
    public CommonResult<Boolean> updateSemester(@Valid @RequestBody SemesterSaveReqVO updateReqVO) {
        semesterService.updateSemester(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除学期")
    @Parameter(name = "id", description = "学期编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('edu:semester:delete')")
    public CommonResult<Boolean> deleteSemester(@RequestParam("id") Long id) {
        semesterService.deleteSemester(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得学期")
    @Parameter(name = "id", description = "学期编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('edu:semester:query')")
    public CommonResult<SemesterRespVO> getSemester(@RequestParam("id") Long id) {
        SemesterDO semester = semesterService.getSemester(id);
        if (semester == null) {
            return success(null);
        }
        return success(BeanUtils.toBean(semester, SemesterRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得学期分页")
    @PreAuthorize("@ss.hasPermission('edu:semester:query')")
    public CommonResult<PageResult<SemesterRespVO>> getSemesterPage(@Valid SemesterPageReqVO pageReqVO) {
        PageResult<SemesterDO> pageResult = semesterService.getSemesterPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, SemesterRespVO.class));
    }

    @PutMapping("/archive")
    @Operation(summary = "归档 / 取消归档学期")
    @Parameter(name = "id", description = "学期编号", required = true, example = "1024")
    @Parameter(name = "archived", description = "是否归档（true 归档、false 取消归档）", required = true, example = "true")
    @PreAuthorize("@ss.hasPermission('edu:semester:update')")
    public CommonResult<Boolean> archiveSemester(@RequestParam("id") Long id,
                                                @RequestParam("archived") Boolean archived) {
        semesterService.archiveSemester(id, archived);
        return success(true);
    }

    @GetMapping("/get-college-name")
    @Operation(summary = "获得学院名称", description = "用于学期名称自动拼装，如「能工智人学院」")
    @PreAuthorize("@ss.hasPermission('edu:semester:query')")
    public CommonResult<String> getCollegeName() {
        return success(semesterService.getCollegeName());
    }

    @GetMapping("/get-current")
    @Operation(summary = "获得当前学期")
    @PreAuthorize("@ss.hasPermission('edu:semester:query')")
    public CommonResult<SemesterRespVO> getCurrentSemester() {
        SemesterDO semester = semesterService.getCurrentSemester();
        if (semester == null) {
            return success(null);
        }
        return success(BeanUtils.toBean(semester, SemesterRespVO.class));
    }

    @PostMapping("/calendar/create")
    @Operation(summary = "创建校历节点")
    @PreAuthorize("@ss.hasPermission('edu:semester:calendar:create')")
    public CommonResult<Long> createCalendar(@Valid @RequestBody SemesterCalendarSaveReqVO createReqVO) {
        return success(semesterService.createCalendar(createReqVO));
    }

    @PutMapping("/calendar/update")
    @Operation(summary = "更新校历节点")
    @PreAuthorize("@ss.hasPermission('edu:semester:calendar:update')")
    public CommonResult<Boolean> updateCalendar(@Valid @RequestBody SemesterCalendarSaveReqVO updateReqVO) {
        semesterService.updateCalendar(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/calendar/delete")
    @Operation(summary = "删除校历节点")
    @Parameter(name = "id", description = "校历节点编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('edu:semester:calendar:delete')")
    public CommonResult<Boolean> deleteCalendar(@RequestParam("id") Long id) {
        semesterService.deleteCalendar(id);
        return success(true);
    }

    @GetMapping("/calendar/list")
    @Operation(summary = "获得学期校历节点列表")
    @Parameter(name = "semesterId", description = "学期编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('edu:semester:calendar:query')")
    public CommonResult<List<SemesterCalendarRespVO>> getCalendarList(@RequestParam("semesterId") Long semesterId) {
        return success(BeanUtils.toBean(semesterService.getCalendarList(semesterId), SemesterCalendarRespVO.class));
    }

}
