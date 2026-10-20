package cn.iocoder.yudao.module.edu.controller.admin.student.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

/**
 * 管理后台 - 学生新增/修改 Request VO
 *
 * 专业由自由输入改为下拉，取专业表 dict_major。
 * TODO: 字典中心模块待接入
 */
@Schema(description = "管理后台 - 学生新增/修改 Request VO")
@Data
public class StudentSaveReqVO {

    @Schema(description = "学生编号", example = "1")
    private Long id;

    @Schema(description = "学号（同时作为系统登录用户名）", requiredMode = Schema.RequiredMode.REQUIRED, example = "20230101")
    @NotBlank(message = "学号不能为空")
    @Size(max = 64, message = "学号长度不能超过 64 个字符")
    private String studentNo;

    @Schema(description = "姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotBlank(message = "姓名不能为空")
    @Size(max = 64, message = "姓名长度不能超过 64 个字符")
    private String name;

    @Schema(description = "性别（1男 2女）", example = "1")
    private Integer gender;

    @Schema(description = "身份证号", example = "110101200001011234")
    private String idCard;

    @Schema(description = "出生日期（不得超过今天）", requiredMode = Schema.RequiredMode.REQUIRED, example = "2000-01-01")
    @NotNull(message = "出生日期不能为空")
    @PastOrPresent(message = "出生日期不能晚于今天")
    private LocalDate birthDate;

    @Schema(description = "民族", example = "汉族")
    private String nation;

    @Schema(description = "政治面貌（字典 edu_political_status）", example = "3")
    private Integer politicalStatus;

    @Schema(description = "籍贯", example = "北京市")
    private String nativePlace;

    @Schema(description = "年级编号", example = "1")
    private Long gradeId;

    @Schema(description = "班级编号", example = "1")
    private Long classId;

    @Schema(description = "专业编号（占位表 dict_major，下拉选择，依赖年级）", example = "1")
    private Long majorId;

    @Schema(description = "所属院系编号", example = "100")
    private Long deptId;

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
    @NotNull(message = "状态不能为空")
    private Integer status = 0;

    @Schema(description = "证件照地址")
    private String photoUrl;

    @Schema(description = "身份证正面地址")
    private String idCardFrontUrl;

    @Schema(description = "身份证反面地址")
    private String idCardBackUrl;

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

}
