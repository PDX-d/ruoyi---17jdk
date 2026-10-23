package cn.iocoder.yudao.module.exam.service;

import cn.iocoder.yudao.module.exam.api.reference.ExamReferenceData.*;
import cn.iocoder.yudao.module.exam.controller.admin.vo.ExamVO.*;
import cn.iocoder.yudao.module.exam.dal.dataobject.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;
import static cn.iocoder.yudao.module.exam.support.ExamException.require;

@Component
public class ExamViews {
    @Resource private ExamStore store;
    @Resource private ExamReferences references;

    public QueryWrapper<ExamSessionDO> query(ExamQueryReq request) {
        require(request != null, "查询条件不能为空");
        require(request.getStartDate() == null || request.getEndDate() == null
                || !request.getStartDate().isAfter(request.getEndDate()), "日期范围无效");
        QueryWrapper<ExamSessionDO> query = ExamStore.<ExamSessionDO>query()
                .eq(request.getSessionId() != null, "id", request.getSessionId())
                .eq(request.getSemesterId() != null, "semester_id", request.getSemesterId())
                .eq(request.getCourseId() != null, "course_id", request.getCourseId())
                .like(request.getName() != null, "name", request.getName());
        if (request.getStartDate() != null) {
            query.ge("start_time", request.getStartDate().atStartOfDay());
        }
        if (request.getEndDate() != null) {
            query.lt("start_time", request.getEndDate().plusDays(1).atStartOfDay());
        }
        if (request.getClassId() != null) {
            ExamRules.id(request.getClassId());
            query.inSql("id", "select session_id from exam_session_class where tenant_id=" + ExamStore.tenant()
                    + " and deleted=0 and class_id=" + request.getClassId());
        }
        return query;
    }

    public SessionResp session(ExamSessionDO session, ExamAccess.Scope scope, Catalog catalog) {
        SessionResp result = new SessionResp();
        BeanUtils.copyProperties(session, result);
        List<Long> classes = store.classes(session.getId()).stream().filter(id -> scope.canRead(session, id))
                .collect(Collectors.toList());
        result.setClassIds(classes);
        result.setClassNames(classes.stream().map(catalog.getClasses()::get).collect(Collectors.toList()));
        Semester semester = references.semester(catalog, session.getSemesterId());
        result.setSemesterName(semester.getName());
        result.setArchived(semester.isArchived());
        result.setCourseName(courseName(catalog, session.getCourseId()));
        result.setScoreItems(store.items(session.getId()).stream().map(this::item).collect(Collectors.toList()));
        result.setScoreConfigLocked(Boolean.TRUE.equals(session.getScoreConfigLocked()));
        result.setParticipantsLocked(!ExamRules.now().isBefore(session.getStartTime())
                || store.participants(session.getId()).stream().anyMatch(row -> row.getSignTime() != null || row.getFinalScore() != null));
        return result;
    }

    public String courseName(Catalog catalog, Long courseId) {
        Course course = catalog.getCourses().stream().filter(Objects::nonNull)
                .filter(row -> Objects.equals(courseId, row.getId())).findFirst().orElse(null);
        require(course != null && course.getName() != null, "公共课程名称缺失");
        return course.getName();
    }

    public ScoreItemResp item(ExamScoreItemDO item) {
        ScoreItemResp result = new ScoreItemResp();
        BeanUtils.copyProperties(item, result);
        return result;
    }

    public ParticipantResp participant(ExamParticipantDO participant) {
        ParticipantResp result = new ParticipantResp();
        BeanUtils.copyProperties(participant, result);
        return result;
    }

    public List<ExamScoreItemDO> scoreItems(List<ScoreItemReq> requests) {
        if (requests == null || requests.isEmpty()) {
            return Collections.emptyList();
        }
        require(requests.stream().allMatch(request -> request != null && request.getSort() != null), "成绩分项或顺序不能为空");
        return requests.stream().sorted(Comparator.comparing(ScoreItemReq::getSort)).map(request -> {
            ExamScoreItemDO item = new ExamScoreItemDO();
            BeanUtils.copyProperties(request, item);
            return item;
        }).collect(Collectors.toList());
    }
}
