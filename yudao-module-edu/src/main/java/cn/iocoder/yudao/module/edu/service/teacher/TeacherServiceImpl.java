package cn.iocoder.yudao.module.edu.service.teacher;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.edu.controller.admin.teacher.vo.TeacherPageReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.teacher.vo.TeacherSaveReqVO;
import cn.iocoder.yudao.module.edu.dal.dataobject.teacher.TeacherDO;
import cn.iocoder.yudao.module.edu.dal.mysql.teacher.TeacherMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.TEACHER_JOB_NO_DUPLICATE;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.TEACHER_NOT_EXISTS;

/**
 * 教师 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class TeacherServiceImpl implements TeacherService {

    @Resource
    private TeacherMapper teacherMapper;

    @Override
    public Long createTeacher(TeacherSaveReqVO createReqVO) {
        // 1. 校验工号唯一
        validateJobNoUnique(null, createReqVO.getJobNo());
        // 2. 插入教师
        TeacherDO teacher = BeanUtils.toBean(createReqVO, TeacherDO.class);
        try {
            teacherMapper.insert(teacher);
        } catch (DuplicateKeyException e) {
            throw exception(TEACHER_JOB_NO_DUPLICATE);
        }
        return teacher.getId();
    }

    @Override
    public void updateTeacher(TeacherSaveReqVO updateReqVO) {
        // 1. 校验教师存在
        validateTeacherExists(updateReqVO.getId());
        // 2. 校验工号唯一
        validateJobNoUnique(updateReqVO.getId(), updateReqVO.getJobNo());
        // 3. 更新教师
        try {
            if (teacherMapper.updateById(BeanUtils.toBean(updateReqVO, TeacherDO.class)) != 1) {
            throw exception(TEACHER_NOT_EXISTS);
        }
        } catch (DuplicateKeyException e) {
            throw exception(TEACHER_JOB_NO_DUPLICATE);
        }
    }

    @Override
    public void deleteTeacher(Long id) {
        // 1. 校验教师存在
        validateTeacherExists(id);
        // 2. 删除教师
        if (teacherMapper.deleteById(id) != 1) {
            throw exception(TEACHER_NOT_EXISTS);
        }
    }

    @Override
    public TeacherDO getTeacher(Long id) {
        return teacherMapper.selectById(id);
    }

    @Override
    public PageResult<TeacherDO> getTeacherPage(TeacherPageReqVO pageReqVO) {
        return teacherMapper.selectPage(pageReqVO);
    }

    private void validateTeacherExists(Long id) {
        if (teacherMapper.selectById(id) == null) {
            throw exception(TEACHER_NOT_EXISTS);
        }
    }

    private void validateJobNoUnique(Long id, String jobNo) {
        if (jobNo == null) {
            return;
        }
        TeacherDO teacher = teacherMapper.selectByJobNo(jobNo);
        if (teacher == null) {
            return;
        }
        if (id == null || !teacher.getId().equals(id)) {
            throw exception(TEACHER_JOB_NO_DUPLICATE);
        }
    }

}
