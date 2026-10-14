package cn.iocoder.yudao.module.edu.controller.admin.counselor.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 辅导员分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CounselorPageReqVO extends PageParam {

    @Schema(description = "工号", example = "GH001")
    private String jobNo;

    @Schema(description = "姓名", example = "张三")
    private String name;

    @Schema(description = "性别", example = "1")
    private Integer gender;

    @Schema(description = "手机号", example = "13800138000")
    private String mobile;

    @Schema(description = "职称", example = "讲师")
    private String title;

    @Schema(description = "所属院系编号", example = "100")
    private Long deptId;

    @Schema(description = "状态", example = "0")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
