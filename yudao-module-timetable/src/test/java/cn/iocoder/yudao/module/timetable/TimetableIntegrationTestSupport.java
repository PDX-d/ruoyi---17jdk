package cn.iocoder.yudao.module.timetable;

import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.system.api.notify.NotifyMessageSendApi;
import cn.iocoder.yudao.module.system.api.notify.dto.NotifySendSingleToUserReqDTO;
import cn.iocoder.yudao.module.system.api.permission.PermissionApi;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import cn.iocoder.yudao.module.timetable.api.reference.ReferenceData;
import cn.iocoder.yudao.module.timetable.api.reference.TimetableReferenceGateway;
import cn.iocoder.yudao.module.timetable.controller.admin.vo.*;
import cn.iocoder.yudao.module.timetable.service.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TimetableTestConfiguration.class)
public abstract class TimetableIntegrationTestSupport {
    @Autowired protected LessonService lessons;
    @Autowired protected ClassroomService classrooms;
    @Autowired protected ChangeRequestService changes;
    @Autowired protected LessonStore store;
    @Autowired protected JdbcTemplate jdbc;
    @Autowired protected DataSource source;
    @Autowired protected PermissionApi permissions;
    @Autowired protected TimetableReferenceGateway references;
    @Autowired protected AdminUserApi users;
    @Autowired protected NotifyMessageSendApi messages;
    protected Long room;
    protected LocalDate day;
    protected ReferenceData.Semester semester;

    @BeforeEach
    void prepareIsolatedDatabase() throws Exception {
        try (Connection connection = source.getConnection()) {
            assertTrue(connection.getMetaData().getURL().startsWith("jdbc:h2:mem:timetable_test_"));
        }
        jdbc.update("DELETE FROM timetable_test_notification");
        jdbc.update("DELETE FROM timetable_change_request");
        jdbc.update("DELETE FROM timetable_lesson_class");
        jdbc.update("DELETE FROM timetable_lesson");
        jdbc.update("DELETE FROM timetable_classroom");
        jdbc.update("DELETE FROM timetable_schedule_guard");
        reset(permissions, references, users, messages);
        login(1L);
        day = LocalDate.now().plusDays(5);
        semester = semester(1L, false);
        when(references.getSemester(1L)).thenReturn(semester);
        when(references.getSemester(2L)).thenReturn(semester(2L, false));
        when(references.getSemester(3L)).thenReturn(semester(3L, true));
        when(references.getCurrentSemester()).thenReturn(semester);
        ReferenceData.Course course = new ReferenceData.Course();
        course.setId(1L);
        course.setName("程序设计课程");
        course.setTeacherIds(new HashSet<>(Arrays.asList(1L, 2L)));
        course.setClassIds(new HashSet<>(Arrays.asList(1L, 2L)));
        when(references.getCourse(1L)).thenReturn(course);
        when(references.getTeacherName(1L)).thenReturn("教师甲");
        when(references.getTeacherName(2L)).thenReturn("教师乙");
        when(references.getClassName(1L)).thenReturn("一班");
        when(references.getClassName(2L)).thenReturn("二班");
        when(references.getActor(2L)).thenReturn(actor("TEACHER", 1L));
        when(references.getActor(3L)).thenReturn(actor("TEACHER", 2L));
        when(references.getActor(4L)).thenReturn(actor("STUDENT", 101L, 1L));
        when(references.getActor(5L)).thenReturn(actor("STUDENT", 102L, 2L));
        when(references.getActor(6L)).thenReturn(actor("COUNSELOR", null, 1L));
        when(references.getClassStudents(1L)).thenReturn(Arrays.asList(student(101L, 4L), student(103L, 7L)));
        when(references.getClassStudents(2L)).thenReturn(Collections.singletonList(student(102L, 5L)));
        when(permissions.hasAnyRoles(anyLong(), eq("super_admin"))).thenAnswer(call -> call.getArgument(0).equals(1L));
        when(permissions.hasAnyPermissions(anyLong(), anyString())).thenAnswer(call ->
                call.getArgument(0).equals(1L) || !"timetable:admin".equals(call.getArgument(1)));
        when(users.getUserMap(anyCollection())).thenAnswer(call -> {
            Map<Long, AdminUserRespDTO> result = new HashMap<>();
            for (Long id : (Collection<Long>) call.getArgument(0)) {
                AdminUserRespDTO user = new AdminUserRespDTO();
                user.setId(id);
                result.put(id, user);
            }
            return result;
        });
        when(messages.sendSingleMessageToAdmin(any())).thenAnswer(call -> {
            NotifySendSingleToUserReqDTO request = call.getArgument(0);
            jdbc.update("INSERT INTO timetable_test_notification (user_id, template_code) VALUES (?, ?)",
                    request.getUserId(), request.getTemplateCode());
            return 1L;
        });
        room = createRoom("R1");
    }

    @AfterEach
    void clearIdentity() {
        SecurityContextHolder.clearContext();
        TenantContextHolder.clear();
    }

    protected void login(Long account) {
        TenantContextHolder.setTenantId(1L);
        LoginUser user = new LoginUser();
        user.setId(account);
        user.setUserType(2);
        user.setTenantId(1L);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                user, null, Collections.emptyList()));
    }

    protected Long createRoom(String code) {
        ClassroomSaveReqVO request = new ClassroomSaveReqVO();
        request.setCode(code);
        request.setName("测试教室" + code);
        request.setLocation("测试教学楼");
        request.setCapacity(40);
        request.setStatus(0);
        return classrooms.create(request);
    }

    protected LessonSaveReqVO lesson(int hour, Long teacher, Long classroom, Long... classIds) {
        LessonSaveReqVO request = new LessonSaveReqVO();
        request.setSemesterId(1L);
        request.setCourseId(1L);
        request.setTeacherId(teacher);
        request.setClassroomId(classroom);
        request.setClassIds(Arrays.asList(classIds));
        request.setStartTime(day.atTime(hour, 0));
        request.setEndTime(day.atTime(hour + 1, 0));
        return request;
    }

    protected LessonQueryReqVO query() {
        LessonQueryReqVO request = new LessonQueryReqVO();
        request.setSemesterId(1L);
        request.setStartDate(day);
        request.setEndDate(day.plusDays(6));
        return request;
    }

    protected ChangeReviewReqVO review(Long id) {
        ChangeReviewReqVO request = new ChangeReviewReqVO();
        request.setId(id);
        request.setReason("测试审批");
        return request;
    }

    protected int lessonCount() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM timetable_lesson WHERE deleted = FALSE", Integer.class);
    }

    protected List<Long> ids(List<LessonRespVO> lessons) {
        return lessons.stream().map(LessonRespVO::getId).collect(Collectors.toList());
    }

    private ReferenceData.Semester semester(Long id, boolean archived) {
        ReferenceData.Semester value = new ReferenceData.Semester();
        value.setId(id);
        value.setName("测试学期");
        value.setStartDate(LocalDate.now().minusDays(30));
        value.setEndDate(LocalDate.now().plusDays(60));
        value.setArchived(archived);
        return value;
    }

    private ReferenceData.Actor actor(String type, Long identity, Long... classes) {
        ReferenceData.Actor value = new ReferenceData.Actor();
        value.setType(type);
        value.setTeacherId("TEACHER".equals(type) ? identity : null);
        value.setStudentId("STUDENT".equals(type) ? identity : null);
        value.setClassIds(new HashSet<>(Arrays.asList(classes)));
        return value;
    }

    protected ReferenceData.Student student(Long id, Long account) {
        ReferenceData.Student value = new ReferenceData.Student();
        value.setId(id);
        value.setAccountId(account);
        return value;
    }
}
