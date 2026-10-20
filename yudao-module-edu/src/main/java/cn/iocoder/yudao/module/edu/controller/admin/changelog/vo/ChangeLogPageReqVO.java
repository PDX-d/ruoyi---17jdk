package cn.iocoder.yudao.module.edu.controller.admin.changelog.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 管理后台 - 变更日志分页 Request VO
 */
@Schema(description = "管理后台 - 变更日志分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ChangeLogPageReqVO extends PageParam {

    @Schema(description = "业务模块（如：学生管理）", example = "学生管理")
    private String bizModule;

    @Schema(description = "业务主键（如：学生编号）", example = "1")
    private Long bizId;

    @Schema(description = "业务编号（如：学号）", example = "20230101")
    private String bizNo;

    @Schema(description = "业务名称（如：学生姓名）", example = "张三")
    private String bizName;

    @Schema(description = "操作类型（字典 edu_change_log_action）", example = "2")
    private Integer action;

    @Schema(description = "操作人姓名", example = "芋道")
    private String operatorName;

    @Schema(description = "操作时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
