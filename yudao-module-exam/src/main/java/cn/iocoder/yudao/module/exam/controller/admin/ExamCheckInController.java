package cn.iocoder.yudao.module.exam.controller.admin;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.exam.controller.admin.vo.ExamVO.*;
import cn.iocoder.yudao.module.exam.service.ExamCheckInService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.validation.Valid;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@RestController
@RequestMapping("/exam/check-in")
@Validated
public class ExamCheckInController {
    @Resource private ExamCheckInService service;

    @PostMapping("/create")
    @PreAuthorize("@ss.hasPermission('exam:check-in:create')")
    public CommonResult<CheckInResp> create(@Valid @RequestBody CheckInReq request) {
        return success(service.create(request));
    }
}
