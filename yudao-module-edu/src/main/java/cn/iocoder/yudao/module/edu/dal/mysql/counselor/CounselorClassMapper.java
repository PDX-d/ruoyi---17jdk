package cn.iocoder.yudao.module.edu.dal.mysql.counselor;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.edu.dal.dataobject.counselor.CounselorClassDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * 辅导员带班 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface CounselorClassMapper extends BaseMapperX<CounselorClassDO> {

    default List<CounselorClassDO> selectListByCounselorId(Long counselorId) {
        return selectList(CounselorClassDO::getCounselorId, counselorId);
    }

    default List<CounselorClassDO> selectListByCounselorIds(Collection<Long> counselorIds) {
        return selectList(CounselorClassDO::getCounselorId, counselorIds);
    }

    default List<CounselorClassDO> selectListByClassName(String className) {
        return selectList(CounselorClassDO::getClassName, className);
    }

    default Long selectCountByCounselorId(Long counselorId) {
        return selectCount(CounselorClassDO::getCounselorId, counselorId);
    }

    /**
     * 物理删除辅导员名下的所有带班关联
     *
     * 说明：edu_counselor_class 使用「租户 + 班级」维度的唯一键，
     * 若走逻辑删除，重复移交同一班级会产生多条 deleted=1 的记录从而触发唯一键冲突，
     * 因此这里改为物理删除，保证「先删后插」可以重复执行。
     */
    @Delete("DELETE FROM edu_counselor_class WHERE counselor_id = #{counselorId}")
    int deleteByCounselorId(@Param("counselorId") Long counselorId);

}
