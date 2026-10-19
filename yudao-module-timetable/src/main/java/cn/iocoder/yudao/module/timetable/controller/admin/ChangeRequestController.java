package cn.iocoder.yudao.module.timetable.controller.admin;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.timetable.controller.admin.vo.*;
import cn.iocoder.yudao.module.timetable.service.ChangeRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.Positive;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 调停课申请")
@RestController
@RequestMapping("/timetable/change-request")
@Validated
public class ChangeRequestController {
    @Resource private ChangeRequestService service;

    @PostMapping("/create")
    @Operation(summary = "教师申请调整本人尚未开始的课次")
    @PreAuthorize("@ss.hasPermission('timetable:change-request:create')")
    public CommonResult<Long> create(@Valid @RequestBody ChangeCreateReqVO request) {
        return success(service.create(request));
    }

    @GetMapping("/get")
    @Operation(summary = "申请详情")
    @PreAuthorize("@ss.hasPermission('timetable:change-request:query')")
    public CommonResult<ChangeRespVO> get(@RequestParam @Positive Long id) {
        return success(service.get(id));
    }

    @GetMapping("/page")
    @Operation(summary = "申请分页")
    @PreAuthorize("@ss.hasPermission('timetable:change-request:query')")
    public CommonResult<PageResult<ChangeRespVO>> page(@Valid ChangePageReqVO request) {
        return success(service.page(request));
    }

    @PutMapping("/approve")
    @Operation(summary = "管理员通过申请并事务发送站内信")
    @PreAuthorize("@ss.hasPermission('timetable:change-request:approve')")
    public CommonResult<Boolean> approve(@Valid @RequestBody ChangeReviewReqVO request) {
        service.approve(request);
        return success(true);
    }

    @PutMapping("/reject")
    @Operation(summary = "管理员拒绝申请")
    @PreAuthorize("@ss.hasPermission('timetable:change-request:reject')")
    public CommonResult<Boolean> reject(@Valid @RequestBody ChangeReviewReqVO request) {
        service.reject(request);
        return success(true);
    }
}
