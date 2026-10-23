package cn.iocoder.yudao.module.exam.controller.admin;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.exam.controller.admin.vo.ExamVO.*;
import cn.iocoder.yudao.module.exam.service.ExamMyExamService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.Positive;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@RestController
@RequestMapping("/exam/my-exam")
@Validated
public class ExamMyExamController {
    @Resource private ExamMyExamService service;

    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('exam:my-exam:query')")
    public CommonResult<PageResult<MyExamResp>> page(@Valid ExamQueryReq request) {
        return success(service.page(request));
    }

    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('exam:my-exam:query')")
    public CommonResult<MyExamResp> get(@RequestParam @Positive Long id) {
        return success(service.get(id));
    }
}
