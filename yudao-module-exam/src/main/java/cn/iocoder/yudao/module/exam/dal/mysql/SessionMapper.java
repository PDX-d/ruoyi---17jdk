package cn.iocoder.yudao.module.exam.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.exam.dal.dataobject.ExamSessionDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SessionMapper extends BaseMapperX<ExamSessionDO> {
    @Select("SELECT * FROM exam_session WHERE tenant_id = #{tenantId} AND id = #{id} AND deleted = 0 FOR UPDATE")
    ExamSessionDO lock(@Param("tenantId") Long tenantId, @Param("id") Long id);

    @Select("SELECT COUNT(*) FROM exam_session WHERE tenant_id = #{tenantId} AND venue_id = #{venueId}")
    long countAllVenueReferences(@Param("tenantId") Long tenantId, @Param("venueId") Long venueId);
}

