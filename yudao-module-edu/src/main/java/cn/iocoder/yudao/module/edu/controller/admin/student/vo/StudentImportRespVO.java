package cn.iocoder.yudao.module.edu.controller.admin.student.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 管理后台 - 学生导入 Response VO
 */
@Schema(description = "管理后台 - 学生导入 Response VO")
@Data
@Builder
public class StudentImportRespVO {

    @Schema(description = "创建成功的学生学号数组", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> createStudentNos;

    @Schema(description = "更新成功的学生学号数组", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> updateStudentNos;

    @Schema(description = "导入失败的学生集合，key 为学号，value 为失败原因", requiredMode = Schema.RequiredMode.REQUIRED)
    private Map<String, String> failureStudentNos;

}
