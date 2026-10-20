package cn.iocoder.yudao.module.edu.controller.admin.changelog.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理后台 - 变更日志 Response VO
 *
 * 同时用于接口返回与 Excel 导出（导出依赖 {@link ExcelProperty} 的表头配置）。
 */
@Schema(description = "管理后台 - 变更日志 Response VO")
@Data
public class ChangeLogRespVO {

    @Schema(description = "编号", example = "1")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "业务模块", example = "学生管理")
    @ExcelProperty("业务模块")
    private String bizModule;

    @Schema(description = "业务编号（如：学号）", example = "20230101")
    @ExcelProperty("业务编号")
    private String bizNo;

    @Schema(description = "业务名称（如：学生姓名）", example = "张三")
    @ExcelProperty("业务名称")
    private String bizName;

    @Schema(description = "操作类型（字典 edu_change_log_action）", example = "2")
    @ExcelProperty(value = "操作类型", converter = DictConvert.class)
    @DictFormat("edu_change_log_action")
    private Integer action;

    @Schema(description = "变更字段中文名", example = "手机号")
    @ExcelProperty("变更字段")
    private String fieldLabel;

    @Schema(description = "变更前值", example = "15601691300")
    @ExcelProperty("变更前")
    private String beforeValue;

    @Schema(description = "变更后值", example = "15601701300")
    @ExcelProperty("变更后")
    private String afterValue;

    @Schema(description = "操作人姓名", example = "芋道")
    @ExcelProperty("操作人")
    private String operatorName;

    @Schema(description = "备注 / 摘要", example = "新增学生：张三")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "操作时间")
    @ExcelProperty("操作时间")
    private LocalDateTime createTime;

}
