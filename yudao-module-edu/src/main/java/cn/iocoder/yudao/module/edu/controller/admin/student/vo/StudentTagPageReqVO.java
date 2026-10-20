package cn.iocoder.yudao.module.edu.controller.admin.student.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 管理后台 - 学生标签分页 Request VO
 */
@Schema(description = "管理后台 - 学生标签分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class StudentTagPageReqVO extends PageParam {

    @Schema(description = "标签名称", example = "优秀学生")
    private String name;

}
