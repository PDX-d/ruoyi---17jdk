package cn.iocoder.yudao.module.exam.service;

import cn.iocoder.yudao.module.exam.controller.admin.vo.ExamVO.*;
import cn.iocoder.yudao.module.exam.dal.dataobject.*;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import javax.validation.Validator;
import java.time.LocalDateTime;
import static cn.iocoder.yudao.module.exam.support.ExamException.require;

@Service
public class ExamCheckInServiceImpl implements ExamCheckInService {
    @Resource private ExamStore store;
    @Resource private ExamAccess access;
    @Resource private ExamReferences references;
    @Resource private Validator validator;

    @Override
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public CheckInResp create(CheckInReq request) {
        Long studentId = access.student("exam:check-in:create");
        ExamRules.valid(validator, request);
        ExamSessionDO session = store.lockSession(request.getSessionId());
        require(studentId.equals(access.student("exam:check-in:create")), "学生绑定已变化");
        references.writable(session.getSemesterId());
        ExamParticipantDO participant = store.participants.selectOne(ExamStore.<ExamParticipantDO>query()
                .eq("session_id", session.getId()).eq("student_id", studentId));
        require(participant != null, "本人不在本场考试名册中");
        participant = store.participants.lock(ExamStore.tenant(), participant.getId());
        LocalDateTime now = ExamRules.now();
        if (participant.getSignTime() == null) {
            require(!now.isBefore(session.getStartTime().minusMinutes(30)) && now.isBefore(session.getStartTime()),
                    "不在考试开始前30分钟签到窗口内");
            require(!request.getCapturedAt().isAfter(now) && !request.getCapturedAt().isBefore(now.minusSeconds(60)),
                    "定位已过期或定位时间在未来");
            ExamRules.coordinates(request.getLatitude(), request.getLongitude());
            require(request.getAccuracy().signum() > 0 && request.getAccuracy().compareTo(session.getRadiusMeters()) <= 0,
                    "定位精度不足");
            require(distance(request, session) <= session.getRadiusMeters().doubleValue(), "不在考场签到范围内");
            require(store.participants.update(null, new UpdateWrapper<ExamParticipantDO>()
                    .eq("tenant_id", ExamStore.tenant()).eq("deleted", false).eq("id", participant.getId())
                    .isNull("sign_time").set("sign_time", now).set("sign_latitude", request.getLatitude())
                    .set("sign_longitude", request.getLongitude()).set("sign_accuracy", request.getAccuracy())
                    .set("position_time", request.getCapturedAt())) == 1, "签到状态已变化");
            participant.setSignTime(now);
        }
        CheckInResp result = new CheckInResp();
        result.setParticipantId(participant.getId());
        result.setCheckedIn(true);
        result.setSignTime(participant.getSignTime());
        result.setServerTime(now);
        return result;
    }

    private double distance(CheckInReq request, ExamSessionDO session) {
        double lat1 = Math.toRadians(session.getVenueLatitude().doubleValue());
        double lat2 = Math.toRadians(request.getLatitude().doubleValue());
        double deltaLat = lat2 - lat1;
        double deltaLon = Math.toRadians(request.getLongitude().doubleValue() - session.getVenueLongitude().doubleValue());
        double a = Math.pow(Math.sin(deltaLat / 2), 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(deltaLon / 2), 2);
        return 6371000D * 2 * Math.asin(Math.sqrt(Math.min(1D, Math.max(0D, a))));
    }
}
