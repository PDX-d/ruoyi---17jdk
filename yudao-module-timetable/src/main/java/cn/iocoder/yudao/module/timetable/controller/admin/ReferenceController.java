package cn.iocoder.yudao.module.timetable.controller.admin;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.timetable.controller.admin.vo.ReferenceOptionsRespVO;
import cn.iocoder.yudao.module.timetable.service.reference.ReferenceOptionsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 课程表公共选项")
@RestController
@RequestMapping("/timetable/reference")
public class ReferenceController {
    @Resource private ReferenceOptionsService service;

    @GetMapping("/options")
    @Operation(summary = "获取学期、课程、教师、班级选项及本人身份，模拟元数据与排课校验同源")
    @PreAuthorize("@ss.hasPermission('timetable:lesson:query')")
    public CommonResult<ReferenceOptionsRespVO> options() {
        return success(service.getOptions());
    }
}
