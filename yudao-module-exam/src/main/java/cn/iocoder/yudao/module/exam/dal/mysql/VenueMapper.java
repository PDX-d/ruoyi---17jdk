package cn.iocoder.yudao.module.exam.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.exam.dal.dataobject.ExamVenueDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface VenueMapper extends BaseMapperX<ExamVenueDO> {
    @Select("SELECT * FROM exam_venue WHERE tenant_id = #{tenantId} AND id = #{id} AND deleted = 0 FOR UPDATE")
    ExamVenueDO lock(@Param("tenantId") Long tenantId, @Param("id") Long id);
}

