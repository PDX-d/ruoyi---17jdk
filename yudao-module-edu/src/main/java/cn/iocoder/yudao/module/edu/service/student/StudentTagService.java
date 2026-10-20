package cn.iocoder.yudao.module.edu.service.student;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.StudentTagPageReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.StudentTagSaveReqVO;
import cn.iocoder.yudao.module.edu.dal.dataobject.student.StudentTagDO;

import java.util.Collection;
import java.util.List;

/**
 * 学生标签 Service 接口
 */
public interface StudentTagService {

    /**
     * 创建学生标签
     *
     * @param createReqVO 创建信息
     * @return 标签编号
     */
    Long createStudentTag(StudentTagSaveReqVO createReqVO);

    /**
     * 更新学生标签
     *
     * @param updateReqVO 更新信息
     */
    void updateStudentTag(StudentTagSaveReqVO updateReqVO);

    /**
     * 删除学生标签
     *
     * @param id 标签编号
     */
    void deleteStudentTag(Long id);

    /**
     * 批量重排学生标签（拖拽排序）
     *
     * <p>前端提交拖拽后的完整顺序，后端校验其必须是「当前全部标签」的一个完整排列（不重不漏），
     * 再批量重写 sort。
     *
     * @param orderedTagIds 拖拽后的标签编号顺序
     */
    void reorderStudentTags(List<Long> orderedTagIds);

    /**
     * 获得学生标签
     *
     * @param id 标签编号
     * @return 标签
     */
    StudentTagDO getStudentTag(Long id);

    /**
     * 获得全部学生标签
     *
     * @return 标签列表
     */
    List<StudentTagDO> getStudentTagList();

    /**
     * 获得学生标签分页
     *
     * @param pageReqVO 分页查询
     * @return 标签分页
     */
    PageResult<StudentTagDO> getStudentTagPage(StudentTagPageReqVO pageReqVO);

    /**
     * 按编号集合查询学生标签
     *
     * @param ids 标签编号集合
     * @return 标签列表
     */
    List<StudentTagDO> getStudentTagListByIds(Collection<Long> ids);

}
