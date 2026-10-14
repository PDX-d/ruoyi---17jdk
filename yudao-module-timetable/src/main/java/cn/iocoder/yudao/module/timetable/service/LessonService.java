package cn.iocoder.yudao.module.timetable.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.timetable.controller.admin.vo.*;
import java.util.List;

public interface LessonService {
    Long create(LessonSaveReqVO request);
    List<Long> batchCreate(LessonBatchReqVO request);
    void update(LessonSaveReqVO request);
    void delete(Long id);
    LessonRespVO get(Long id);
    PageResult<LessonRespVO> page(LessonQueryReqVO request);
    List<LessonRespVO> calendar(LessonQueryReqVO request);
    List<LessonRespVO> exportData(LessonQueryReqVO request);
    List<ConflictRespVO> checkConflict(LessonSaveReqVO request);
}
