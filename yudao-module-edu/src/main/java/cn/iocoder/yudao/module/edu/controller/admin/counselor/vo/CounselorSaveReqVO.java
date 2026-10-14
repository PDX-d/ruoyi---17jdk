package cn.iocoder.yudao.module.edu.controller.admin.counselor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

@Schema(description = "管理后台 - 辅导员新增/修改 Request VO")
@Data
public class CounselorSaveReqVO {

    @Schema(description = "辅导员编号", example = "1")
    private Long id;

    @Schema(description = "工号", requiredMode = Schema.RequiredMode.REQUIRED, example = "GH001")
    @NotBlank(message = "工号不能为空")
    @Size(max = 64, message = "工号长度不能超过 64 个字符")
    private String jobNo;

    @Schema(description = "姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotBlank(message = "姓名不能为空")
    @Size(max = 64, message = "姓名长度不能超过 64 个字符")
    private String name;

    @Schema(description = "性别", example = "1")
    private Integer gender;

    @Schema(description = "手机号", example = "13800138000")
    @Size(max = 20, message = "手机号长度不能超过 20 个字符")
    private String mobile;

    @Schema(description = "职称", example = "讲师")
    @Size(max = 64, message = "职称长度不能超过 64 个字符")
    private String title;

    @Schema(description = "所属院系编号", example = "100")
    private Long deptId;

    @Schema(description = "所带班级名称列表", example = "[\"计算机2301班\",\"计算机2302班\"]")
    private List<String> classNames;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

    @Schema(description = "备注", example = "备注")
    private String remark;

}
