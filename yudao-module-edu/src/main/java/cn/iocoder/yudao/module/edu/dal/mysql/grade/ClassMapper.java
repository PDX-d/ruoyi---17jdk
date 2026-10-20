package cn.iocoder.yudao.module.edu.dal.mysql.grade;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.edu.dal.dataobject.grade.ClassDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 班级 Mapper
 */
@Mapper
public interface ClassMapper extends BaseMapperX<ClassDO> {

    /**
     * 按年级查询班级（供学生表单下拉使用）
     *
     * @param gradeId 年级编号
     * @return 班级列表
     */
    default List<ClassDO> selectListByGradeId(Long gradeId) {
        return selectList(ClassDO::getGradeId, gradeId);
    }

    /**
     * 按班级名称查询（供学生 Excel 导入时「班级名称 → 班级编号」反查使用）
     *
     * @param name 班级名称（如 软件工程2301班）
     * @return 班级；不存在时返回 null
     */
    default ClassDO selectByName(String name) {
        return CollUtil.getFirst(selectList(ClassDO::getName, name));
    }

}
