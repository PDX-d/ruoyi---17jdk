package cn.iocoder.yudao.module.exam.controller.admin;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.exam.controller.admin.vo.ExamVO.*;
import cn.iocoder.yudao.module.exam.service.ExamReferenceService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.validation.constraints.Positive;
import java.util.List;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@RestController
@RequestMapping("/exam/reference")
@Validated
public class ExamReferenceController {
    @Resource private ExamReferenceService service;

    @GetMapping("/options")
    @PreAuthorize("@ss.hasPermission('exam:reference:query')")
    public CommonResult<ReferenceOptionsResp> options() {
        return success(service.options());
    }

    @GetMapping("/students")
    @PreAuthorize("@ss.hasPermission('exam:reference:query')")
    public CommonResult<List<StudentResp>> students(@RequestParam @Positive Long semesterId,
                                                  @RequestParam @Positive Long courseId,
                                                  @RequestParam @Positive Long classId) {
        return success(service.students(semesterId, courseId, classId));
    }
}
