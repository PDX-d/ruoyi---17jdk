package cn.iocoder.yudao.module.edu.dal.mysql.student;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.StudentChangeRecordPageReqVO;
import cn.iocoder.yudao.module.edu.dal.dataobject.student.StudentChangeRecordDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 学生变更记录 Mapper
 */
@Mapper
public interface StudentChangeRecordMapper extends BaseMapperX<StudentChangeRecordDO> {

    /**
     * 查询某学生的变更记录列表
     *
     * @param studentId 学生编号
     * @return 变更记录列表
     */
    default List<StudentChangeRecordDO> selectListByStudentId(Long studentId) {
        return selectList(StudentChangeRecordDO::getStudentId, studentId);
    }

    /**
     * 变更记录分页查询
     *
     * @param reqVO 分页查询条件
     * @return 变更记录分页
     */
    default PageResult<StudentChangeRecordDO> selectPage(StudentChangeRecordPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<StudentChangeRecordDO>()
                .eqIfPresent(StudentChangeRecordDO::getStudentId, reqVO.getStudentId())
                .eqIfPresent(StudentChangeRecordDO::getApprovalStatus, reqVO.getApprovalStatus())
                .eqIfPresent(StudentChangeRecordDO::getChangeType, reqVO.getChangeType())
                .orderByDesc(StudentChangeRecordDO::getId));
    }

}
