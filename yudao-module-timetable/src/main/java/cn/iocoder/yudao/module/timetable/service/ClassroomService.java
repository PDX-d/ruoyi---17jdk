package cn.iocoder.yudao.module.timetable.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.timetable.controller.admin.vo.*;
import cn.iocoder.yudao.module.timetable.dal.dataobject.ClassroomDO;
import java.util.List;
import java.util.Collection;
import java.util.Map;

public interface ClassroomService {
    Long create(ClassroomSaveReqVO request);
    void update(ClassroomSaveReqVO request);
    void delete(Long id);
    ClassroomRespVO get(Long id);
    PageResult<ClassroomRespVO> page(ClassroomPageReqVO request);
    List<ClassroomRespVO> simpleList();
    ClassroomDO find(Long id);
    Map<Long, ClassroomDO> findMap(Collection<Long> ids);
}
