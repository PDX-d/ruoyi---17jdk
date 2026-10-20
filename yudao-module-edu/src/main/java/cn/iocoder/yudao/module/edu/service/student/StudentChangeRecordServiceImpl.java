package cn.iocoder.yudao.module.edu.service.student;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.StudentChangeRecordPageReqVO;
import cn.iocoder.yudao.module.edu.dal.dataobject.student.StudentChangeRecordDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.student.StudentDO;
import cn.iocoder.yudao.module.edu.dal.mysql.student.StudentChangeRecordMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

/**
 * 学生变更记录 Service 实现类
 */
@Service
@Validated
public class StudentChangeRecordServiceImpl implements StudentChangeRecordService {

    @Resource
    private StudentChangeRecordMapper studentChangeRecordMapper;

    @Resource
    private StudentService studentService;

    @Override
    public PageResult<StudentChangeRecordDO> getChangeRecordPage(StudentChangeRecordPageReqVO pageReqVO) {
        // 「仅本人数据」过滤：学生登录后只能查看自己的变更记录
        StudentDO loginStudent = studentService.getStudentByUserId(SecurityFrameworkUtils.getLoginUserId());
        if (loginStudent != null) {
            pageReqVO.setStudentId(loginStudent.getId());
        }
        return studentChangeRecordMapper.selectPage(pageReqVO);
    }

    @Override
    public List<StudentChangeRecordDO> getChangeRecordList(Long studentId) {
        return studentChangeRecordMapper.selectListByStudentId(studentId);
    }

    @Override
    public StudentChangeRecordDO getChangeRecord(Long id) {
        return studentChangeRecordMapper.selectById(id);
    }

}
