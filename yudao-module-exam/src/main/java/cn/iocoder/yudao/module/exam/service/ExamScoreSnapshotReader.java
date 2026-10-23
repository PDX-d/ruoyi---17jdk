package cn.iocoder.yudao.module.exam.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.exam.api.reference.ExamReferenceData.Catalog;
import cn.iocoder.yudao.module.exam.controller.admin.vo.ExamVO.*;
import cn.iocoder.yudao.module.exam.dal.dataobject.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import javax.validation.Validator;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import static cn.iocoder.yudao.module.exam.support.ExamException.require;

@Component
public class ExamScoreSnapshotReader {
    @Resource private ExamStore store;
    @Resource private ExamAccess access;
    @Resource private ExamReferences references;
    @Resource private ExamViews views;
    @Resource private Validator validator;

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRES_NEW)
    public ScoreSnapshotResp read(ExamQueryReq request, String permission) {
        require(Arrays.asList("exam:score:query", "exam:score:export", "exam:score:import", "exam:statistics:query")
                .contains(permission), "快照用途无效");
        ExamAccess.Scope scope = access.staff(permission);
        ExamRules.valid(validator, request);
        List<ExamSessionDO> sessions = sessions(request, scope);
        if (request.getSessionId() != null) {
            require(!sessions.isEmpty(), "考试不存在或不在可访问范围");
        }
        Catalog catalog = references.catalog();
        List<ExamParticipantDO> participants = new ArrayList<>();
        for (ExamSessionDO session : sessions) {
            List<Long> classes = readableClasses(session, scope, request.getClassId());
            if (!classes.isEmpty()) {
                participants.addAll(store.participants.selectList(ExamStore.<ExamParticipantDO>query()
                        .eq("session_id", session.getId()).in("class_id", classes).orderByAsc("student_no", "id")));
            }
        }
        List<ScoreItemResp> items = items(sessions);
        List<ScoreRowResp> rows = rows(participants, sessions, items, catalog);
        ScoreSnapshotResp result = new ScoreSnapshotResp();
        result.setSessions(sessions.stream().map(session -> views.session(session, scope, catalog)).collect(Collectors.toList()));
        result.setItems(items);
        result.setRows(rows);
        result.setStatistics(statistics(rows));
        result.setGeneratedAt(ExamRules.now());
        return result;
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRES_NEW)
    public PageResult<ScoreRowResp> page(ScoreQueryReq request) {
        ExamAccess.Scope scope = access.staff("exam:score:query");
        ExamRules.valid(validator, request);
        ExamRules.page(request);
        List<ExamSessionDO> sessions = sessions(request, scope);
        QueryWrapper<ExamParticipantDO> query = ExamStore.<ExamParticipantDO>query()
                .like(request.getStudentNo() != null, "student_no", request.getStudentNo())
                .like(request.getStudentName() != null, "student_name", request.getStudentName());
        Map<Long, List<Long>> classes = new LinkedHashMap<>();
        sessions.forEach(session -> {
            List<Long> ids = readableClasses(session, scope, request.getClassId());
            if (!ids.isEmpty()) {
                classes.put(session.getId(), ids);
            }
        });
        if (classes.isEmpty()) {
            query.eq("id", -1L);
        } else {
            query.and(group -> classes.forEach((sessionId, ids) -> group.or(branch -> branch
                    .eq("session_id", sessionId).in("class_id", ids))));
        }
        PageResult<ExamParticipantDO> page = store.participants.selectPage(request, query.orderByAsc("session_id", "student_no", "id"));
        return new PageResult<>(rows(page.getList(), sessions, items(sessions), references.catalog()), page.getTotal());
    }

    private List<ExamSessionDO> sessions(ExamQueryReq request, ExamAccess.Scope scope) {
        QueryWrapper<ExamSessionDO> query = views.query(request);
        scope.sessionQuery(query, request.getClassId());
        return store.sessions.selectList(query.orderByAsc("start_time", "id"));
    }

    private List<Long> readableClasses(ExamSessionDO session, ExamAccess.Scope scope, Long requestedClass) {
        return store.classes(session.getId()).stream()
                .filter(id -> (requestedClass == null || requestedClass.equals(id)) && scope.canRead(session, id))
                .collect(Collectors.toList());
    }

    private List<ScoreItemResp> items(List<ExamSessionDO> sessions) {
        List<ScoreItemResp> items = new ArrayList<>();
        sessions.forEach(session -> store.items(session.getId()).forEach(item -> items.add(views.item(item))));
        return items;
    }

    private List<ScoreRowResp> rows(List<ExamParticipantDO> participants, List<ExamSessionDO> sessions,
                                    List<ScoreItemResp> items, Catalog catalog) {
        Map<Long, ExamSessionDO> sessionMap = sessions.stream().collect(Collectors.toMap(ExamSessionDO::getId, Function.identity()));
        Map<Long, ScoreItemResp> itemMap = items.stream().collect(Collectors.toMap(ScoreItemResp::getId, Function.identity()));
        Map<Long, ExamParticipantDO> participantMap = participants.stream()
                .collect(Collectors.toMap(ExamParticipantDO::getId, Function.identity()));
        Map<Long, Map<Long, BigDecimal>> scores = new HashMap<>();
        List<Long> ids = participants.stream().map(ExamParticipantDO::getId).collect(Collectors.toList());
        for (int start = 0; start < ids.size(); start += 1000) {
            List<ExamScoreDetailDO> details = store.details.selectList(ExamStore.<ExamScoreDetailDO>query()
                    .in("participant_id", ids.subList(start, Math.min(start + 1000, ids.size()))));
            for (ExamScoreDetailDO detail : details) {
                ScoreItemResp item = itemMap.get(detail.getItemId());
                require(item != null && Objects.equals(item.getSessionId(),
                        participantMap.get(detail.getParticipantId()).getSessionId()), "成绩分项与考试不一致");
                scores.computeIfAbsent(detail.getParticipantId(), id -> new LinkedHashMap<>())
                        .put(detail.getItemId(), detail.getScore());
            }
        }
        List<ScoreRowResp> rows = new ArrayList<>();
        for (ExamParticipantDO participant : participants) {
            ExamSessionDO session = sessionMap.get(participant.getSessionId());
            ScoreRowResp row = new ScoreRowResp();
            BeanUtils.copyProperties(participant, row);
            row.setSessionName(session.getName());
            row.setCourseId(session.getCourseId());
            row.setCourseName(views.courseName(catalog, session.getCourseId()));
            row.setStartTime(session.getStartTime());
            row.setEndTime(session.getEndTime());
            row.setSessionVersion(session.getSessionVersion());
            row.setScores(scores.getOrDefault(participant.getId(), Collections.emptyMap()));
            Set<Long> expected = items.stream().filter(item -> session.getId().equals(item.getSessionId()))
                    .map(ScoreItemResp::getId).collect(Collectors.toSet());
            boolean complete = !expected.isEmpty() && row.getScores().keySet().equals(expected)
                    && row.getScores().values().stream().allMatch(Objects::nonNull);
            row.setFinalScore(complete ? participant.getFinalScore() : null);
            rows.add(row);
        }
        return rows;
    }

    private StatisticsResp statistics(List<ScoreRowResp> rows) {
        List<BigDecimal> graded = rows.stream().map(ScoreRowResp::getFinalScore).filter(Objects::nonNull).collect(Collectors.toList());
        StatisticsResp result = new StatisticsResp();
        result.setParticipantCount(rows.size());
        result.setGradedCount(graded.size());
        result.setUngradedCount(rows.size() - graded.size());
        result.setPassCount(graded.stream().filter(score -> score.compareTo(new BigDecimal("60")) >= 0).count());
        if (!graded.isEmpty()) {
            result.setMaxScore(Collections.max(graded));
            result.setMinScore(Collections.min(graded));
            result.setAverageScore(graded.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(graded.size()), 2, RoundingMode.HALF_UP));
            result.setPassRate(BigDecimal.valueOf(result.getPassCount()).multiply(new BigDecimal("100"))
                    .divide(BigDecimal.valueOf(graded.size()), 2, RoundingMode.HALF_UP));
        }
        List<ScoreBandResp> bands = new ArrayList<>();
        int[] bounds = {0, 60, 70, 80, 90, 101};
        String[] names = {"0-59.99", "60-69.99", "70-79.99", "80-89.99", "90-100"};
        for (int index = 0; index < names.length; index++) {
            BigDecimal lower = BigDecimal.valueOf(bounds[index]);
            BigDecimal upper = BigDecimal.valueOf(bounds[index + 1]);
            ScoreBandResp band = new ScoreBandResp();
            band.setName(names[index]);
            band.setCount(graded.stream().filter(score -> score.compareTo(lower) >= 0 && score.compareTo(upper) < 0).count());
            bands.add(band);
        }
        result.setBands(bands);
        return result;
    }
}
