package cn.iocoder.yudao.module.exam.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.exam.controller.admin.vo.ExamVO.*;

public interface ExamScoreService {
    PageResult<ScoreRowResp> page(ScoreQueryReq request);
    void save(ScoreSaveReq request);
    void importScores(ScoreSaveReq request);
    void config(ScoreConfigReq request);
    ScoreSnapshotResp snapshot(ExamQueryReq request);
    ScoreSnapshotResp exportSnapshot(ExamQueryReq request);
    ScoreSnapshotResp importSnapshot(ExamQueryReq request);
    StatisticsResp statistics(ExamQueryReq request);
}
