package cn.iocoder.yudao.module.edu.service.student;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.StudentChangeRecordPageReqVO;
import cn.iocoder.yudao.module.edu.dal.dataobject.student.StudentChangeRecordDO;

import java.util.List;

/**
 * 学生变更记录 Service 接口
 *
 * 仅提供变更记录的「查询」能力；「提交与审批」收敛在 StudentApprovalService。
 */
public interface StudentChangeRecordService {

    /**
     * 获得学生变更记录分页
     *
     * @param pageReqVO 分页查询
     * @return 变更记录分页
     */
    PageResult<StudentChangeRecordDO> getChangeRecordPage(StudentChangeRecordPageReqVO pageReqVO);

    /**
     * 获得某学生的变更记录列表
     *
     * @param studentId 学生编号
     * @return 变更记录列表
     */
    List<StudentChangeRecordDO> getChangeRecordList(Long studentId);

    /**
     * 获得变更记录
     *
     * @param id 变更记录编号
     * @return 变更记录
     */
    StudentChangeRecordDO getChangeRecord(Long id);

}
