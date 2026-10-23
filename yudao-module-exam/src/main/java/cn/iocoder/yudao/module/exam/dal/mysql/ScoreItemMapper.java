package cn.iocoder.yudao.module.exam.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.exam.dal.dataobject.ExamScoreItemDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ScoreItemMapper extends BaseMapperX<ExamScoreItemDO> {
}

