package cn.iocoder.yudao.module.exam.controller.admin;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.exam.controller.admin.vo.ExamVO.*;
import cn.iocoder.yudao.module.exam.service.ExamScoreService;
import cn.iocoder.yudao.module.exam.service.export.ExamScoreFileService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.io.IOException;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@RestController
@RequestMapping("/exam/score")
@Validated
public class ExamScoreController {
    @Resource private ExamScoreService service;
    @Resource private ExamScoreFileService files;

    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('exam:score:query')")
    public CommonResult<PageResult<ScoreRowResp>> page(@Valid ScoreQueryReq request) {
        return success(service.page(request));
    }

    @PutMapping("/batch-save")
    @PreAuthorize("@ss.hasPermission('exam:score:update')")
    public CommonResult<Boolean> save(@Valid @RequestBody ScoreSaveReq request) {
        service.save(request);
        return success(true);
    }

    @PutMapping("/config")
    @PreAuthorize("@ss.hasPermission('exam:score:config')")
    public CommonResult<Boolean> config(@Valid @RequestBody ScoreConfigReq request) {
        service.config(request);
        return success(true);
    }

    @GetMapping("/import-template")
    @PreAuthorize("@ss.hasPermission('exam:score:import')")
    public void template(@RequestParam @Positive Long sessionId, HttpServletResponse response) throws IOException {
        files.template(sessionId, response);
    }

    @PostMapping(value = "/import", consumes = "multipart/form-data")
    @PreAuthorize("@ss.hasPermission('exam:score:import')")
    public CommonResult<ImportResp> importScores(@RequestParam MultipartFile file,
                                                 @RequestParam @Positive Long sessionId,
                                                 @RequestParam @PositiveOrZero Long sessionVersion) {
        return success(files.importScores(file, sessionId, sessionVersion));
    }

    @GetMapping("/export")
    @PreAuthorize("@ss.hasPermission('exam:score:export')")
    public void export(@Valid ExamQueryReq request,
                       @RequestParam(defaultValue = "xlsx") @Pattern(regexp = "xlsx|pdf") String format,
                       HttpServletResponse response) throws IOException {
        files.export(request, format, response);
    }
}
