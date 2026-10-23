package cn.iocoder.yudao.server.exam;

import cn.iocoder.yudao.module.exam.api.reference.ExamReferenceGateway;
import cn.iocoder.yudao.module.timetable.api.reference.TimetableReferenceGateway;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("local & !prod & !production")
@ConditionalOnProperty(prefix = "exam.mock", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(ExamMockProperties.class)
public class ExamMockConfiguration {
    @Bean
    @ConditionalOnMissingBean(ExamReferenceGateway.class)
    public ExamReferenceGateway examMockGateway(ExamMockProperties properties,
            ObjectProvider<TimetableReferenceGateway> timetableGateway) {
        return new ExamMockGateway(properties, timetableGateway);
    }
}

