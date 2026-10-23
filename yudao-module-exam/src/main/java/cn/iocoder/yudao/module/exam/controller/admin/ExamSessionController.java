package cn.iocoder.yudao.module.exam.controller.admin;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.exam.controller.admin.vo.ExamVO.*;
import cn.iocoder.yudao.module.exam.service.ExamSessionService;
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
@RequestMapping("/exam/session")
@Validated
public class ExamSessionController {
    @Resource private ExamSessionService service;

    @PostMapping("/create")
    @PreAuthorize("@ss.hasPermission('exam:session:create')")
    public CommonResult<Long> create(@Valid @RequestBody SessionCreateReq request) {
        return success(service.create(request));
    }

    @PutMapping("/update")
    @PreAuthorize("@ss.hasPermission('exam:session:update')")
    public CommonResult<Boolean> update(@Valid @RequestBody SessionUpdateReq request) {
        service.update(request);
        return success(true);
    }

    @DeleteMapping("/delete")
    @PreAuthorize("@ss.hasPermission('exam:session:delete')")
    public CommonResult<Boolean> delete(@RequestParam @Positive Long id,
                                        @RequestParam @PositiveOrZero Long sessionVersion) {
        service.delete(id, sessionVersion);
        return success(true);
    }

    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('exam:session:query')")
    public CommonResult<SessionResp> get(@RequestParam @Positive Long id) {
        return success(service.get(id));
    }

    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('exam:session:query')")
    public CommonResult<PageResult<SessionResp>> page(@Valid ExamQueryReq request) {
        return success(service.page(request));
    }

    @GetMapping("/participants")
    @PreAuthorize("@ss.hasPermission('exam:session:query')")
    public CommonResult<List<ParticipantResp>> participants(@RequestParam @Positive Long sessionId,
                                                           @RequestParam(required = false) @Positive Long classId) {
        return success(service.participants(sessionId, classId));
    }
}
