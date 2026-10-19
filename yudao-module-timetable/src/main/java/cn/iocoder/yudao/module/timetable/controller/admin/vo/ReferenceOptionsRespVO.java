package cn.iocoder.yudao.module.timetable.controller.admin.vo;

import cn.iocoder.yudao.module.timetable.api.reference.ReferenceData;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Data
@Schema(description = "课程表公共选项及本人业务身份")
public class ReferenceOptionsRespVO {
    @Schema(description = "是否为本地模拟公共数据")
    private boolean mock;
    private Long currentSemesterId;
    @Schema(description = "本人身份，类型为 ADMIN、COUNSELOR、TEACHER 或 STUDENT；不是权限授权依据")
    private ReferenceData.Actor actor;
    private List<SemesterOption> semesters;
    private List<CourseOption> courses;
    private List<NamedOption> teachers;
    private List<NamedOption> classes;

    @Data
    public static class SemesterOption {
        private Long id;
        private String name;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private LocalDate startDate;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private LocalDate endDate;
        private boolean archived;
    }

    @Data
    public static class CourseOption {
        private Long id;
        private String name;
        private Set<Long> teacherIds;
        private Set<Long> classIds;
    }

    @Data
    public static class NamedOption {
        private Long id;
        private String name;
    }
}
