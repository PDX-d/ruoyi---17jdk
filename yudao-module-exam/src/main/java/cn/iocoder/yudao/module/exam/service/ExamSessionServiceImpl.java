package cn.iocoder.yudao.module.exam.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.exam.api.reference.ExamReferenceData.*;
import cn.iocoder.yudao.module.exam.controller.admin.vo.ExamVO.*;
import cn.iocoder.yudao.module.exam.dal.dataobject.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import javax.validation.Validator;
import java.util.*;
import java.util.stream.Collectors;
import static cn.iocoder.yudao.module.exam.support.ExamException.require;

@Service
public class ExamSessionServiceImpl implements ExamSessionService {
    @Resource private ExamStore store;
    @Resource private ExamAccess access;
    @Resource private ExamGuard guard;
    @Resource private ExamReferences references;
    @Resource private ExamRoster roster;
    @Resource private ExamConflicts conflicts;
    @Resource private ExamViews views;
    @Resource private ExamScoreConfiguration configuration;
    @Resource private Validator validator;

    @Override
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public Long create(SessionCreateReq request) {
        access.permission("exam:session:create");
        ExamRules.valid(validator, request);
        guard.lock();
        ExamVenueDO venue = store.venue(request.getVenueId(), true);
        access.permission("exam:session:create");
        access.maintain(request.getClassIds());
        references.selection(request.getSemesterId(), request.getCourseId(), request.getClassIds());
        references.timeRange(request.getSemesterId(), request.getStartTime(), request.getEndTime());
        require(request.getStartTime().isAfter(ExamRules.now()), "只能创建尚未开始的考试");
        List<Student> students = references.students(request.getClassIds());
        ExamSessionDO session = new ExamSessionDO();
        BeanUtils.copyProperties(request, session);
        session.setTenantId(ExamStore.tenant());
        session.setSessionVersion(0L);
        session.setScoreConfigLocked(false);
        store.copyVenue(session, venue);
        conflicts.check(session, request.getClassIds(), students.stream().map(Student::getId).collect(Collectors.toList()));
        store.sessions.insert(session);
        roster.replace(session.getId(), request.getClassIds(), students);
        List<ExamScoreItemDO> items = views.scoreItems(request.getScoreItems());
        configuration.replace(session.getId(), items.isEmpty() ? configuration.defaults() : items);
        require(session.getStartTime().isAfter(ExamRules.now()), "考试已开始，本次创建已回滚");
        return session.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void update(SessionUpdateReq request) {
        access.permission("exam:session:update");
        ExamRules.valid(validator, request);
        guard.lock();
        ExamSessionDO before = store.session(request.getId());
        ExamVenueDO venue = Objects.equals(before.getVenueId(), request.getVenueId())
                ? null : store.venue(request.getVenueId(), true);
        ExamSessionDO session = store.lockSession(request.getId());
        access.permission("exam:session:update");
        ExamRules.version(request.getSessionVersion(), session.getSessionVersion());
        List<Long> oldClasses = store.classes(session.getId());
        access.maintain(oldClasses);
        access.maintain(request.getClassIds());
        references.writable(session.getSemesterId());
        references.selection(request.getSemesterId(), request.getCourseId(), request.getClassIds());
        references.timeRange(request.getSemesterId(), request.getStartTime(), request.getEndTime());
        store.mutable(session);
        java.time.LocalDateTime originalStart = session.getStartTime();
        require(request.getStartTime().isAfter(ExamRules.now()), "考试开始时间必须晚于当前时间");
        boolean refresh = Boolean.TRUE.equals(request.getRefreshParticipants())
                || !Objects.equals(session.getSemesterId(), request.getSemesterId())
                || !Objects.equals(session.getCourseId(), request.getCourseId())
                || !new HashSet<>(oldClasses).equals(new HashSet<>(request.getClassIds()));
        List<Student> students = refresh ? references.students(request.getClassIds()) : Collections.emptyList();
        List<Long> studentIds = refresh ? students.stream().map(Student::getId).collect(Collectors.toList())
                : store.participants(session.getId()).stream().map(ExamParticipantDO::getStudentId).collect(Collectors.toList());
        BeanUtils.copyProperties(request, session);
        if (venue != null) {
            store.copyVenue(session, venue);
        }
        conflicts.check(session, request.getClassIds(), studentIds);
        if (refresh) {
            roster.replace(session.getId(), request.getClassIds(), students);
        }
        require(ExamRules.now().isBefore(originalStart) && request.getStartTime().isAfter(ExamRules.now()),
                "考试已开始，本次修改已回滚");
        store.updateSession(session, request.getSessionVersion());
    }

    @Override
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void delete(Long id, Long sessionVersion) {
        access.permission("exam:session:delete");
        guard.lock();
        ExamSessionDO session = store.lockSession(id);
        access.permission("exam:session:delete");
        ExamRules.version(sessionVersion, session.getSessionVersion());
        access.maintain(store.classes(id));
        references.writable(session.getSemesterId());
        store.mutable(session);
        roster.delete(id);
        require(ExamRules.now().isBefore(session.getStartTime()), "考试已开始，本次删除已回滚");
        require(store.sessions.delete(ExamStore.<ExamSessionDO>query().eq("id", id)
                .eq("session_version", sessionVersion)) == 1, "考试版本已变化");
    }

    @Override
    public SessionResp get(Long id) {
        ExamAccess.Scope scope = access.staff("exam:session:query");
        ExamSessionDO session = store.session(id);
        store.read(scope, session);
        return views.session(session, scope, references.catalog());
    }

    @Override
    public PageResult<SessionResp> page(ExamQueryReq request) {
        ExamAccess.Scope scope = access.staff("exam:session:query");
        ExamRules.valid(validator, request);
        ExamRules.page(request);
        QueryWrapper<ExamSessionDO> query = views.query(request);
        scope.sessionQuery(query, request.getClassId());
        PageResult<ExamSessionDO> result = store.sessions.selectPage(request, query.orderByDesc("start_time", "id"));
        Catalog catalog = references.catalog();
        return new PageResult<>(result.getList().stream().map(row -> views.session(row, scope, catalog))
                .collect(Collectors.toList()), result.getTotal());
    }

    @Override
    public List<ParticipantResp> participants(Long sessionId, Long classId) {
        ExamAccess.Scope scope = access.staff("exam:session:query");
        ExamSessionDO session = store.session(sessionId);
        store.read(scope, session);
        return store.participants(sessionId).stream()
                .filter(row -> (classId == null || classId.equals(row.getClassId())) && scope.canRead(session, row.getClassId()))
                .map(views::participant).collect(Collectors.toList());
    }
}
