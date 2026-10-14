package cn.iocoder.yudao.module.timetable.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.timetable.controller.admin.vo.*;

public interface ChangeRequestService {
    Long create(ChangeCreateReqVO request);
    ChangeRespVO get(Long id);
    PageResult<ChangeRespVO> page(ChangePageReqVO request);
    void approve(ChangeReviewReqVO request);
    void reject(ChangeReviewReqVO request);
}
