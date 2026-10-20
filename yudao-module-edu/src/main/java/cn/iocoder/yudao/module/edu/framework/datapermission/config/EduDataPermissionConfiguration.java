package cn.iocoder.yudao.module.edu.framework.datapermission.config;

import cn.iocoder.yudao.framework.datapermission.core.rule.dept.DeptDataPermissionRuleCustomizer;
import cn.iocoder.yudao.module.edu.dal.dataobject.student.StudentDO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * edu 模块的数据权限 Configuration：把「学生档案」登记进部门数据权限规则，
 * 由 MyBatis-Plus 在 SQL 层自动追加过滤条件（学生 = 仅本人，其他 = 本部门及以下）。
 */
@Configuration(proxyBeanMethods = false)
public class EduDataPermissionConfiguration {

    @Bean
    public DeptDataPermissionRuleCustomizer eduDeptDataPermissionRuleCustomizer() {
        return rule -> {
            // 学生档案：辅导员 / 教师按 dept_id 过滤（data_scope=4 本部门及以下）
            rule.addDeptColumn(StudentDO.class, "dept_id");
            // 学生档案：学生 / 学委 / 纪委按 user_id 过滤（data_scope=5 仅本人）
            rule.addUserColumn(StudentDO.class, "user_id");
        };
    }

}
