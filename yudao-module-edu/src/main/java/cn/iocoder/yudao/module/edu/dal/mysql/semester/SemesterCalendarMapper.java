package cn.iocoder.yudao.module.edu.dal.mysql.semester;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.edu.dal.dataobject.semester.SemesterCalendarDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 学期校历/时间节点 Mapper
 */
@Mapper
public interface SemesterCalendarMapper extends BaseMapperX<SemesterCalendarDO> {

    /**
     * 按学期编号查询校历节点列表
     *
     * @param semesterId 学期编号
     * @return 校历节点列表
     */
    default List<SemesterCalendarDO> selectListBySemesterId(Long semesterId) {
        return selectList(SemesterCalendarDO::getSemesterId, semesterId);
    }

    /**
     * 物理删除某学期下的全部校历节点（学期删除时一并清理，避免孤儿数据）
     *
     * @param semesterId 学期编号
     * @return 删除数量
     */
    @Delete("DELETE FROM edu_semester_calendar WHERE semester_id = #{semesterId}")
    int deleteBySemesterId(@Param("semesterId") Long semesterId);

}
