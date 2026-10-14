package cn.iocoder.yudao.module.timetable.controller.admin;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.timetable.controller.admin.vo.*;
import cn.iocoder.yudao.module.timetable.service.ClassroomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 课程表教室")
@RestController
@RequestMapping("/timetable/classroom")
@Validated
public class ClassroomController {
    @Resource private ClassroomService service;

    @PostMapping("/create")
    @Operation(summary = "创建教室")
    @PreAuthorize("@ss.hasPermission('timetable:classroom:create')")
    public CommonResult<Long> create(@Valid @RequestBody ClassroomSaveReqVO request) {
        return success(service.create(request));
    }

    @PutMapping("/update")
    @Operation(summary = "修改或停用教室")
    @PreAuthorize("@ss.hasPermission('timetable:classroom:update')")
    public CommonResult<Boolean> update(@Valid @RequestBody ClassroomSaveReqVO request) {
        service.update(request);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除未被课次引用的教室")
    @PreAuthorize("@ss.hasPermission('timetable:classroom:delete')")
    public CommonResult<Boolean> delete(@RequestParam @Positive Long id) {
        service.delete(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "教室详情")
    @PreAuthorize("@ss.hasPermission('timetable:classroom:query')")
    public CommonResult<ClassroomRespVO> get(@RequestParam @Positive Long id) {
        return success(service.get(id));
    }

    @GetMapping("/page")
    @Operation(summary = "教室分页")
    @PreAuthorize("@ss.hasPermission('timetable:classroom:query')")
    public CommonResult<PageResult<ClassroomRespVO>> page(@Valid ClassroomPageReqVO request) {
        return success(service.page(request));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "可选启用教室")
    @PreAuthorize("@ss.hasPermission('timetable:classroom:query')")
    public CommonResult<List<ClassroomRespVO>> simpleList() {
        return success(service.simpleList());
    }
}
