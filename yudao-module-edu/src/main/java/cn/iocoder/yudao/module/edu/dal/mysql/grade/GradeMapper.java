package cn.iocoder.yudao.module.edu.dal.mysql.grade;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.edu.dal.dataobject.grade.GradeDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 年级 Mapper
 */
@Mapper
public interface GradeMapper extends BaseMapperX<GradeDO> {

    /**
     * 查询全部年级（供学生表单下拉使用）
     *
     * @return 年级列表
     */
    default List<GradeDO> selectListAll() {
        return selectList();
    }

    /**
     * 按年级名称查询（供学生 Excel 导入时「年级名称 → 年级编号」反查使用）
     *
     * @param name 年级名称（如 2023级）
     * @return 年级；不存在时返回 null
     */
    default GradeDO selectByName(String name) {
        return CollUtil.getFirst(selectList(GradeDO::getName, name));
    }

}
