package cn.iocoder.yudao.module.edu.controller.admin.student.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 管理后台 - 学生 Excel 导出 VO
 *
 * 表头全为中文，列顺序与 {@link StudentImportExcelVO} 一致，保证「导出 → 修改 → 重新导入」闭环。
 * 有编号的信息（性别/政治面貌/学制/学籍状态）经字典转换输出为文字，专业/院系/年级/班级直接输出名称。
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentExportExcelVO {

    @ExcelProperty("学号")
    private String studentNo;

    @ExcelProperty("姓名")
    private String name;

    /** 字典 system_user_sex：导出为「男 / 女」文字 */
    @ExcelProperty(value = "性别", converter = DictConvert.class)
    @DictFormat("system_user_sex")
    private Integer gender;

    @ExcelProperty("身份证号")
    private String idCard;

    @ExcelProperty("出生日期")
    private LocalDate birthDate;

    @ExcelProperty("民族")
    private String nation;

    /** 字典 edu_political_status：导出为「中共党员 / 共青团员 …」文字 */
    @ExcelProperty(value = "政治面貌", converter = DictConvert.class)
    @DictFormat("edu_political_status")
    private Integer politicalStatus;

    @ExcelProperty("籍贯")
    private String nativePlace;

    /** 专业名称（导出文字，导入时按名称反查编号） */
    @ExcelProperty("专业")
    private String majorName;

    /** 院系名称（导出文字，导入时按名称反查编号） */
    @ExcelProperty("院系")
    private String deptName;

    /** 字典 edu_education_system：导出为「二年制 / 四年制 / 五年制」文字 */
    @ExcelProperty(value = "学制", converter = DictConvert.class)
    @DictFormat("edu_education_system")
    private Integer educationSystem;

    /** 年级名称（导出文字，导入时按名称反查编号） */
    @ExcelProperty("年级")
    private String gradeName;

    /** 班级名称（导出文字，导入时按名称反查编号） */
    @ExcelProperty("班级")
    private String className;

    @ExcelProperty("入学年份")
    private Integer enrollmentYear;

    @ExcelProperty("宿舍")
    private String dormitory;

    @ExcelProperty("手机号")
    private String mobile;

    @ExcelProperty("邮箱")
    private String email;

    /** 字典 edu_student_status：导出为「在读 / 休学 / 退学 / 毕业 / 复学」文字 */
    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat("edu_student_status")
    private Integer status;

    @ExcelProperty("监护人姓名")
    private String guardianName;

    @ExcelProperty("监护人手机号")
    private String guardianMobile;

    @ExcelProperty("备注")
    private String remark;

}
