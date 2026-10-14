package cn.iocoder.yudao.module.timetable;

import cn.iocoder.yudao.module.system.api.notify.NotifyMessageSendApi;
import cn.iocoder.yudao.module.system.api.permission.PermissionApi;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.timetable.api.reference.TimetableReferenceGateway;
import cn.iocoder.yudao.module.timetable.dal.mysql.ClassroomMapper;
import cn.iocoder.yudao.module.timetable.service.*;
import cn.iocoder.yudao.module.timetable.service.export.TimetableExportService;
import cn.iocoder.yudao.module.timetable.service.reference.TimetableReferences;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.mockito.Mockito.mock;

@TestConfiguration
@EnableTransactionManagement
@MapperScan(basePackageClasses = ClassroomMapper.class)
@Import({ClassroomServiceImpl.class, LessonServiceImpl.class, ChangeRequestServiceImpl.class,
        LessonStore.class, LessonRelations.class, LessonRules.class, LessonConflicts.class,
        LessonViews.class, TimetableAccess.class, ScheduleGuard.class, TimetableNotifier.class,
        TimetableReferences.class, TimetableExportService.class})
public class TimetableTestConfiguration {
    @Bean
    public DataSource dataSource() {
        String url = "jdbc:h2:mem:timetable_test_" + UUID.randomUUID().toString().replace("-", "")
                + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
        DriverManagerDataSource source = new DriverManagerDataSource(url, "sa", "");
        new ResourceDatabasePopulator(new ClassPathResource("timetable-test-schema.sql")).execute(source);
        return source;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource source) {
        return new JdbcTemplate(source);
    }

    @Bean
    public PlatformTransactionManager transactionManager(DataSource source) {
        return new DataSourceTransactionManager(source);
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource source) throws Exception {
        MybatisSqlSessionFactoryBean factory = new MybatisSqlSessionFactoryBean();
        factory.setDataSource(source);
        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        factory.setConfiguration(configuration);
        GlobalConfig global = new GlobalConfig();
        global.setDbConfig(new GlobalConfig.DbConfig().setIdType(IdType.AUTO));
        global.setMetaObjectHandler(new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject meta) {
                setFieldValByName("createTime", LocalDateTime.now(), meta);
                setFieldValByName("updateTime", LocalDateTime.now(), meta);
                setFieldValByName("creator", "test", meta);
                setFieldValByName("updater", "test", meta);
            }

            @Override
            public void updateFill(MetaObject meta) {
                setFieldValByName("updateTime", LocalDateTime.now(), meta);
                setFieldValByName("updater", "test", meta);
            }
        });
        factory.setGlobalConfig(global);
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.H2));
        factory.setPlugins(interceptor);
        return factory.getObject();
    }

    @Bean
    public TimetableReferenceGateway referenceGateway() {
        return mock(TimetableReferenceGateway.class);
    }

    @Bean
    public PermissionApi permissionApi() {
        return mock(PermissionApi.class);
    }

    @Bean
    public AdminUserApi adminUserApi() {
        return mock(AdminUserApi.class);
    }

    @Bean
    public NotifyMessageSendApi notifyMessageSendApi() {
        return mock(NotifyMessageSendApi.class);
    }
}
