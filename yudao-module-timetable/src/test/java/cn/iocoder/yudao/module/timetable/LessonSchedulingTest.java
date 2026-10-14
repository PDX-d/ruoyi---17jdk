package cn.iocoder.yudao.module.timetable;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.timetable.controller.admin.vo.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DataIntegrityViolationException;
import java.time.LocalTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class LessonSchedulingTest extends TimetableIntegrationTestSupport {
    @ParameterizedTest
    @ValueSource(strings = {"TEACHER", "CLASSROOM", "CLASS"})
    void rejectsEachResourceConflictIndependently(String type) {
        lessons.create(lesson(9, 1L, room, 1L));
        LessonSaveReqVO request = lesson(9, 2L, createRoom("R2"), 2L);
        if ("TEACHER".equals(type)) request.setTeacherId(1L);
        if ("CLASSROOM".equals(type)) request.setClassroomId(room);
        if ("CLASS".equals(type)) request.setClassIds(Collections.singletonList(1L));
        List<ConflictRespVO> conflicts = lessons.checkConflict(request);
        assertEquals(1, conflicts.size());
        assertEquals(type, conflicts.get(0).getType());
        assertThrows(ServiceException.class, () -> lessons.create(request));
        assertEquals(1, lessonCount());
    }

    @Test
    void permitsAdjacentLessons() {
        lessons.create(lesson(9, 1L, room, 1L));
        assertTrue(lessons.checkConflict(lesson(10, 1L, room, 1L)).isEmpty());
        lessons.create(lesson(10, 1L, room, 1L));
        assertEquals(2, lessonCount());
    }

    @Test
    void detectsOverlappingSemesters() {
        lessons.create(lesson(9, 1L, room, 1L));
        LessonSaveReqVO request = lesson(9, 1L, room, 1L);
        request.setSemesterId(2L);
        assertThrows(ServiceException.class, () -> lessons.create(request));
        assertEquals(1, lessonCount());
    }

    @Test
    void ignoresCancelledAndDeletedLessonsForConflicts() {
        Long id = lessons.create(lesson(9, 1L, room, 1L));
        jdbc.update("UPDATE timetable_lesson SET status = 1 WHERE id = ?", id);
        assertTrue(lessons.checkConflict(lesson(9, 1L, room, 1L)).isEmpty());
        Long next = lessons.create(lesson(9, 1L, room, 1L));
        lessons.delete(next);
        assertTrue(lessons.checkConflict(lesson(9, 1L, room, 1L)).isEmpty());
    }

    @Test
    void repeatedlyReplacesCombinedClassRelationsWithoutDuplicates() {
        Long id = lessons.create(lesson(9, 1L, room, 1L));
        LessonSaveReqVO stale = store.request(store.find(id));
        for (int i = 0; i < 4; i++) {
            LessonSaveReqVO update = store.request(store.find(id));
            List<Long> classes = i % 2 == 0 ? Arrays.asList(1L, 2L) : Collections.singletonList(1L);
            update.setClassIds(classes);
            lessons.update(update);
            assertEquals(classes, lessons.get(id).getClassIds());
            assertEquals(Long.valueOf(i + 2), lessons.get(id).getVersion());
            assertEquals(classes.size(), jdbc.queryForObject(
                    "SELECT COUNT(*) FROM timetable_lesson_class WHERE lesson_id = ?", Integer.class, id));
        }
        assertThrows(ServiceException.class, () -> lessons.update(stale));
        assertEquals(Long.valueOf(5), lessons.get(id).getVersion());
    }

    @Test
    void createsBatchAndRejectsWholeConflictingBatch() {
        List<Long> created = lessons.batchCreate(batch(11));
        assertEquals(3, created.size());
        assertEquals(created, ids(lessons.calendar(query())));
        LessonBatchReqVO overlap = batch(11);
        overlap.setStartDate(day.minusDays(1));
        overlap.setWeekdays(new HashSet<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7)));
        assertThrows(ServiceException.class, () -> lessons.batchCreate(overlap));
        assertEquals(3, lessonCount());
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM timetable_lesson WHERE start_time < ?",
                Integer.class, day.atStartOfDay()));
    }

    @Test
    void rollsBackAlreadyInsertedBatchRowsWhenLaterInsertFails() {
        jdbc.execute("ALTER TABLE timetable_lesson ADD CONSTRAINT tt_test_fail_second CHECK"
                + " (EXTRACT(DAY FROM start_time) <> " + day.plusDays(1).getDayOfMonth() + ")");
        try {
            assertThrows(DataIntegrityViolationException.class, () -> lessons.batchCreate(batch(11)));
            assertEquals(0, lessonCount());
            assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM timetable_lesson_class", Integer.class));
        } finally {
            jdbc.execute("ALTER TABLE timetable_lesson DROP CONSTRAINT tt_test_fail_second");
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"duplicate-class", "teacher", "class", "course", "time", "semester", "archived"})
    void rejectsInvalidReferenceAndTimeData(String invalid) {
        LessonSaveReqVO request = lesson(9, 1L, room, 1L);
        switch (invalid) {
            case "duplicate-class": request.setClassIds(Arrays.asList(1L, 1L)); break;
            case "teacher": request.setTeacherId(99L); break;
            case "class": request.setClassIds(Collections.singletonList(99L)); break;
            case "course": request.setCourseId(99L); break;
            case "time": request.setEndTime(request.getStartTime()); break;
            case "semester":
                request.setStartTime(semester.getEndDate().plusDays(1).atTime(9, 0));
                request.setEndTime(request.getStartTime().plusHours(1));
                break;
            case "archived": request.setSemesterId(3L); break;
            default: fail("未知测试项");
        }
        assertThrows(ServiceException.class, () -> lessons.create(request));
        assertEquals(0, lessonCount());
    }

    @Test
    void rejectsExistingTeachersAndClassesOutsideCourseAssociations() {
        references.getCourse(1L).setTeacherIds(Collections.singleton(1L));
        references.getCourse(1L).setClassIds(Collections.singleton(1L));
        assertThrows(ServiceException.class, () -> lessons.create(lesson(9, 2L, room, 1L)));
        assertThrows(ServiceException.class, () -> lessons.create(lesson(9, 1L, room, 2L)));
        assertEquals(0, lessonCount());
    }

    @Test
    void archivedSemesterRemainsReadableButCannotBeChanged() {
        Long id = lessons.create(lesson(9, 1L, room, 1L));
        semester.setArchived(true);
        assertEquals(id, lessons.get(id).getId());
        assertThrows(ServiceException.class, () -> lessons.update(store.request(store.find(id))));
        assertThrows(ServiceException.class, () -> lessons.delete(id));
        assertEquals(1, lessonCount());
    }

    @Test
    void disabledClassroomPreservesHistoryAndCannotBeScheduledOrDeleted() {
        Long id = lessons.create(lesson(9, 1L, room, 1L));
        ClassroomSaveReqVO update = new ClassroomSaveReqVO();
        BeanUtils.copyProperties(classrooms.get(room), update);
        update.setStatus(1);
        classrooms.update(update);
        assertEquals(id, lessons.get(id).getId());
        assertThrows(ServiceException.class, () -> lessons.create(lesson(11, 1L, room, 1L)));
        assertThrows(ServiceException.class, () -> classrooms.delete(room));
        lessons.delete(id);
        assertThrows(ServiceException.class, () -> classrooms.delete(room));
    }

    @Test
    void deletesUnreferencedClassroomAndRejectsDuplicateCodes() {
        assertThrows(ServiceException.class, () -> createRoom("R1"));
        Long disposable = createRoom("unused");
        classrooms.delete(disposable);
        assertThrows(ServiceException.class, () -> classrooms.get(disposable));
    }

    private LessonBatchReqVO batch(int hour) {
        LessonBatchReqVO request = new LessonBatchReqVO();
        request.setSemesterId(1L);
        request.setCourseId(1L);
        request.setTeacherId(1L);
        request.setClassroomId(room);
        request.setClassIds(Collections.singletonList(1L));
        request.setStartDate(day);
        request.setEndDate(day.plusDays(2));
        request.setWeekdays(new HashSet<>(Arrays.asList(day.getDayOfWeek().getValue(),
                day.plusDays(1).getDayOfWeek().getValue(), day.plusDays(2).getDayOfWeek().getValue())));
        request.setStartTime(LocalTime.of(hour, 0));
        request.setEndTime(LocalTime.of(hour + 1, 0));
        return request;
    }
}
