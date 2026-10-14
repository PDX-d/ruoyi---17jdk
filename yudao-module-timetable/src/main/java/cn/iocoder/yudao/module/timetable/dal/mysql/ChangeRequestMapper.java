package cn.iocoder.yudao.module.timetable.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.timetable.dal.dataobject.ChangeRequestDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChangeRequestMapper extends BaseMapperX<ChangeRequestDO> {
}
