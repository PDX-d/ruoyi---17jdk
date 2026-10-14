package cn.iocoder.yudao.module.timetable.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.timetable.dal.dataobject.ClassroomDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ClassroomMapper extends BaseMapperX<ClassroomDO> {
    @Select("SELECT (SELECT COUNT(*) FROM timetable_lesson WHERE tenant_id = #{tenantId} AND classroom_id = #{id})"
            + " + (SELECT COUNT(*) FROM timetable_change_request WHERE tenant_id = #{tenantId}"
            + " AND (original_classroom_id = #{id} OR target_classroom_id = #{id}))")
    Long countReferences(@Param("id") Long id, @Param("tenantId") Long tenantId);
}
