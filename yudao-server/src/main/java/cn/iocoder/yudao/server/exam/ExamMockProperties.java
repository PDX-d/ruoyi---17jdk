package cn.iocoder.yudao.server.exam;

import cn.iocoder.yudao.module.exam.api.reference.ExamReferenceData;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "exam.mock")
public class ExamMockProperties {
    private boolean enabled;
    private Long tenantId;
    private Map<Long, ExamReferenceData.Student> students = new HashMap<>();
    private List<ExamReferenceData.TeachingPair> teachingPairs = new ArrayList<>();
}

