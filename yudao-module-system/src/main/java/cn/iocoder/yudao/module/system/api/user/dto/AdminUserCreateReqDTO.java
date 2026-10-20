package cn.iocoder.yudao.module.system.api.user.dto;

import lombok.Data;

/**
 * Admin 用户创建 Request DTO
 *
 * 【说明，给后续开发同学】
 * 本 DTO 是「模块三 学生管理 - 创建学生时自动建号」这个跨模块调用的最小新增，
 * 目的是让 edu 模块不必依赖 system 模块的 controller 层 VO（UserSaveReqVO）。
 * 本期只暴露建号所需的最少字段；后续若其它模块也需要建号，
 * 可在此 DTO 上继续补充字段，并同步 {@link cn.iocoder.yudao.module.system.api.user.AdminUserApi#createUser} 的实现。
 *
 * @author 芋道源码
 */
@Data
public class AdminUserCreateReqDTO {

    /**
     * 用户账号（必填，4-30 位数字/字母）
     */
    private String username;
    /**
     * 用户昵称（必填）
     */
    private String nickname;
    /**
     * 密码（必填，4-16 位）
     */
    private String password;
    /**
     * 部门编号
     */
    private Long deptId;
    /**
     * 手机号码
     */
    private String mobile;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 性别（1 男 2 女）
     */
    private Integer sex;
    /**
     * 备注
     */
    private String remark;

}
