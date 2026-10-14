package cn.iocoder.yudao.module.timetable.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.timetable.dal.dataobject.LessonClassDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LessonClassMapper extends BaseMapperX<LessonClassDO> {
    @Delete("DELETE FROM timetable_lesson_class WHERE lesson_id = #{lessonId} AND tenant_id = #{tenantId}")
    int removeCurrentRelations(@Param("lessonId") Long lessonId, @Param("tenantId") Long tenantId);
}
