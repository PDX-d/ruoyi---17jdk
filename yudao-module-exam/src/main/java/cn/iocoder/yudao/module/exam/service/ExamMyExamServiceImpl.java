package cn.iocoder.yudao.module.exam.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.exam.api.reference.ExamReferenceData.Catalog;
import cn.iocoder.yudao.module.exam.controller.admin.vo.ExamVO.*;
import cn.iocoder.yudao.module.exam.dal.dataobject.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import javax.validation.Validator;
import java.util.*;
import java.util.stream.Collectors;
import static cn.iocoder.yudao.module.exam.support.ExamException.require;

@Service
public class ExamMyExamServiceImpl implements ExamMyExamService {
    @Resource private ExamAccess access;
    @Resource private ExamStore store;
    @Resource private ExamViews views;
    @Resource private ExamReferences references;
    @Resource private Validator validator;

    @Override
    public PageResult<MyExamResp> page(ExamQueryReq request) {
        Long studentId = access.student("exam:my-exam:query");
        ExamRules.valid(validator, request);
        ExamRules.page(request);
        QueryWrapper<ExamSessionDO> query = views.query(request).inSql("id",
                "select session_id from exam_participant where tenant_id=" + ExamStore.tenant()
                        + " and deleted=0 and student_id=" + studentId
                        + (request.getClassId() == null ? "" : " and class_id=" + request.getClassId()));
        PageResult<ExamSessionDO> page = store.sessions.selectPage(request, query.orderByDesc("start_time", "id"));
        Catalog catalog = references.catalog();
        return new PageResult<>(page.getList().stream().map(row -> response(row, studentId, catalog))
                .collect(Collectors.toList()), page.getTotal());
    }

    @Override
    public MyExamResp get(Long id) {
        Long studentId = access.student("exam:my-exam:query");
        return response(store.session(id), studentId, references.catalog());
    }

    private MyExamResp response(ExamSessionDO session, Long studentId, Catalog catalog) {
        ExamParticipantDO participant = store.participants.selectOne(ExamStore.<ExamParticipantDO>query()
                .eq("session_id", session.getId()).eq("student_id", studentId));
        require(participant != null, "本人不在本场考试名册中");
        MyExamResp result = new MyExamResp();
        result.setId(session.getId());
        result.setName(session.getName());
        result.setCourseName(views.courseName(catalog, session.getCourseId()));
        result.setVenueName(session.getVenueName());
        result.setVenueAddress(session.getVenueAddress());
        result.setStartTime(session.getStartTime());
        result.setEndTime(session.getEndTime());
        result.setSignTime(participant.getSignTime());
        result.setCheckInOpenTime(session.getStartTime().minusMinutes(30));
        result.setServerTime(ExamRules.now());
        result.setArchived(references.semester(catalog, session.getSemesterId()).isArchived());
        return result;
    }
}
