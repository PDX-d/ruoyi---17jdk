package cn.iocoder.yudao.module.edu.controller.admin.student.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理后台 - 学生 Response VO
 */
@Schema(description = "管理后台 - 学生 Response VO")
@Data
public class StudentRespVO {

    @Schema(description = "学生编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "学号", requiredMode = Schema.RequiredMode.REQUIRED, example = "20230101")
    private String studentNo;

    @Schema(description = "姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    private String name;

    @Schema(description = "性别（1男 2女）", example = "1")
    private Integer gender;

    @Schema(description = "身份证号", example = "110101200001011234")
    private String idCard;

    @Schema(description = "出生日期", example = "2000-01-01")
    private LocalDate birthDate;

    @Schema(description = "民族", example = "汉族")
    private String nation;

    @Schema(description = "政治面貌（字典 edu_political_status）", example = "3")
    private Integer politicalStatus;

    @Schema(description = "籍贯", example = "北京市")
    private String nativePlace;

    @Schema(description = "年级编号", example = "1")
    private Long gradeId;

    @Schema(description = "年级名称", example = "2023级")
    private String gradeName;

    @Schema(description = "班级编号", example = "1")
    private Long classId;

    @Schema(description = "班级名称", example = "软件工程2301班")
    private String className;

    @Schema(description = "专业编号", example = "1")
    private Long majorId;

    @Schema(description = "专业名称", example = "软件工程")
    private String majorName;

    @Schema(description = "所属院系编号", example = "100")
    private Long deptId;

    @Schema(description = "所属院系名称", example = "计算机学院")
    private String deptName;

    @Schema(description = "学制（字典 edu_education_system，已移除「三年制」）", example = "3")
    private Integer educationSystem;

    @Schema(description = "入学年份", example = "2023")
    private Integer enrollmentYear;

    @Schema(description = "入学日期", example = "2023-09-01")
    private LocalDate admissionDate;

    @Schema(description = "毕业日期", example = "2027-06-30")
    private LocalDate graduateDate;

    @Schema(description = "宿舍", example = "1号楼101")
    private String dormitory;

    @Schema(description = "手机号", example = "13800138000")
    private String mobile;

    @Schema(description = "邮箱", example = "student@iocoder.cn")
    private String email;

    @Schema(description = "状态（0在读 1休学 2退学 3毕业 4复学）", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

    @Schema(description = "证件照地址")
    private String photoUrl;

    @Schema(description = "身份证正面地址")
    private String idCardFrontUrl;

    @Schema(description = "身份证反面地址")
    private String idCardBackUrl;

    @Schema(description = "关联系统账号编号", example = "1")
    private Long userId;

    @Schema(description = "监护人姓名", example = "张父")
    private String guardianName;

    @Schema(description = "监护人手机号", example = "13800138000")
    private String guardianMobile;

    @Schema(description = "家庭住址", example = "北京市海淀区")
    private String address;

    @Schema(description = "紧急联系人", example = "李四")
    private String emergencyContact;

    @Schema(description = "紧急联系人电话", example = "13800138002")
    private String emergencyMobile;

    @Schema(description = "备注", example = "备注")
    private String remark;

    @Schema(description = "标签编号列表", example = "[1,2]")
    private List<Long> tagIds;

    @Schema(description = "标签列表")
    private List<StudentTagRespVO> tags;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
