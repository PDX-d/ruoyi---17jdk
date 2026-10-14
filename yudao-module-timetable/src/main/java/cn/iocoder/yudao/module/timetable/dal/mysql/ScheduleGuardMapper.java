package cn.iocoder.yudao.module.timetable.dal.mysql;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ScheduleGuardMapper {
    @Insert("INSERT INTO timetable_schedule_guard (tenant_id) VALUES (#{tenantId})"
            + " ON DUPLICATE KEY UPDATE tenant_id = #{tenantId}")
    int ensure(@Param("tenantId") Long tenantId);

    @Select("SELECT tenant_id FROM timetable_schedule_guard WHERE tenant_id = #{tenantId} FOR UPDATE")
    Long lock(@Param("tenantId") Long tenantId);
}
