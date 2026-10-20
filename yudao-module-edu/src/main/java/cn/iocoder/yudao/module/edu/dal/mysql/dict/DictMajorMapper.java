package cn.iocoder.yudao.module.edu.dal.mysql.dict;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.edu.dal.dataobject.dict.DictMajorDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 专业 Mapper（表 dict_major）
 *
 * 此处仅实现学生表单「专业下拉（按年级联动）」所需的最小查询。
 * TODO: 字典中心模块待接入
 */
@Mapper
public interface DictMajorMapper extends BaseMapperX<DictMajorDO> {

    /**
     * 按年级查询专业列表（不限年级的通用专业一并返回）
     *
     * @param gradeId 年级编号；为空时返回全部专业
     * @return 专业列表
     */
    default List<DictMajorDO> selectListByGradeId(Long gradeId) {
        return selectList(new LambdaQueryWrapperX<DictMajorDO>()
                .eqIfPresent(DictMajorDO::getGradeId, gradeId)
                .orderByAsc(DictMajorDO::getSort)
                .orderByAsc(DictMajorDO::getId));
    }

    /**
     * 按专业名称查询（供学生 Excel 导入时「专业名称 → 专业编号」反查使用）
     *
     * @param name 专业名称（如 软件工程）
     * @return 专业；不存在时返回 null
     */
    default DictMajorDO selectByName(String name) {
        return CollUtil.getFirst(selectList(DictMajorDO::getName, name));
    }

}
