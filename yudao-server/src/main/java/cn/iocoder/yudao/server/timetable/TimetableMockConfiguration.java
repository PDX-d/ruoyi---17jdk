package cn.iocoder.yudao.server.timetable;

import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.timetable.api.reference.ReferenceData;
import cn.iocoder.yudao.module.timetable.api.reference.TimetableReferenceGateway;
import cn.iocoder.yudao.module.timetable.support.TimetableException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

@Configuration
@Profile("local & !prod & !production")
@ConditionalOnProperty(prefix = "timetable.mock", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(TimetableMockProperties.class)
public class TimetableMockConfiguration {
    @Bean
    @ConditionalOnMissingBean(TimetableReferenceGateway.class)
    public TimetableReferenceGateway timetableMockGateway(TimetableMockProperties properties) {
        TimetableException.require(properties.getTenantId() != null && properties.getCurrentSemesterId() != null,
                "模拟模式必须显式指定测试租户和当前学期");
        return new TimetableReferenceGateway() {
            private void tenant() {
                TimetableException.require(Objects.equals(properties.getTenantId(), TenantContextHolder.getRequiredTenantId()),
                        "模拟公共数据未配置当前租户");
            }

            @Override
            public ReferenceData.Catalog getCatalog() {
                tenant();
                ReferenceData.Catalog catalog = new ReferenceData.Catalog();
                catalog.setMock(true);
                catalog.setCurrentSemesterId(properties.getCurrentSemesterId());
                catalog.setSemesters(new ArrayList<>(properties.getSemesters().values()));
                catalog.setCourses(new ArrayList<>(properties.getCourses().values()));
                catalog.setTeachers(new HashMap<>(properties.getTeachers()));
                catalog.setClasses(new HashMap<>(properties.getClasses()));
                return catalog;
            }

            @Override
            public ReferenceData.Semester getSemester(Long id) {
                tenant();
                return properties.getSemesters().get(id);
            }

            @Override
            public ReferenceData.Semester getCurrentSemester() {
                return getSemester(properties.getCurrentSemesterId());
            }

            @Override
            public ReferenceData.Course getCourse(Long id) {
                tenant();
                return properties.getCourses().get(id);
            }

            @Override
            public String getTeacherName(Long id) {
                tenant();
                return properties.getTeachers().get(id);
            }

            @Override
            public String getClassName(Long id) {
                tenant();
                return properties.getClasses().get(id);
            }

            @Override
            public ReferenceData.Actor getActor(Long accountId) {
                tenant();
                return properties.getAccounts().get(accountId);
            }

            @Override
            public List<ReferenceData.Student> getClassStudents(Long classId) {
                tenant();
                return properties.getStudents().get(classId);
            }
        };
    }
}
