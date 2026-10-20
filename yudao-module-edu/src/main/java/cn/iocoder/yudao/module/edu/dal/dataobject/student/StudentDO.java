package cn.iocoder.yudao.module.edu.dal.dataobject.student;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDate;

/**
 * 学生 DO
 *
 * 字段覆盖学生档案基本信息；后续若需拆分家庭信息 / 附件关联表，直接改本 DO 与 DDL。
 */
@TableName("edu_student")
@KeySequence("edu_student_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentDO extends TenantBaseDO {

    /**
     * 学生编号
     */
    @TableId
    private Long id;
    /**
     * 学号（同时作为系统登录用户名）
     */
    private String studentNo;
    /**
     * 姓名
     */
    private String name;
    /**
     * 性别（1男 2女）
     */
    private Integer gender;
    /**
     * 身份证号
     */
    private String idCard;
    /**
     * 出生日期
     */
    private LocalDate birthDate;
    /**
     * 民族
     */
    private String nation;
    /**
     * 政治面貌（字典 edu_political_status）
     */
    private Integer politicalStatus;
    /**
     * 籍贯
     */
    private String nativePlace;
    /**
     * 年级编号
     */
    private Long gradeId;
    /**
     * 班级编号
     */
    private Long classId;
    /**
     * 专业编号
     *
     * 数据源：专业表 dict_major。
     * TODO: 字典中心模块待接入
     */
    private Long majorId;
    /**
     * 专业名称（冗余存储，便于展示；保存时由 dict_major 回填）
     */
    private String major;
    /**
     * 所属院系编号
     */
    private Long deptId;
    /**
     * 学制（字典 edu_education_system，已移除「三年制」）
     */
    private Integer educationSystem;
    /**
     * 入学年份
     */
    private Integer enrollmentYear;
    /**
     * 入学日期
     */
    private LocalDate admissionDate;
    /**
     * 毕业日期
     */
    private LocalDate graduateDate;
    /**
     * 宿舍
     */
    private String dormitory;
    /**
     * 手机号
     */
    private String mobile;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 状态
     *
     * 0 - 在读；1 - 休学；2 - 退学；3 - 毕业；4 - 复学
     */
    private Integer status;
    /**
     * 证件照地址
     */
    private String photoUrl;
    /**
     * 身份证正面地址
     */
    private String idCardFrontUrl;
    /**
     * 身份证反面地址
     */
    private String idCardBackUrl;
    /**
     * 关联系统账号编号（创建学生时自动建号后回写）
     */
    private Long userId;
    /**
     * 监护人姓名
     */
    private String guardianName;
    /**
     * 监护人手机号
     */
    private String guardianMobile;
    /**
     * 家庭住址
     */
    private String address;
    /**
     * 紧急联系人
     */
    private String emergencyContact;
    /**
     * 紧急联系人电话
     */
    private String emergencyMobile;
    /**
     * 备注
     */
    private String remark;

}
