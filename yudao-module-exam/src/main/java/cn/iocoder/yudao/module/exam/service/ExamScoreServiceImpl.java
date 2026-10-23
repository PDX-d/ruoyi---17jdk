package cn.iocoder.yudao.module.exam.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.exam.controller.admin.vo.ExamVO.*;
import cn.iocoder.yudao.module.exam.dal.dataobject.*;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import javax.validation.Validator;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import static cn.iocoder.yudao.module.exam.support.ExamException.require;

@Service
public class ExamScoreServiceImpl implements ExamScoreService {
    @Resource private ExamStore store;
    @Resource private ExamAccess access;
    @Resource private ExamReferences references;
    @Resource private ExamViews views;
    @Resource private ExamScoreConfiguration configuration;
    @Resource private ExamScoreSnapshotReader snapshots;
    @Resource private Validator validator;

    @Override
    public PageResult<ScoreRowResp> page(ScoreQueryReq request) {
        access.staff("exam:score:query");
        return snapshots.page(request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void save(ScoreSaveReq request) {
        saveRows(request, "exam:score:update");
    }

    @Override
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void importScores(ScoreSaveReq request) {
        saveRows(request, "exam:score:import");
    }

    private void saveRows(ScoreSaveReq request, String permission) {
        access.staff(permission);
        ExamRules.valid(validator, request);
        require(request.getRows().size() <= 5000, "每批最多5000条成绩");
        ExamSessionDO session = store.lockSession(request.getSessionId());
        ExamAccess.Scope scope = access.staff(permission);
        references.writable(session.getSemesterId());
        ExamRules.version(request.getSessionVersion(), session.getSessionVersion());
        Set<Long> rowIds = new TreeSet<>();
        for (ScoreRowSave row : request.getRows()) {
            require(row != null, "成绩行不能为空");
            require(rowIds.add(row.getParticipantId()), "同批成绩存在重复学生");
        }
        require(store.participants.selectCount(ExamStore.<ExamParticipantDO>query()
                .eq("session_id", session.getId()).in("id", rowIds)) == rowIds.size(), "包含未知或其他考试学生");
        Map<Long, ExamParticipantDO> participants = new LinkedHashMap<>();
        for (Long id : rowIds) {
            ExamParticipantDO participant = store.participants.lock(ExamStore.tenant(), id);
            require(participant != null && session.getId().equals(participant.getSessionId()), "考试名册已变化");
            scope.grade(session, participant.getClassId());
            participants.put(id, participant);
        }
        List<ExamScoreItemDO> items = store.items(session.getId());
        require(!items.isEmpty(), "考试未配置成绩分项");
        require(items.stream().map(ExamScoreItemDO::getWeight).reduce(BigDecimal.ZERO, BigDecimal::add)
                .compareTo(new BigDecimal("100")) == 0, "考试分项权重无效");
        Set<Long> itemIds = items.stream().map(ExamScoreItemDO::getId).collect(Collectors.toSet());
        for (ScoreRowSave row : request.getRows()) {
            ExamRules.version(row.getRowVersion(), participants.get(row.getParticipantId()).getRowVersion());
            require(row.getScores() != null && row.getScores().keySet().equals(itemIds), "每个学生必须完整提交全部成绩分项");
            row.getScores().values().forEach(score -> require(score != null && score.signum() >= 0
                    && score.compareTo(new BigDecimal("100")) <= 0 && score.stripTrailingZeros().scale() <= 2,
                    "成绩必须为0至100且至多两位小数"));
        }
        Map<Long, ScoreRowSave> rows = request.getRows().stream()
                .collect(Collectors.toMap(ScoreRowSave::getParticipantId, Function.identity()));
        for (ExamParticipantDO participant : participants.values()) {
            writeRow(participant, rows.get(participant.getId()), items);
        }
        if (!Boolean.TRUE.equals(session.getScoreConfigLocked())) {
            require(store.sessions.update(null, new UpdateWrapper<ExamSessionDO>()
                    .eq("tenant_id", ExamStore.tenant()).eq("deleted", false).eq("id", session.getId())
                    .eq("session_version", session.getSessionVersion()).set("score_config_locked", true)) == 1,
                    "考试版本已变化");
        }
    }

    private void writeRow(ExamParticipantDO participant, ScoreRowSave row, List<ExamScoreItemDO> items) {
        Map<Long, ExamScoreDetailDO> existing = store.details.selectList(ExamStore.<ExamScoreDetailDO>query()
                .eq("participant_id", participant.getId())).stream()
                .collect(Collectors.toMap(ExamScoreDetailDO::getItemId, Function.identity()));
        BigDecimal total = BigDecimal.ZERO;
        for (ExamScoreItemDO item : items) {
            BigDecimal score = row.getScores().get(item.getId());
            ExamScoreDetailDO detail = existing.get(item.getId());
            if (detail == null) {
                detail = new ExamScoreDetailDO();
                detail.setTenantId(ExamStore.tenant());
                detail.setParticipantId(participant.getId());
                detail.setItemId(item.getId());
                detail.setScore(score);
                store.details.insert(detail);
            } else {
                detail.setScore(score);
                require(store.details.update(detail, ExamStore.<ExamScoreDetailDO>query().eq("id", detail.getId())) == 1,
                        "成绩分项已变化");
            }
            total = total.add(score.multiply(item.getWeight()));
        }
        total = total.divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        require(store.participants.update(null, new UpdateWrapper<ExamParticipantDO>()
                .eq("tenant_id", ExamStore.tenant()).eq("deleted", false).eq("id", participant.getId())
                .eq("row_version", row.getRowVersion()).set("final_score", total)
                .set("row_version", row.getRowVersion() + 1)) == 1, "学生成绩版本已变化");
    }

    @Override
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void config(ScoreConfigReq request) {
        access.staff("exam:score:config");
        ExamRules.valid(validator, request);
        ExamSessionDO session = store.lockSession(request.getSessionId());
        access.staff("exam:score:config");
        access.maintain(store.classes(session.getId()));
        references.writable(session.getSemesterId());
        ExamRules.version(request.getSessionVersion(), session.getSessionVersion());
        require(!Boolean.TRUE.equals(session.getScoreConfigLocked())
                && store.participants.selectCount(ExamStore.<ExamParticipantDO>query().eq("session_id", session.getId())
                .isNotNull("final_score")) == 0, "首次录分后禁止修改成绩分项和权重");
        configuration.replace(session.getId(), views.scoreItems(request.getScoreItems()));
        store.updateSession(session, request.getSessionVersion());
    }

    @Override
    public ScoreSnapshotResp snapshot(ExamQueryReq request) {
        access.staff("exam:score:query");
        return snapshots.read(request, "exam:score:query");
    }

    @Override
    public ScoreSnapshotResp exportSnapshot(ExamQueryReq request) {
        access.staff("exam:score:export");
        require(request != null && (request.getSessionId() != null || request.getCourseId() != null
                || request.getClassId() != null), "导出须限定考试、课程或班级");
        return snapshots.read(request, "exam:score:export");
    }

    @Override
    public ScoreSnapshotResp importSnapshot(ExamQueryReq request) {
        access.staff("exam:score:import");
        require(request != null && request.getSessionId() != null, "模板和导入必须指定考试");
        return snapshots.read(request, "exam:score:import");
    }

    @Override
    public StatisticsResp statistics(ExamQueryReq request) {
        access.staff("exam:statistics:query");
        return snapshots.read(request, "exam:statistics:query").getStatistics();
    }
}
