package cn.iocoder.yudao.module.edu.controller.admin.student.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 管理后台 - 学生分页 Request VO
 */
@Schema(description = "管理后台 - 学生分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class StudentPageReqVO extends PageParam {

    @Schema(description = "学号", example = "20230101")
    private String studentNo;

    @Schema(description = "姓名", example = "张三")
    private String name;

    @Schema(description = "性别（1男 2女）", example = "1")
    private Integer gender;

    @Schema(description = "状态（0在读 1休学 2退学 3毕业 4复学）", example = "0")
    private Integer status;

    @Schema(description = "年级编号", example = "1")
    private Long gradeId;

    @Schema(description = "班级编号", example = "1")
    private Long classId;

    @Schema(description = "所属院系编号", example = "100")
    private Long deptId;

    @Schema(description = "手机号", example = "13800138000")
    private String mobile;

    @Schema(description = "政治面貌（字典 edu_political_status）", example = "3")
    private Integer politicalStatus;

    @Schema(description = "入学年份", example = "2023")
    private Integer enrollmentYear;

    @Schema(description = "标签编号（按标签筛选，对应 SM-005）", example = "1")
    private Long tagId;

    @Schema(description = "关联系统账号编号（内部使用：学生「仅本人数据」过滤，不对外暴露）", hidden = true)
    private Long userId;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
