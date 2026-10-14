package cn.iocoder.yudao.module.timetable.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.timetable.api.reference.ReferenceData;
import cn.iocoder.yudao.module.timetable.controller.admin.vo.*;
import cn.iocoder.yudao.module.timetable.dal.dataobject.LessonDO;
import cn.iocoder.yudao.module.timetable.dal.mysql.LessonMapper;
import cn.iocoder.yudao.module.timetable.service.reference.TimetableReferences;
import cn.iocoder.yudao.module.timetable.support.TimetableException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LessonServiceImpl implements LessonService {
    @Resource private LessonMapper mapper;
    @Resource private LessonStore store;
    @Resource private LessonRelations relations;
    @Resource private LessonRules rules;
    @Resource private LessonConflicts conflicts;
    @Resource private TimetableAccess access;
    @Resource private ScheduleGuard guard;
    @Resource private LessonViews views;
    @Resource private ClassroomService classrooms;
    @Resource private TimetableReferences references;

    @Override
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public Long create(LessonSaveReqVO request) {
        access.permission("timetable:lesson:create");
        TimetableException.require(request.getId() == null && request.getVersion() == null, "创建课次不能指定 ID 或版本");
        access.maintain(request.getClassIds());
        guard.lock();
        rules.validate(request);
        conflicts.requireFree(request);
        return insert(request);
    }

    private Long insert(LessonSaveReqVO request) {
        LessonDO lesson = new LessonDO();
        BeanUtils.copyProperties(request, lesson);
        lesson.setStatus(0);
        lesson.setVersion(1L);
        lesson.setTenantId(TenantContextHolder.getRequiredTenantId());
        mapper.insert(lesson);
        relations.replace(lesson.getId(), request.getClassIds());
        return lesson.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public List<Long> batchCreate(LessonBatchReqVO request) {
        access.permission("timetable:lesson:create");
        access.maintain(request.getClassIds());
        guard.lock();
        ReferenceData.Semester semester = rules.semester(request.getSemesterId(), true);
        rules.range(semester, request.getStartDate(), request.getEndDate());
        TimetableException.require(ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) <= 366,
                "单次批量日期范围不能超过 367 天");
        TimetableException.require(request.getStartTime().isBefore(request.getEndTime()), "每日开始时间必须早于结束时间");
        List<LessonSaveReqVO> generated = new ArrayList<>();
        for (LocalDate date = request.getStartDate(); !date.isAfter(request.getEndDate()); date = date.plusDays(1)) {
            if (!request.getWeekdays().contains(date.getDayOfWeek().getValue())) {
                continue;
            }
            LessonSaveReqVO lesson = new LessonSaveReqVO();
            BeanUtils.copyProperties(request, lesson);
            lesson.setStartTime(date.atTime(request.getStartTime()));
            lesson.setEndTime(date.atTime(request.getEndTime()));
            rules.validate(lesson);
            generated.add(lesson);
        }
        TimetableException.require(!generated.isEmpty(), "日期范围和星期未生成任何课次");
        conflicts.requireBatchFree(generated);
        return generated.stream().map(this::insert).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void update(LessonSaveReqVO request) {
        access.permission("timetable:lesson:update");
        guard.lock();
        LessonDO lesson = store.find(request.getId());
        access.maintain(relations.getClassIds(lesson.getId()));
        access.maintain(request.getClassIds());
        TimetableException.require(Objects.equals(request.getVersion(), lesson.getVersion()), "课次版本已变化，请刷新后重试");
        TimetableException.require(lesson.getStatus() == 0, "停课课次不能直接修改");
        rules.semester(lesson.getSemesterId(), true);
        rules.validate(request);
        conflicts.requireFree(request);
        Long originalVersion = lesson.getVersion();
        BeanUtils.copyProperties(request, lesson);
        store.update(lesson, originalVersion);
        relations.replace(lesson.getId(), request.getClassIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void delete(Long id) {
        access.permission("timetable:lesson:delete");
        guard.lock();
        LessonDO lesson = store.find(id);
        access.maintain(relations.getClassIds(id));
        rules.semester(lesson.getSemesterId(), true);
        store.noPending(id);
        store.update(lesson, lesson.getVersion());
        mapper.delete(store.query().eq(LessonDO::getId, id));
        relations.replace(id, Collections.emptyList());
    }

    @Override
    public LessonRespVO get(Long id) {
        access.permission("timetable:lesson:query");
        LessonDO lesson = store.find(id);
        access.read(lesson, relations.getClassIds(id));
        return views.response(lesson);
    }

    private LambdaQueryWrapper<LessonDO> query(LessonQueryReqVO request, boolean explicitRange) {
        ReferenceData.Semester semester = rules.semester(request.getSemesterId(), false);
        request.setSemesterId(semester.getId());
        if (!explicitRange && request.getStartDate() == null && request.getEndDate() == null) {
            request.setStartDate(semester.getStartDate());
            request.setEndDate(semester.getEndDate());
        }
        rules.range(semester, request.getStartDate(), request.getEndDate());
        LambdaQueryWrapper<LessonDO> query = store.query()
                .eq(LessonDO::getSemesterId, semester.getId())
                .lt(LessonDO::getStartTime, request.getEndDate().plusDays(1).atStartOfDay())
                .gt(LessonDO::getEndTime, request.getStartDate().atStartOfDay())
                .eq(request.getStatus() != null, LessonDO::getStatus, request.getStatus());
        access.scope(query);
        if ("CLASS".equals(request.getDimension())) {
            TimetableException.require(request.getTargetId() != null, "班级维度需要目标班级 ID");
            access.classTarget(request.getTargetId());
            TimetableException.require(references.get().getClassName(request.getTargetId()) != null, "目标班级不存在");
            query.inSql(LessonDO::getId, relations.lessonIdsSql(Collections.singleton(request.getTargetId())));
        } else if ("CLASSROOM".equals(request.getDimension())) {
            classrooms.find(request.getTargetId());
            query.eq(LessonDO::getClassroomId, request.getTargetId());
        } else {
            TimetableException.require("SELF".equals(request.getDimension()) && request.getTargetId() == null,
                    "本人维度不能指定其他人的 ID");
        }
        return query.orderByAsc(LessonDO::getStartTime).orderByAsc(LessonDO::getId);
    }

    @Override
    public PageResult<LessonRespVO> page(LessonQueryReqVO request) {
        access.permission("timetable:lesson:query");
        TimetableException.require(request.getPageSize() > 0, "请使用有效分页大小");
        PageResult<LessonDO> page = mapper.selectPage(request, query(request, false));
        return new PageResult<>(views.responses(page.getList()), page.getTotal());
    }

    private List<LessonRespVO> list(LessonQueryReqVO request) {
        return views.responses(mapper.selectList(query(request, true)));
    }

    @Override
    public List<LessonRespVO> calendar(LessonQueryReqVO request) {
        access.permission("timetable:lesson:query");
        return list(request);
    }

    @Override
    public List<LessonRespVO> exportData(LessonQueryReqVO request) {
        access.permission("timetable:lesson:export");
        return list(request);
    }

    @Override
    public List<ConflictRespVO> checkConflict(LessonSaveReqVO request) {
        access.permission("timetable:lesson:query");
        if (request.getId() == null) {
            access.maintain(request.getClassIds());
        } else {
            LessonDO existing = store.find(request.getId());
            if (!access.isAdmin() && "TEACHER".equals(access.actor().getType())) {
                access.teacher(existing);
                TimetableException.require(Objects.equals(existing.getCourseId(), request.getCourseId())
                        && Objects.equals(existing.getTeacherId(), request.getTeacherId())
                        && Objects.equals(existing.getSemesterId(), request.getSemesterId())
                        && new HashSet<>(relations.getClassIds(existing.getId())).equals(new HashSet<>(request.getClassIds())),
                        "教师只能预检本人课次的时间和教室调整");
            } else {
                access.maintain(relations.getClassIds(existing.getId()));
                access.maintain(request.getClassIds());
            }
        }
        rules.validate(request);
        return conflicts.check(request);
    }
}
