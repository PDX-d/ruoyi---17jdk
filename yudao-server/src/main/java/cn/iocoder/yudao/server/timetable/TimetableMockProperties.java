package cn.iocoder.yudao.server.timetable;

import cn.iocoder.yudao.module.timetable.api.reference.ReferenceData;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "timetable.mock")
public class TimetableMockProperties {
    private boolean enabled;
    private Long tenantId;
    private Long currentSemesterId;
    private Map<Long, ReferenceData.Semester> semesters = new HashMap<>();
    private Map<Long, ReferenceData.Course> courses = new HashMap<>();
    private Map<Long, String> teachers = new HashMap<>();
    private Map<Long, String> classes = new HashMap<>();
    private Map<Long, ReferenceData.Actor> accounts = new HashMap<>();
    private Map<Long, List<ReferenceData.Student>> students = new HashMap<>();
}
