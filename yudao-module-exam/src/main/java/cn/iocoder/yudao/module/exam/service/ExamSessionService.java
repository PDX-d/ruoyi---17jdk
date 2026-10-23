package cn.iocoder.yudao.module.exam.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.exam.controller.admin.vo.ExamVO.*;
import java.util.List;

public interface ExamSessionService {
    Long create(SessionCreateReq request);
    void update(SessionUpdateReq request);
    void delete(Long id, Long sessionVersion);
    SessionResp get(Long id);
    PageResult<SessionResp> page(ExamQueryReq request);
    List<ParticipantResp> participants(Long sessionId, Long classId);
}
