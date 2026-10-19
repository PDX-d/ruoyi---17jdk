package cn.iocoder.yudao.module.edu.controller.admin.teacher.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 教师 Response VO")
@Data
public class TeacherRespVO {

    @Schema(description = "教师编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "工号", requiredMode = Schema.RequiredMode.REQUIRED, example = "JS001")
    private String jobNo;

    @Schema(description = "姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    private String name;

    @Schema(description = "性别", example = "1")
    private Integer gender;

    @Schema(description = "手机号", example = "13800138000")
    private String mobile;

    @Schema(description = "职称", example = "副教授")
    private String title;

    @Schema(description = "学历", example = "博士")
    private String education;

    @Schema(description = "所属院系编号", example = "100")
    private Long deptId;

    @Schema(description = "所属院系名称", example = "计算机学院")
    private String deptName;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

    @Schema(description = "备注", example = "备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
