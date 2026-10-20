package cn.iocoder.yudao.module.edu.dal.mysql.student;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.StudentPageReqVO;
import cn.iocoder.yudao.module.edu.dal.dataobject.student.StudentDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学生 Mapper
 */
@Mapper
public interface StudentMapper extends BaseMapperX<StudentDO> {

    /**
     * 按学号查询学生
     *
     * @param studentNo 学号
     * @return 学生
     */
    default StudentDO selectByStudentNo(String studentNo) {
        return selectOne(StudentDO::getStudentNo, studentNo);
    }

    /**
     * 按系统账号编号查询学生（用于「仅本人数据」过滤）
     *
     * @param userId 系统账号编号
     * @return 学生
     */
    default StudentDO selectByUserId(Long userId) {
        return selectOne(StudentDO::getUserId, userId);
    }

    /**
     * 学生分页查询
     *
     * @param reqVO 分页查询条件
     * @return 学生分页
     */
    default PageResult<StudentDO> selectPage(StudentPageReqVO reqVO) {
        LambdaQueryWrapperX<StudentDO> wrapper = new LambdaQueryWrapperX<StudentDO>()
                .likeIfPresent(StudentDO::getStudentNo, reqVO.getStudentNo())
                .likeIfPresent(StudentDO::getName, reqVO.getName())
                .eqIfPresent(StudentDO::getGender, reqVO.getGender())
                .eqIfPresent(StudentDO::getStatus, reqVO.getStatus())
                .eqIfPresent(StudentDO::getGradeId, reqVO.getGradeId())
                .eqIfPresent(StudentDO::getClassId, reqVO.getClassId())
                .eqIfPresent(StudentDO::getDeptId, reqVO.getDeptId())
                .likeIfPresent(StudentDO::getMobile, reqVO.getMobile())
                .eqIfPresent(StudentDO::getPoliticalStatus, reqVO.getPoliticalStatus())
                .eqIfPresent(StudentDO::getEnrollmentYear, reqVO.getEnrollmentYear())
                // 仅本人数据：由 Service 在学生登录时注入 userId
                .eqIfPresent(StudentDO::getUserId, reqVO.getUserId())
                .betweenIfPresent(StudentDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(StudentDO::getId);
        // 按标签筛选：用 EXISTS 子查询，避免与分页 count 的 join 冲突
        wrapper.apply(reqVO.getTagId() != null,
                "EXISTS (SELECT 1 FROM edu_student_tag_rel rel"
                        + " WHERE rel.student_id = edu_student.id AND rel.deleted = b'0' AND rel.tag_id = {0})",
                reqVO.getTagId());
        return selectPage(reqVO, wrapper);
    }

}
