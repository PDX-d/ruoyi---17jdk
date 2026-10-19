package cn.iocoder.yudao.module.edu.dal.mysql.teacher;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.edu.controller.admin.teacher.vo.TeacherPageReqVO;
import cn.iocoder.yudao.module.edu.dal.dataobject.teacher.TeacherDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 教师 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface TeacherMapper extends BaseMapperX<TeacherDO> {

    default TeacherDO selectByJobNo(String jobNo) {
        return selectOne(TeacherDO::getJobNo, jobNo);
    }

    default PageResult<TeacherDO> selectPage(TeacherPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<TeacherDO>()
                .likeIfPresent(TeacherDO::getJobNo, reqVO.getJobNo())
                .likeIfPresent(TeacherDO::getName, reqVO.getName())
                .eqIfPresent(TeacherDO::getGender, reqVO.getGender())
                .likeIfPresent(TeacherDO::getMobile, reqVO.getMobile())
                .likeIfPresent(TeacherDO::getTitle, reqVO.getTitle())
                .likeIfPresent(TeacherDO::getEducation, reqVO.getEducation())
                .eqIfPresent(TeacherDO::getDeptId, reqVO.getDeptId())
                .eqIfPresent(TeacherDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(TeacherDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(TeacherDO::getId));
    }

}
