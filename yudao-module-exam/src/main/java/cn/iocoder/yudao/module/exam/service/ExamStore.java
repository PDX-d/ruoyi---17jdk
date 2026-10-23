package cn.iocoder.yudao.module.exam.service;

import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.exam.dal.dataobject.*;
import cn.iocoder.yudao.module.exam.dal.mysql.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;
import static cn.iocoder.yudao.module.exam.support.ExamException.require;

@Component
public class ExamStore {
    @Resource SessionMapper sessions;
    @Resource VenueMapper venues;
    @Resource SessionClassMapper sessionClasses;
    @Resource ParticipantMapper participants;
    @Resource ScoreItemMapper items;
    @Resource ScoreDetailMapper details;

    public static <T> QueryWrapper<T> query() {
        return new QueryWrapper<T>().eq("tenant_id", tenant()).eq("deleted", false);
    }

    public static Long tenant() {
        return TenantContextHolder.getRequiredTenantId();
    }

    public ExamSessionDO session(Long id) {
        ExamRules.id(id);
        ExamSessionDO row = sessions.selectOne(ExamStore.<ExamSessionDO>query().eq("id", id));
        require(row != null, "考试不存在或不在可访问范围");
        return row;
    }

    public ExamSessionDO lockSession(Long id) {
        ExamRules.writeTransaction();
        ExamRules.id(id);
        ExamSessionDO row = sessions.lock(tenant(), id);
        require(row != null, "考试不存在或不在可访问范围");
        return row;
    }

    public ExamVenueDO venue(Long id, boolean lock) {
        ExamRules.id(id);
        ExamVenueDO row = lock ? venues.lock(tenant(), id)
                : venues.selectOne(ExamStore.<ExamVenueDO>query().eq("id", id));
        require(row != null, "考场不存在");
        return row;
    }

    public List<Long> classes(Long sessionId) {
        return sessionClasses.selectList(ExamStore.<ExamSessionClassDO>query().eq("session_id", sessionId)
                .orderByAsc("class_id")).stream().map(ExamSessionClassDO::getClassId).collect(Collectors.toList());
    }

    public List<ExamParticipantDO> participants(Long sessionId) {
        return participants.selectList(ExamStore.<ExamParticipantDO>query().eq("session_id", sessionId).orderByAsc("id"));
    }

    public List<ExamScoreItemDO> items(Long sessionId) {
        return items.selectList(ExamStore.<ExamScoreItemDO>query().eq("session_id", sessionId).orderByAsc("sort", "id"));
    }

    public void mutable(ExamSessionDO session) {
        require(ExamRules.now().isBefore(session.getStartTime()), "考试已开始，禁止修改基础信息、名册或删除");
        require(participants.selectCount(ExamStore.<ExamParticipantDO>query().eq("session_id", session.getId())
                .and(q -> q.isNotNull("sign_time").or().isNotNull("final_score"))) == 0,
                "考试已有签到或成绩，禁止修改基础信息、名册或删除");
    }

    public void updateSession(ExamSessionDO session, Long previousVersion) {
        session.setSessionVersion(previousVersion + 1);
        require(sessions.update(session, ExamStore.<ExamSessionDO>query().eq("id", session.getId())
                .eq("session_version", previousVersion)) == 1, "考试版本已变化，请刷新后重试");
    }

    public void read(ExamAccess.Scope scope, ExamSessionDO session) {
        require(classes(session.getId()).stream().anyMatch(classId -> scope.canRead(session, classId)),
                "考试不存在或不在可访问范围");
    }

    public void copyVenue(ExamSessionDO session, ExamVenueDO venue) {
        require(Integer.valueOf(0).equals(venue.getStatus()), "考场已停用");
        session.setVenueId(venue.getId());
        session.setVenueName(venue.getName());
        session.setVenueAddress(venue.getAddress());
        session.setVenueCapacity(venue.getCapacity());
        session.setVenueLatitude(venue.getLatitude());
        session.setVenueLongitude(venue.getLongitude());
        session.setRadiusMeters(venue.getRadiusMeters());
    }
}
