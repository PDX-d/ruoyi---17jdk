package cn.iocoder.yudao.module.exam.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.exam.dal.dataobject.ExamParticipantDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ParticipantMapper extends BaseMapperX<ExamParticipantDO> {
    @Select("SELECT * FROM exam_participant WHERE tenant_id = #{tenantId} AND id = #{id} AND deleted = 0 FOR UPDATE")
    ExamParticipantDO lock(@Param("tenantId") Long tenantId, @Param("id") Long id);
}

