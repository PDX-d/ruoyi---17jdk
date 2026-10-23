package cn.iocoder.yudao.module.exam.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.exam.controller.admin.vo.ExamVO.*;
import java.util.List;

public interface ExamVenueService {
    Long create(VenueSaveReq request);
    void update(VenueSaveReq request);
    void delete(Long id, Long version);
    VenueResp get(Long id);
    PageResult<VenueResp> page(VenueQueryReq request);
    List<VenueResp> simpleList();
}
