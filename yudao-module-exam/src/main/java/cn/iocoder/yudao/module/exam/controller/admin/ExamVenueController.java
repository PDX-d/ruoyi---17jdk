package cn.iocoder.yudao.module.exam.controller.admin;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.exam.controller.admin.vo.ExamVO.*;
import cn.iocoder.yudao.module.exam.service.ExamVenueService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@RestController
@RequestMapping("/exam/venue")
@Validated
public class ExamVenueController {
    @Resource private ExamVenueService service;

    @PostMapping("/create")
    @PreAuthorize("@ss.hasPermission('exam:venue:create')")
    public CommonResult<Long> create(@Valid @RequestBody VenueSaveReq request) {
        return success(service.create(request));
    }

    @PutMapping("/update")
    @PreAuthorize("@ss.hasPermission('exam:venue:update')")
    public CommonResult<Boolean> update(@Valid @RequestBody VenueSaveReq request) {
        service.update(request);
        return success(true);
    }

    @DeleteMapping("/delete")
    @PreAuthorize("@ss.hasPermission('exam:venue:delete')")
    public CommonResult<Boolean> delete(@RequestParam @Positive Long id,
                                        @RequestParam @PositiveOrZero Long version) {
        service.delete(id, version);
        return success(true);
    }

    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('exam:venue:query')")
    public CommonResult<VenueResp> get(@RequestParam @Positive Long id) {
        return success(service.get(id));
    }

    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('exam:venue:query')")
    public CommonResult<PageResult<VenueResp>> page(@Valid VenueQueryReq request) {
        return success(service.page(request));
    }

    @GetMapping("/simple-list")
    @PreAuthorize("@ss.hasAnyPermissions('exam:venue:query','exam:session:query')")
    public CommonResult<List<VenueResp>> simpleList() {
        return success(service.simpleList());
    }
}
