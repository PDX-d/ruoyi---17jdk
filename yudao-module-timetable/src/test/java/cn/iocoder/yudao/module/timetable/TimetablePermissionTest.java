package cn.iocoder.yudao.module.timetable;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.timetable.controller.admin.vo.*;
import cn.iocoder.yudao.module.timetable.service.ScheduleGuard;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TimetablePermissionTest extends TimetableIntegrationTestSupport {
    @Autowired private ScheduleGuard guard;
    @Autowired private PlatformTransactionManager transactions;

    @ParameterizedTest
    @ValueSource(longs = {2L, 4L, 6L})
    void filtersDetailsCalendarPageAndExportForEachIdentity(long account) {
        Long own = lessons.create(lesson(9, 1L, room, 1L));
        Long other = lessons.create(lesson(11, 2L, room, 2L));
        login(account);
        assertEquals(own, lessons.get(own).getId());
        assertThrows(ServiceException.class, () -> lessons.get(other));
        assertEquals(Collections.singletonList(own), ids(lessons.calendar(query())));
        assertEquals(Collections.singletonList(own), ids(lessons.exportData(query())));
        assertEquals(Long.valueOf(1), lessons.page(query()).getTotal());
        assertEquals(Collections.singletonList(own), ids(lessons.page(query()).getList()));
        LessonQueryReqVO classroom = query();
        classroom.setDimension("CLASSROOM");
        classroom.setTargetId(room);
        assertEquals(Collections.singletonList(own), ids(lessons.calendar(classroom)));
    }

    @Test
    void studentCannotForgeClassOrSelfTarget() {
        lessons.create(lesson(9, 1L, room, 1L));
        login(4L);
        LessonQueryReqVO request = query();
        request.setDimension("CLASS");
        request.setTargetId(2L);
        assertThrows(ServiceException.class, () -> lessons.calendar(request));
        assertThrows(ServiceException.class, () -> lessons.exportData(request));
        request.setDimension("SELF");
        request.setTargetId(2L);
        assertThrows(ServiceException.class, () -> lessons.calendar(request));
        assertThrows(ServiceException.class, () -> lessons.create(lesson(11, 1L, room, 1L)));
    }

    @Test
    void teacherCannotDirectlyMaintainFormalLessonsEvenWithOperationPermission() {
        Long id = lessons.create(lesson(9, 1L, room, 1L));
        LessonSaveReqVO update = store.request(store.find(id));
        login(2L);
        assertThrows(ServiceException.class, () -> lessons.create(lesson(11, 1L, room, 1L)));
        assertThrows(ServiceException.class, () -> lessons.update(update));
        assertThrows(ServiceException.class, () -> lessons.delete(id));
    }

    @Test
    void counselorRequiresAllCombinedClassesToBeInScope() {
        Long combined = lessons.create(lesson(9, 1L, room, 1L, 2L));
        LessonSaveReqVO update = store.request(store.find(combined));
        update.setClassIds(Collections.singletonList(1L));
        login(6L);
        assertThrows(ServiceException.class, () -> lessons.update(update));
        assertThrows(ServiceException.class, () -> lessons.delete(combined));
        assertThrows(ServiceException.class, () -> lessons.create(lesson(11, 1L, room, 1L, 2L)));
        Long own = lessons.create(lesson(11, 1L, room, 1L));
        LessonSaveReqVO ownUpdate = store.request(store.find(own));
        ownUpdate.setClassIds(Arrays.asList(1L, 2L));
        assertThrows(ServiceException.class, () -> lessons.update(ownUpdate));
        assertEquals(Collections.singletonList(1L), lessons.get(own).getClassIds());
    }

    @Test
    void hidesOutOfScopeConflictDetails() {
        Long hidden = lessons.create(lesson(9, 2L, room, 2L));
        login(6L);
        List<ConflictRespVO> conflicts = lessons.checkConflict(lesson(9, 1L, room, 1L));
        assertEquals(1, conflicts.size());
        ConflictRespVO result = conflicts.get(0);
        assertEquals("CLASSROOM", result.getType());
        assertNotNull(result.getStartTime());
        assertNotNull(result.getEndTime());
        assertNull(result.getLessonId());
        assertNull(result.getTeacherId());
        assertNull(result.getClassroomId());
        assertNull(result.getClassId());
        assertThrows(ServiceException.class, () -> lessons.get(hidden));
    }

    @Test
    void tenantCannotReadOrConflictWithAnotherTenant() {
        Long id = lessons.create(lesson(9, 1L, room, 1L));
        TenantContextHolder.setTenantId(2L);
        assertThrows(ServiceException.class, () -> lessons.get(id));
        assertTrue(lessons.calendar(query()).isEmpty());
        assertEquals(Long.valueOf(0), lessons.page(query()).getTotal());
        Long tenantRoom = createRoom("R1");
        lessons.create(lesson(9, 1L, tenantRoom, 1L));
        assertEquals(1, lessons.calendar(query()).size());
    }

    @Test
    void missingIdentityAndMissingOperationPermissionFailClosed() {
        Long id = lessons.create(lesson(9, 1L, room, 1L));
        login(99L);
        assertThrows(ServiceException.class, () -> lessons.get(id));
        login(2L);
        when(permissions.hasAnyPermissions(2L, "timetable:lesson:query")).thenReturn(false);
        assertThrows(ServiceException.class, () -> lessons.get(id));
        SecurityContextHolder.clearContext();
        assertThrows(ServiceException.class, () -> lessons.calendar(query()));
    }

    @Test
    void guardRejectsMissingOldSnapshotAndReadOnlyTransactions() {
        assertThrows(ServiceException.class, () -> guard.lock());
        TransactionTemplate oldSnapshot = new TransactionTemplate(transactions);
        oldSnapshot.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
        assertThrows(ServiceException.class, () -> oldSnapshot.execute(status -> { guard.lock(); return null; }));
        TransactionTemplate readOnly = new TransactionTemplate(transactions);
        readOnly.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        readOnly.setReadOnly(true);
        assertThrows(ServiceException.class, () -> readOnly.execute(status -> { guard.lock(); return null; }));
    }
}
