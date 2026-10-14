package cn.iocoder.yudao.module.edu.service.teacher;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.edu.controller.admin.teacher.vo.TeacherPageReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.teacher.vo.TeacherSaveReqVO;
import cn.iocoder.yudao.module.edu.dal.dataobject.teacher.TeacherDO;

/**
 * 教师 Service 接口
 *
 * @author 芋道源码
 */
public interface TeacherService {

    /**
     * 创建教师
     *
     * @param createReqVO 创建信息
     * @return 教师编号
     */
    Long createTeacher(TeacherSaveReqVO createReqVO);

    /**
     * 更新教师
     *
     * @param updateReqVO 更新信息
     */
    void updateTeacher(TeacherSaveReqVO updateReqVO);

    /**
     * 删除教师
     *
     * @param id 教师编号
     */
    void deleteTeacher(Long id);

    /**
     * 获得教师
     *
     * @param id 教师编号
     * @return 教师
     */
    TeacherDO getTeacher(Long id);

    /**
     * 获得教师分页
     *
     * @param pageReqVO 分页查询
     * @return 教师分页
     */
    PageResult<TeacherDO> getTeacherPage(TeacherPageReqVO pageReqVO);

}
