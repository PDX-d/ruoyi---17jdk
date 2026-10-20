package cn.iocoder.yudao.module.edu.dal.mysql.semester;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.edu.controller.admin.semester.vo.SemesterPageReqVO;
import cn.iocoder.yudao.module.edu.dal.dataobject.semester.SemesterDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 学期 Mapper
 */
@Mapper
public interface SemesterMapper extends BaseMapperX<SemesterDO> {

    /**
     * 按「学年 + 学期」查询学期
     *
     * @param schoolYear 学年
     * @param term       学期
     * @return 学期
     */
    default SemesterDO selectBySchoolYearAndTerm(String schoolYear, Integer term) {
        return selectOne(new LambdaQueryWrapper<SemesterDO>()
                .eq(SemesterDO::getSchoolYear, schoolYear)
                .eq(SemesterDO::getTerm, term));
    }

    /**
     * 获得学期分页
     *
     * @param reqVO 分页查询
     * @return 学期分页
     */
    default PageResult<SemesterDO> selectPage(SemesterPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SemesterDO>()
                .likeIfPresent(SemesterDO::getName, reqVO.getName())
                .likeIfPresent(SemesterDO::getSchoolYear, reqVO.getSchoolYear())
                .eqIfPresent(SemesterDO::getTerm, reqVO.getTerm())
                .eqIfPresent(SemesterDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(SemesterDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(SemesterDO::getId));
    }

    /**
     * 获得当前学期（isCurrent = true）
     *
     * @return 当前学期，可能为 null
     */
    default SemesterDO selectCurrent() {
        return selectOne(new LambdaQueryWrapper<SemesterDO>()
                .eq(SemesterDO::getIsCurrent, true)
                .last("LIMIT 1"));
    }

    /**
     * 获得所有「当前学期」标记为 true 的学期列表
     *
     * 用于 isCurrent 互斥：保存前先将旧记录逐条置为 false，避免出现多条当前学期。
     *
     * @return 学期列表
     */
    default List<SemesterDO> selectListByIsCurrent() {
        return selectList(new LambdaQueryWrapper<SemesterDO>()
                .eq(SemesterDO::getIsCurrent, true));
    }

    /**
     * 获得全部学期列表（按开始日期升序），用于按当前日期重算状态与当前学期标记
     *
     * @return 学期列表
     */
    default List<SemesterDO> selectListAll() {
        return selectList(new LambdaQueryWrapper<SemesterDO>()
                .orderByAsc(SemesterDO::getStartDate));
    }

}
