package cn.iocoder.yudao.module.edu.dal.mysql.student;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.StudentTagPageReqVO;
import cn.iocoder.yudao.module.edu.dal.dataobject.student.StudentTagDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * 学生标签 Mapper
 */
@Mapper
public interface StudentTagMapper extends BaseMapperX<StudentTagDO> {

    /**
     * 按名称查询标签
     *
     * @param name 标签名称
     * @return 标签
     */
    default StudentTagDO selectByName(String name) {
        return selectOne(StudentTagDO::getName, name);
    }

    /**
     * 查询全部标签（供学生表单下拉、按标签筛选、标签管理使用）
     *
     * 不做状态过滤，保证标签管理能看到停用标签；排序为「启用在前、停用在后，同状态按 sort 升序」。
     *
     * @return 标签列表
     */
    default List<StudentTagDO> selectListAll() {
        return selectList(new LambdaQueryWrapperX<StudentTagDO>()
                .orderByAsc(StudentTagDO::getStatus)
                .orderByAsc(StudentTagDO::getSort)
                .orderByAsc(StudentTagDO::getId));
    }

    /**
     * 查询当前未删除标签的最大显示序号
     *
     * @return 最大序号；无标签时返回 null
     */
    default Integer selectMaxSort() {
        return selectList().stream()
                .map(StudentTagDO::getSort)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(null);
    }

    /**
     * 按编号集合查询标签
     *
     * @param ids 标签编号集合
     * @return 标签列表
     */
    default List<StudentTagDO> selectListByIds(Collection<Long> ids) {
        return selectList(StudentTagDO::getId, ids);
    }

    /**
     * 标签分页查询
     *
     * @param reqVO 分页查询条件
     * @return 标签分页
     */
    default PageResult<StudentTagDO> selectPage(StudentTagPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<StudentTagDO>()
                .likeIfPresent(StudentTagDO::getName, reqVO.getName())
                .orderByAsc(StudentTagDO::getStatus)
                .orderByAsc(StudentTagDO::getSort)
                .orderByAsc(StudentTagDO::getId));
    }

    /**
     * 批量写入显示序号（单条 UPDATE + CASE 实现，用于重排）
     *
     * 唯一索引 uk_tenant_sort 会使逐行 UPDATE 中途撞键，调用方需按「先写负数区、再写最终值」两阶段执行。
     *
     * @param list 只包含 id 与目标 sort 的标签集合
     * @return 更新条数
     */
    @Update("<script>" +
            "UPDATE edu_student_tag SET sort = CASE id " +
            "<foreach collection='list' item='item'>WHEN #{item.id} THEN #{item.sort} </foreach>" +
            "ELSE sort END " +
            "WHERE deleted = b'0' AND id IN " +
            "<foreach collection='list' item='item' open='(' separator=',' close=')'>#{item.id}</foreach>" +
            "</script>")
    int updateSortBatch(@Param("list") List<StudentTagDO> list);

}
