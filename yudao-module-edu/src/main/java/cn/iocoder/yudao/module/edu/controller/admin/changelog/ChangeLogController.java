package cn.iocoder.yudao.module.edu.controller.admin.changelog;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.edu.controller.admin.changelog.vo.ChangeLogPageReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.changelog.vo.ChangeLogRespVO;
import cn.iocoder.yudao.module.edu.dal.dataobject.changelog.ChangeLogDO;
import cn.iocoder.yudao.module.edu.service.changelog.ChangeLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - 变更日志（档案变更历史）
 *
 * 变更日志由各业务 Service 自动写入；本 Controller 只提供「查询 / 导出」，日志只追加、不允许人工修改。
 */
@Tag(name = "管理后台 - 变更日志")
@RestController
@RequestMapping("/edu/change-log")
@Validated
public class ChangeLogController {

    @Resource
    private ChangeLogService changeLogService;

    @GetMapping("/page")
    @Operation(summary = "获得变更日志分页")
    @PreAuthorize("@ss.hasPermission('edu:change-log:query')")
    public CommonResult<PageResult<ChangeLogRespVO>> getChangeLogPage(@Validated ChangeLogPageReqVO pageReqVO) {
        PageResult<ChangeLogDO> pageResult = changeLogService.getChangeLogPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ChangeLogRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出变更日志")
    @PreAuthorize("@ss.hasPermission('edu:change-log:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportChangeLogExcel(@Validated ChangeLogPageReqVO exportReqVO,
                                     HttpServletResponse response) throws IOException {
        exportReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ChangeLogDO> list = changeLogService.getChangeLogPage(exportReqVO).getList();
        if (CollUtil.isEmpty(list)) {
            list = CollUtil.newArrayList();
        }
        ExcelUtils.write(response, "变更日志.xlsx", "数据", ChangeLogRespVO.class,
                BeanUtils.toBean(list, ChangeLogRespVO.class));
    }

}
