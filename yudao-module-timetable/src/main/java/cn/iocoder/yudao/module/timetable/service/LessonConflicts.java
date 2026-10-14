package cn.iocoder.yudao.module.timetable.service;

import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.timetable.controller.admin.vo.ConflictRespVO;
import cn.iocoder.yudao.module.timetable.controller.admin.vo.LessonSaveReqVO;
import cn.iocoder.yudao.module.timetable.dal.dataobject.LessonDO;
import cn.iocoder.yudao.module.timetable.dal.mysql.LessonMapper;
import cn.iocoder.yudao.module.timetable.support.TimetableException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.*;

@Component
public class LessonConflicts {
    @Resource private LessonMapper mapper;
    @Resource private LessonRelations relations;
    @Resource private TimetableAccess access;

    public List<ConflictRespVO> check(LessonSaveReqVO request) {
        List<LessonDO> candidates = mapper.selectList(new LambdaQueryWrapper<LessonDO>()
                .eq(LessonDO::getTenantId, TenantContextHolder.getRequiredTenantId())
                .eq(LessonDO::getStatus, 0).ne(request.getId() != null, LessonDO::getId, request.getId())
                .lt(LessonDO::getStartTime, request.getEndTime()).gt(LessonDO::getEndTime, request.getStartTime())
                .orderByAsc(LessonDO::getStartTime).orderByAsc(LessonDO::getId));
        List<ConflictRespVO> results = new ArrayList<>();
        for (LessonDO lesson : candidates) {
            List<Long> classIds = relations.getClassIds(lesson.getId());
            boolean visible = access.canRead(lesson, classIds);
            if (Objects.equals(request.getTeacherId(), lesson.getTeacherId())) {
                results.add(conflict("TEACHER", lesson, null, visible));
            }
            if (Objects.equals(request.getClassroomId(), lesson.getClassroomId())) {
                results.add(conflict("CLASSROOM", lesson, null, visible));
            }
            for (Long classId : classIds) {
                if (request.getClassIds().contains(classId)) {
                    results.add(conflict("CLASS", lesson, classId, visible));
                }
            }
        }
        return results;
    }

    public void requireFree(LessonSaveReqVO request) {
        List<ConflictRespVO> conflicts = check(request);
        TimetableException.require(conflicts.isEmpty(), conflicts.isEmpty() ? "排课冲突" :
                "排课冲突：" + conflicts.get(0).getType() + " " + conflicts.get(0).getStartTime()
                        + " 至 " + conflicts.get(0).getEndTime());
    }

    public void requireBatchFree(List<LessonSaveReqVO> requests) {
        for (int i = 0; i < requests.size(); i++) {
            LessonSaveReqVO first = requests.get(i);
            requireFree(first);
            for (int j = 0; j < i; j++) {
                LessonSaveReqVO other = requests.get(j);
                boolean overlap = first.getStartTime().isBefore(other.getEndTime())
                        && first.getEndTime().isAfter(other.getStartTime());
                boolean resource = Objects.equals(first.getTeacherId(), other.getTeacherId())
                        || Objects.equals(first.getClassroomId(), other.getClassroomId())
                        || !Collections.disjoint(first.getClassIds(), other.getClassIds());
                TimetableException.require(!overlap || !resource, "批量排课内部存在教师、教室或班级冲突");
            }
        }
    }

    private ConflictRespVO conflict(String type, LessonDO lesson, Long classId, boolean visible) {
        ConflictRespVO result = new ConflictRespVO();
        result.setType(type);
        result.setStartTime(lesson.getStartTime());
        result.setEndTime(lesson.getEndTime());
        if (visible) {
            result.setLessonId(lesson.getId());
            result.setTeacherId(lesson.getTeacherId());
            result.setClassroomId(lesson.getClassroomId());
            result.setClassId(classId);
        }
        return result;
    }
}
