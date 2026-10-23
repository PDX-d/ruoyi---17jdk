package cn.iocoder.yudao.module.exam.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.exam.dal.dataobject.ExamScoreDetailDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ScoreDetailMapper extends BaseMapperX<ExamScoreDetailDO> {
}

