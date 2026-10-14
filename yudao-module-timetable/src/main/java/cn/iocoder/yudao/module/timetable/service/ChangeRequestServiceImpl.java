package cn.iocoder.yudao.module.timetable.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.timetable.controller.admin.vo.*;
import cn.iocoder.yudao.module.timetable.dal.dataobject.ChangeRequestDO;
import cn.iocoder.yudao.module.timetable.dal.dataobject.LessonDO;
import cn.iocoder.yudao.module.timetable.dal.mysql.ChangeRequestMapper;
import cn.iocoder.yudao.module.timetable.support.TimetableException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ChangeRequestServiceImpl implements ChangeRequestService {
    @Resource private ChangeRequestMapper mapper;
    @Resource private LessonStore lessons;
    @Resource private LessonRules rules;
    @Resource private LessonConflicts conflicts;
    @Resource private LessonViews views;
    @Resource private TimetableAccess access;
    @Resource private ScheduleGuard guard;
    @Resource private TimetableNotifier notifier;

    private LambdaQueryWrapper<ChangeRequestDO> query() {
        return new LambdaQueryWrapper<ChangeRequestDO>()
                .eq(ChangeRequestDO::getTenantId, TenantContextHolder.getRequiredTenantId());
    }

    private ChangeRequestDO find(Long id) {
        TimetableException.require(id != null && id > 0, "申请 ID 无效");
        ChangeRequestDO change = mapper.selectOne(query().eq(ChangeRequestDO::getId, id));
        TimetableException.require(change != null, "申请不存在或不在可访问范围");
        return change;
    }

    private void futureNormal(LessonDO lesson) {
        TimetableException.require(lesson.getStatus() == 0 && lesson.getStartTime().isAfter(LocalDateTime.now()),
                "只能调整尚未开始的正常课次");
        rules.semester(lesson.getSemesterId(), true);
    }

    private LessonSaveReqVO target(LessonDO lesson, ChangeRequestDO change) {
        LessonSaveReqVO request = lessons.request(lesson);
        request.setStartTime(change.getTargetStartTime());
        request.setEndTime(change.getTargetEndTime());
        request.setClassroomId(change.getTargetClassroomId());
        rules.validate(request);
        TimetableException.require(request.getStartTime().isAfter(LocalDateTime.now()), "调课目标时间必须尚未开始");
        conflicts.requireFree(request);
        return request;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public Long create(ChangeCreateReqVO request) {
        access.permission("timetable:change-request:create");
        guard.lock();
        LessonDO lesson = lessons.find(request.getLessonId());
        access.teacher(lesson);
        futureNormal(lesson);
        lessons.noPending(lesson.getId());
        ChangeRequestDO change = new ChangeRequestDO();
        BeanUtils.copyProperties(request, change);
        TimetableException.require("CHANGE".equals(change.getType()) || "CANCEL".equals(change.getType()), "申请类型无效");
        if ("CHANGE".equals(change.getType())) {
            target(lesson, change);
        } else {
            TimetableException.require(change.getTargetStartTime() == null && change.getTargetEndTime() == null
                    && change.getTargetClassroomId() == null, "停课申请不能指定新时间或新教室");
        }
        change.setOriginalVersion(lesson.getVersion());
        change.setOriginalStartTime(lesson.getStartTime());
        change.setOriginalEndTime(lesson.getEndTime());
        change.setOriginalClassroomId(lesson.getClassroomId());
        change.setApplicantId(access.accountId());
        change.setStatus(0);
        change.setTenantId(TenantContextHolder.getRequiredTenantId());
        mapper.insert(change);
        return change.getId();
    }

    @Override
    public ChangeRespVO get(Long id) {
        access.permission("timetable:change-request:query");
        ChangeRequestDO change = find(id);
        if (!access.isAdmin()) {
            TimetableException.require(Objects.equals(change.getApplicantId(), access.accountId()), "只能查询本人提交的申请");
        }
        return response(change);
    }

    @Override
    public PageResult<ChangeRespVO> page(ChangePageReqVO request) {
        access.permission("timetable:change-request:query");
        TimetableException.require(request.getPageSize() > 0, "请使用有效分页大小");
        LambdaQueryWrapper<ChangeRequestDO> query = query()
                .eq(request.getStatus() != null, ChangeRequestDO::getStatus, request.getStatus())
                .eq(request.getLessonId() != null, ChangeRequestDO::getLessonId, request.getLessonId());
        if (!access.isAdmin()) {
            query.eq(ChangeRequestDO::getApplicantId, access.accountId());
        }
        PageResult<ChangeRequestDO> page = mapper.selectPage(request, query.orderByDesc(ChangeRequestDO::getId));
        return new PageResult<>(page.getList().stream().map(this::response).collect(Collectors.toList()), page.getTotal());
    }

    private void review(ChangeRequestDO change, ChangeReviewReqVO request, int status) {
        TimetableException.require(change.getStatus() == 0, "申请已处理，不能重复审批");
        change.setStatus(status);
        change.setReviewerId(access.accountId());
        change.setReviewTime(LocalDateTime.now());
        change.setReviewReason(request.getReason());
        TimetableException.require(mapper.update(change, new LambdaUpdateWrapper<ChangeRequestDO>()
                .eq(ChangeRequestDO::getTenantId, TenantContextHolder.getRequiredTenantId())
                .eq(ChangeRequestDO::getId, change.getId()).eq(ChangeRequestDO::getStatus, 0)) == 1,
                "申请已处理，不能重复审批");
    }

    @Override
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void approve(ChangeReviewReqVO request) {
        access.permission("timetable:change-request:approve");
        access.admin();
        guard.lock();
        ChangeRequestDO change = find(request.getId());
        TimetableException.require(change.getStatus() == 0, "申请已处理，不能重复审批");
        LessonDO lesson = lessons.find(change.getLessonId());
        futureNormal(lesson);
        TimetableException.require(Objects.equals(lesson.getVersion(), change.getOriginalVersion()),
                "原课次版本已变化，旧申请不能覆盖当前课表");
        Long version = lesson.getVersion();
        if ("CHANGE".equals(change.getType())) {
            LessonSaveReqVO target = target(lesson, change);
            lesson.setStartTime(target.getStartTime());
            lesson.setEndTime(target.getEndTime());
            lesson.setClassroomId(target.getClassroomId());
        } else {
            TimetableException.require("CANCEL".equals(change.getType()), "申请类型无效");
            lesson.setStatus(1);
        }
        lessons.update(lesson, version);
        review(change, request, 1);
        notifier.changed(views.response(lesson), change.getType());
    }

    @Override
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void reject(ChangeReviewReqVO request) {
        access.permission("timetable:change-request:reject");
        access.admin();
        guard.lock();
        ChangeRequestDO change = find(request.getId());
        rules.semester(lessons.find(change.getLessonId()).getSemesterId(), true);
        review(change, request, 2);
    }

    private ChangeRespVO response(ChangeRequestDO change) {
        ChangeRespVO result = new ChangeRespVO();
        BeanUtils.copyProperties(change, result);
        return result;
    }
}
