package cn.iocoder.yudao.module.exam.api.reference;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ExamReferenceData {
    private ExamReferenceData() {
    }

    @Data
    public static class Catalog {
        private boolean mock;
        private Long currentSemesterId;
        private List<Semester> semesters = Collections.emptyList();
        private List<Course> courses = Collections.emptyList();
        private Map<Long, String> classes = Collections.emptyMap();
        private List<TeachingPair> teachingPairs = Collections.emptyList();
    }

    @Data
    public static class Semester {
        private Long id;
        private String name;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate startDate;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate endDate;
        private boolean archived;
    }

    @Data
    public static class Course {
        private Long id;
        private String name;
        private Set<Long> classIds = Collections.emptySet();
    }

    @Data
    public static class TeachingPair {
        private Long semesterId;
        private Long teacherId;
        private Long courseId;
        private Long classId;
    }

    @Data
    public static class Actor {
        private String type;
        private Long teacherId;
        private Long studentId;
        private Set<Long> classIds = Collections.emptySet();
    }

    @Data
    public static class Student {
        private Long id;
        private String name;
        private String studentNo;
        private Long classId;
    }
}

