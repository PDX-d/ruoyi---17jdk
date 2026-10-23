package cn.iocoder.yudao.module.exam.controller.admin;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.exam.controller.admin.vo.ExamVO.*;
import cn.iocoder.yudao.module.exam.service.ExamScoreService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.validation.Valid;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@RestController
@RequestMapping("/exam/statistics")
@Validated
public class ExamStatisticsController {
    @Resource private ExamScoreService service;

    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('exam:statistics:query')")
    public CommonResult<StatisticsResp> get(@Valid ExamQueryReq request) {
        return success(service.statistics(request));
    }
}
