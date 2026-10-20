package cn.iocoder.yudao.module.edu.dal.mysql.student;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.edu.dal.dataobject.student.StudentTagRelDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * 学生-标签关联 Mapper
 */
@Mapper
public interface StudentTagRelMapper extends BaseMapperX<StudentTagRelDO> {

    /**
     * 查询某学生关联的标签关联列表
     *
     * @param studentId 学生编号
     * @return 关联列表
     */
    default List<StudentTagRelDO> selectListByStudentId(Long studentId) {
        return selectList(StudentTagRelDO::getStudentId, studentId);
    }

    /**
     * 批量查询学生关联的标签关联列表
     *
     * @param studentIds 学生编号集合
     * @return 关联列表
     */
    default List<StudentTagRelDO> selectListByStudentIds(Collection<Long> studentIds) {
        return selectList(StudentTagRelDO::getStudentId, studentIds);
    }

    /**
     * 物理删除某学生名下的所有标签关联
     *
     * 走逻辑删除会因唯一键冲突导致「先删后插」不可重复执行，故改为物理删除；租户条件由租户插件自动拼接。
     *
     * @param studentId 学生编号
     * @return 删除条数
     */
    @Delete("DELETE FROM edu_student_tag_rel WHERE student_id = #{studentId}")
    int deleteByStudentId(@Param("studentId") Long studentId);

    /**
     * 物理删除某标签下的所有学生关联
     *
     * 删除标签时同步清理关联，避免出现脏关联；物理删除原因同 {@link #deleteByStudentId(Long)}。
     *
     * @param tagId 标签编号
     * @return 删除条数
     */
    @Delete("DELETE FROM edu_student_tag_rel WHERE tag_id = #{tagId}")
    int deleteByTagId(@Param("tagId") Long tagId);

}
