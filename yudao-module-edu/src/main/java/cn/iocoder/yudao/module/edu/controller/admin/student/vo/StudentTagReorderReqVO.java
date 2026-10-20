package cn.iocoder.yudao.module.edu.controller.admin.student.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 管理后台 - 学生标签拖拽排序 Request VO
 *
 * tagIds 按新顺序排列，后端校验其为「当前全部未删除标签」的完整排列后批量重写 sort。
 */
@Schema(description = "管理后台 - 学生标签拖拽排序 Request VO")
@Data
public class StudentTagReorderReqVO {

    @Schema(description = "拖拽后的标签编号顺序（必须包含当前全部标签，不重不漏）",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "3,1,2")
    @NotEmpty(message = "标签顺序不能为空")
    private List<Long> tagIds;

}
