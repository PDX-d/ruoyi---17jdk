package cn.iocoder.yudao.module.timetable;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.system.api.notify.dto.NotifySendSingleToUserReqDTO;
import cn.iocoder.yudao.module.timetable.controller.admin.vo.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ChangeRequestTransactionTest extends TimetableIntegrationTestSupport {
    @Test
    void pendingChangeDoesNotAlterFormalLessonAndApprovalNotifiesStudents() {
        Long lesson = lessons.create(lesson(9, 1L, room, 1L));
        Long request = apply(lesson, "CHANGE");
        assertEquals(day.atTime(9, 0), lessons.get(lesson).getStartTime());
        assertEquals(Long.valueOf(1), lessons.get(lesson).getVersion());
        verifyNoInteractions(messages);
        changes.approve(review(request));
        assertEquals(day.atTime(11, 0), lessons.get(lesson).getStartTime());
        assertEquals(Long.valueOf(2), lessons.get(lesson).getVersion());
        assertEquals(Integer.valueOf(1), changes.get(request).getStatus());
        ArgumentCaptor<NotifySendSingleToUserReqDTO> sent = ArgumentCaptor.forClass(NotifySendSingleToUserReqDTO.class);
        verify(messages, times(2)).sendSingleMessageToAdmin(sent.capture());
        assertEquals(new HashSet<>(Arrays.asList(4L, 7L)), sent.getAllValues().stream()
                .map(NotifySendSingleToUserReqDTO::getUserId).collect(Collectors.toSet()));
        assertTrue(sent.getAllValues().stream().allMatch(message ->
                "timetable_lesson_changed".equals(message.getTemplateCode())
                        && "调课".equals(message.getTemplateParams().get("change"))));
        assertEquals(2, notificationCount());
    }

    @Test
    void cancellationPreservesOriginalTimeAndDeduplicatesCombinedClassRecipients() {
        Long lesson = lessons.create(lesson(9, 1L, room, 1L, 2L));
        when(references.getClassStudents(2L)).thenReturn(Arrays.asList(student(101L, 4L), student(102L, 5L)));
        Long request = apply(lesson, "CANCEL");
        changes.approve(review(request));
        assertEquals(Integer.valueOf(1), lessons.get(lesson).getStatus());
        assertEquals(day.atTime(9, 0), lessons.get(lesson).getStartTime());
        assertEquals(Long.valueOf(2), lessons.get(lesson).getVersion());
        assertEquals(3, notificationCount());
        verify(messages, times(3)).sendSingleMessageToAdmin(any());
    }

    @Test
    void rollsBackLessonApprovalAndPreviouslyInsertedNotificationOnLaterFailure() {
        Long lesson = lessons.create(lesson(9, 1L, room, 1L));
        Long request = apply(lesson, "CHANGE");
        doThrow(new IllegalStateException("模拟第二个接收人发送失败"))
                .when(messages).sendSingleMessageToAdmin(argThat(message -> message.getUserId().equals(7L)));
        assertThrows(IllegalStateException.class, () -> changes.approve(review(request)));
        assertEquals(day.atTime(9, 0), lessons.get(lesson).getStartTime());
        assertEquals(Long.valueOf(1), lessons.get(lesson).getVersion());
        assertEquals(Integer.valueOf(0), changes.get(request).getStatus());
        assertEquals(0, notificationCount());
        verify(messages).sendSingleMessageToAdmin(argThat(message -> message.getUserId().equals(4L)));
    }

    @Test
    void disabledNotificationTemplateRollsBackCancellation() {
        Long lesson = lessons.create(lesson(9, 1L, room, 1L));
        Long request = apply(lesson, "CANCEL");
        doReturn(null).when(messages).sendSingleMessageToAdmin(any());
        assertThrows(ServiceException.class, () -> changes.approve(review(request)));
        assertEquals(Integer.valueOf(0), lessons.get(lesson).getStatus());
        assertEquals(Long.valueOf(1), lessons.get(lesson).getVersion());
        assertEquals(Integer.valueOf(0), changes.get(request).getStatus());
        assertEquals(0, notificationCount());
    }

    @Test
    void missingExplicitNotificationAccountRollsBackApproval() {
        Long lesson = lessons.create(lesson(9, 1L, room, 1L));
        Long request = apply(lesson, "CANCEL");
        when(references.getClassStudents(1L)).thenReturn(Collections.singletonList(student(101L, null)));
        assertThrows(ServiceException.class, () -> changes.approve(review(request)));
        assertEquals(Integer.valueOf(0), lessons.get(lesson).getStatus());
        assertEquals(Integer.valueOf(0), changes.get(request).getStatus());
        verifyNoInteractions(messages);
    }

    @Test
    void nonexistentNotificationAccountRollsBackApproval() {
        Long lesson = lessons.create(lesson(9, 1L, room, 1L));
        Long request = apply(lesson, "CANCEL");
        doReturn(Collections.emptyMap()).when(users).getUserMap(anyCollection());
        assertThrows(ServiceException.class, () -> changes.approve(review(request)));
        assertEquals(Integer.valueOf(0), lessons.get(lesson).getStatus());
        assertEquals(Long.valueOf(1), lessons.get(lesson).getVersion());
        assertEquals(Integer.valueOf(0), changes.get(request).getStatus());
        verifyNoInteractions(messages);
    }

    @Test
    void rejectsDuplicatePendingRequestAndPreventsDeletionUntilReviewed() {
        Long lesson = lessons.create(lesson(9, 1L, room, 1L));
        Long request = apply(lesson, "CANCEL");
        assertThrows(ServiceException.class, () -> lessons.delete(lesson));
        login(2L);
        assertThrows(ServiceException.class, () -> changes.create(application(lesson, "CANCEL")));
        login(1L);
        changes.reject(review(request));
        assertEquals(Integer.valueOf(2), changes.get(request).getStatus());
        assertEquals(Long.valueOf(1), lessons.get(lesson).getVersion());
        lessons.delete(lesson);
        assertEquals(0, lessonCount());
        assertEquals(0, notificationCount());
    }

    @Test
    void staleVersionCannotOverwriteUpdatedLesson() {
        Long lesson = lessons.create(lesson(9, 1L, room, 1L));
        Long request = apply(lesson, "CHANGE");
        LessonSaveReqVO update = store.request(store.find(lesson));
        update.setStartTime(day.atTime(10, 0));
        update.setEndTime(day.atTime(11, 0));
        lessons.update(update);
        assertThrows(ServiceException.class, () -> changes.approve(review(request)));
        assertEquals(day.atTime(10, 0), lessons.get(lesson).getStartTime());
        assertEquals(Long.valueOf(2), lessons.get(lesson).getVersion());
        assertEquals(Integer.valueOf(0), changes.get(request).getStatus());
        verifyNoInteractions(messages);
    }

    @Test
    void approvalRechecksNewConflicts() {
        Long lesson = lessons.create(lesson(9, 1L, room, 1L));
        Long request = apply(lesson, "CHANGE");
        lessons.create(lesson(11, 2L, room, 2L));
        assertThrows(ServiceException.class, () -> changes.approve(review(request)));
        assertEquals(day.atTime(9, 0), lessons.get(lesson).getStartTime());
        assertEquals(Integer.valueOf(0), changes.get(request).getStatus());
        verifyNoInteractions(messages);
    }

    @Test
    void processedRequestCannotBeReviewedAgain() {
        Long lesson = lessons.create(lesson(9, 1L, room, 1L));
        Long request = apply(lesson, "CANCEL");
        changes.approve(review(request));
        assertThrows(ServiceException.class, () -> changes.approve(review(request)));
        assertThrows(ServiceException.class, () -> changes.reject(review(request)));
        assertEquals(Long.valueOf(2), lessons.get(lesson).getVersion());
        assertEquals(2, notificationCount());
    }

    @Test
    void teacherCannotApplyOrReadAnotherTeachersRequestOrApproveOwnRequest() {
        Long own = lessons.create(lesson(9, 1L, room, 1L));
        Long other = lessons.create(lesson(11, 2L, room, 2L));
        Long request = apply(own, "CANCEL");
        login(3L);
        assertThrows(ServiceException.class, () -> changes.create(application(own, "CANCEL")));
        assertThrows(ServiceException.class, () -> changes.get(request));
        assertEquals(Long.valueOf(0), changes.page(new ChangePageReqVO()).getTotal());
        login(2L);
        assertThrows(ServiceException.class, () -> changes.create(application(other, "CANCEL")));
        assertThrows(ServiceException.class, () -> changes.approve(review(request)));
        assertEquals(Long.valueOf(1), changes.page(new ChangePageReqVO()).getTotal());
    }

    @Test
    void archivedOrAlreadyStartedLessonCannotBeAdjusted() {
        LessonSaveReqVO past = lesson(9, 1L, room, 1L);
        past.setStartTime(day.minusDays(6).atTime(9, 0));
        past.setEndTime(past.getStartTime().plusHours(1));
        Long pastId = lessons.create(past);
        Long future = lessons.create(lesson(9, 1L, room, 1L));
        Long request = apply(future, "CANCEL");
        login(2L);
        assertThrows(ServiceException.class, () -> changes.create(application(pastId, "CANCEL")));
        semester.setArchived(true);
        login(1L);
        assertThrows(ServiceException.class, () -> changes.approve(review(request)));
        assertEquals(Integer.valueOf(0), changes.get(request).getStatus());
    }

    private ChangeCreateReqVO application(Long lessonId, String type) {
        ChangeCreateReqVO request = new ChangeCreateReqVO();
        request.setLessonId(lessonId);
        request.setType(type);
        request.setReason("自动化测试申请");
        if ("CHANGE".equals(type)) {
            request.setTargetClassroomId(room);
            request.setTargetStartTime(day.atTime(11, 0));
            request.setTargetEndTime(day.atTime(12, 0));
        }
        return request;
    }

    private Long apply(Long lessonId, String type) {
        login(2L);
        Long id = changes.create(application(lessonId, type));
        login(1L);
        return id;
    }

    private int notificationCount() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM timetable_test_notification", Integer.class);
    }
}
