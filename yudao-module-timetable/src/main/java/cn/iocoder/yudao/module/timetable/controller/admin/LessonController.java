package cn.iocoder.yudao.module.timetable.controller.admin;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.timetable.controller.admin.vo.*;
import cn.iocoder.yudao.module.timetable.service.LessonService;
import cn.iocoder.yudao.module.timetable.service.export.TimetableExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.io.IOException;
import java.util.List;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 课程表课次")
@RestController
@RequestMapping("/timetable/lesson")
@Validated
public class LessonController {
    @Resource private LessonService service;
    @Resource private TimetableExportService exports;

    @PostMapping("/create")
    @Operation(summary = "创建具体课次")
    @PreAuthorize("@ss.hasPermission('timetable:lesson:create')")
    public CommonResult<Long> create(@Valid @RequestBody LessonSaveReqVO request) {
        return success(service.create(request));
    }

    @PostMapping("/batch-create")
    @Operation(summary = "按日期、星期和每日时间整批排课")
    @PreAuthorize("@ss.hasPermission('timetable:lesson:create')")
    public CommonResult<List<Long>> batchCreate(@Valid @RequestBody LessonBatchReqVO request) {
        return success(service.batchCreate(request));
    }

    @PutMapping("/update")
    @Operation(summary = "按当前版本更新课次")
    @PreAuthorize("@ss.hasPermission('timetable:lesson:update')")
    public CommonResult<Boolean> update(@Valid @RequestBody LessonSaveReqVO request) {
        service.update(request);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除无待审申请的课次")
    @PreAuthorize("@ss.hasPermission('timetable:lesson:delete')")
    public CommonResult<Boolean> delete(@RequestParam @Positive Long id) {
        service.delete(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "课次详情")
    @PreAuthorize("@ss.hasPermission('timetable:lesson:query')")
    public CommonResult<LessonRespVO> get(@RequestParam @Positive Long id) {
        return success(service.get(id));
    }

    @GetMapping("/page")
    @Operation(summary = "按权限范围查询课次分页")
    @PreAuthorize("@ss.hasPermission('timetable:lesson:query')")
    public CommonResult<PageResult<LessonRespVO>> page(@Valid LessonQueryReqVO request) {
        return success(service.page(request));
    }

    @GetMapping("/calendar")
    @Operation(summary = "查询明确日期范围内的课次日历数据")
    @PreAuthorize("@ss.hasPermission('timetable:lesson:query')")
    public CommonResult<List<LessonRespVO>> calendar(@Valid LessonQueryReqVO request) {
        return success(service.calendar(request));
    }

    @PostMapping("/check-conflict")
    @Operation(summary = "教师、班级、教室冲突预检，不替代保存时检查")
    @PreAuthorize("@ss.hasPermission('timetable:lesson:query')")
    public CommonResult<List<ConflictRespVO>> checkConflict(@Valid @RequestBody LessonSaveReqVO request) {
        return success(service.checkConflict(request));
    }

    @GetMapping("/export")
    @Operation(summary = "按相同权限导出 PNG 或按周分页 PDF")
    @PreAuthorize("@ss.hasPermission('timetable:lesson:export')")
    public void export(@Valid LessonQueryReqVO request, HttpServletResponse response) throws IOException {
        exports.export(request, response);
    }
}
