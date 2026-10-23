package cn.iocoder.yudao.module.exam.controller.admin.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;
import javax.validation.Valid;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public final class ExamVO {
    private ExamVO() {}

    @Data
    public static class Option {
        private Long id;
        private String name;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class SemesterOption extends Option {
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private LocalDate startDate;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private LocalDate endDate;
        private boolean archived;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class CourseOption extends Option {
        private List<Long> classIds;
    }

    @Data
    public static class ReferenceOptionsResp {
        private boolean mock;
        private Long currentSemesterId;
        private List<SemesterOption> semesters;
        private List<CourseOption> courses;
        private List<Option> classes;
    }

    @Data
    public static class StudentResp {
        private Long id;
        private String studentNo;
        private String name;
        private Long classId;
    }

    @Data
    public static class VenueSaveReq {
        @Positive private Long id;
        @PositiveOrZero private Long version;
        @NotBlank @Size(max = 100) private String name;
        @NotBlank @Size(max = 255) private String address;
        @NotNull @Min(1) private Integer capacity;
        @NotNull @DecimalMin("-90") @DecimalMax("90") @Digits(integer = 2, fraction = 7) private BigDecimal latitude;
        @NotNull @DecimalMin("-180") @DecimalMax("180") @Digits(integer = 3, fraction = 7) private BigDecimal longitude;
        @NotNull @DecimalMin(value = "0", inclusive = false) @DecimalMax("10000") @Digits(integer = 5, fraction = 2) private BigDecimal radiusMeters = new BigDecimal("100");
        @NotNull @Min(0) @Max(1) private Integer status;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class VenueResp extends VenueSaveReq {}

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class VenueQueryReq extends PageParam {
        private String name;
        @Min(0) @Max(1) private Integer status;
    }

    @Data
    public static class ScoreItemReq {
        @Positive private Long id;
        @NotBlank @Size(max = 50) private String name;
        @NotNull @DecimalMin("0") @DecimalMax("100") @Digits(integer = 3, fraction = 2) private BigDecimal weight;
        @NotNull @Min(0) private Integer sort;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class ScoreItemResp extends ScoreItemReq {
        private Long sessionId;
    }

    @Data
    public static class SessionBaseReq {
        @NotBlank @Size(max = 100) private String name;
        @NotNull @Positive private Long semesterId;
        @NotNull @Positive private Long courseId;
        @NotEmpty private List<@NotNull @Positive Long> classIds;
        @NotNull @Positive private Long venueId;
        @NotNull private LocalDateTime startTime;
        @NotNull private LocalDateTime endTime;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class SessionCreateReq extends SessionBaseReq {
        @Valid private List<ScoreItemReq> scoreItems;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class SessionUpdateReq extends SessionBaseReq {
        @NotNull @Positive private Long id;
        @NotNull @PositiveOrZero private Long sessionVersion;
        @NotNull private Boolean refreshParticipants = false;
    }

    @Data
    public static class SessionResp {
        private Long id;
        private String name;
        private Long semesterId;
        private String semesterName;
        private Long courseId;
        private String courseName;
        private List<Long> classIds;
        private List<String> classNames;
        private Long venueId;
        private String venueName;
        private String venueAddress;
        private Integer venueCapacity;
        private BigDecimal venueLatitude;
        private BigDecimal venueLongitude;
        private BigDecimal radiusMeters;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Long sessionVersion;
        private boolean scoreConfigLocked;
        private boolean archived;
        private boolean participantsLocked;
        private List<ScoreItemResp> scoreItems;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class ExamQueryReq extends PageParam {
        @Positive private Long semesterId;
        @Positive private Long sessionId;
        @Positive private Long courseId;
        @Positive private Long classId;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) private LocalDate startDate;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) private LocalDate endDate;
        private String name;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class ScoreQueryReq extends ExamQueryReq {
        private String studentNo;
        private String studentName;
    }

    @Data
    public static class ParticipantResp {
        private Long id;
        private Long sessionId;
        private Long studentId;
        private String studentNo;
        private String studentName;
        private Long classId;
        private String className;
        private LocalDateTime signTime;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class ScoreRowResp extends ParticipantResp {
        private String sessionName;
        private Long courseId;
        private String courseName;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Long sessionVersion;
        private Long rowVersion;
        private Map<Long, BigDecimal> scores;
        private BigDecimal finalScore;
    }

    @Data
    public static class ScoreRowSave {
        @NotNull @Positive private Long participantId;
        @NotNull @PositiveOrZero private Long rowVersion;
        @NotEmpty private Map<@NotNull @Positive Long, @NotNull @DecimalMin("0") @DecimalMax("100") @Digits(integer = 3, fraction = 2) BigDecimal> scores;
    }

    @Data
    public static class ScoreSaveReq {
        @NotNull @Positive private Long sessionId;
        @NotNull @PositiveOrZero private Long sessionVersion;
        @NotEmpty @Size(max = 5000) @Valid private List<ScoreRowSave> rows;
    }

    @Data
    public static class ScoreConfigReq {
        @NotNull @Positive private Long sessionId;
        @NotNull @PositiveOrZero private Long sessionVersion;
        @NotEmpty @Valid private List<ScoreItemReq> scoreItems;
    }

    @Data
    public static class ScoreBandResp {
        private String name;
        private long count;
    }

    @Data
    public static class StatisticsResp {
        private long participantCount;
        private long gradedCount;
        private long ungradedCount;
        private long passCount;
        private BigDecimal maxScore;
        private BigDecimal minScore;
        private BigDecimal averageScore;
        private BigDecimal passRate;
        private List<ScoreBandResp> bands;
    }

    @Data
    public static class ScoreSnapshotResp {
        private List<SessionResp> sessions;
        private List<ScoreItemResp> items;
        private List<ScoreRowResp> rows;
        private StatisticsResp statistics;
        private LocalDateTime generatedAt;
    }

    @Data
    public static class CheckInReq {
        @NotNull @Positive private Long sessionId;
        @NotNull @DecimalMin("-90") @DecimalMax("90") @Digits(integer = 2, fraction = 7) private BigDecimal latitude;
        @NotNull @DecimalMin("-180") @DecimalMax("180") @Digits(integer = 3, fraction = 7) private BigDecimal longitude;
        @NotNull @DecimalMin(value = "0", inclusive = false) @Digits(integer = 8, fraction = 2) private BigDecimal accuracy;
        @NotNull private LocalDateTime capturedAt;
    }

    @Data
    public static class CheckInResp {
        private Long participantId;
        private boolean checkedIn;
        private LocalDateTime signTime;
        private LocalDateTime serverTime;
    }

    @Data
    public static class MyExamResp {
        private Long id;
        private String name;
        private String courseName;
        private String venueName;
        private String venueAddress;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private LocalDateTime signTime;
        private LocalDateTime checkInOpenTime;
        private LocalDateTime serverTime;
        private boolean archived;
    }

    @Data
    public static class ImportRowError {
        private int row;
        private String message;
    }

    @Data
    public static class ImportResp {
        private int successCount;
        private List<ImportRowError> errors;
    }
}
