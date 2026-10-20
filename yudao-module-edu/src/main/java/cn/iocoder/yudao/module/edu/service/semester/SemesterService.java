package cn.iocoder.yudao.module.edu.service.semester;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.edu.controller.admin.semester.vo.SemesterCalendarSaveReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.semester.vo.SemesterPageReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.semester.vo.SemesterSaveReqVO;
import cn.iocoder.yudao.module.edu.dal.dataobject.semester.SemesterCalendarDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.semester.SemesterDO;

import java.util.List;

/**
 * 学期 Service 接口
 */
public interface SemesterService {

    /**
     * 创建学期
     *
     * @param createReqVO 创建信息
     * @return 学期编号
     */
    Long createSemester(SemesterSaveReqVO createReqVO);

    /**
     * 更新学期
     *
     * @param updateReqVO 更新信息
     */
    void updateSemester(SemesterSaveReqVO updateReqVO);

    /**
     * 删除学期
     *
     * @param id 学期编号
     */
    void deleteSemester(Long id);

    /**
     * 获得学期
     *
     * @param id 学期编号
     * @return 学期
     */
    SemesterDO getSemester(Long id);

    /**
     * 获得学期分页
     *
     * @param pageReqVO 分页查询
     * @return 学期分页
     */
    PageResult<SemesterDO> getSemesterPage(SemesterPageReqVO pageReqVO);

    /**
     * 获得某学期的校历节点列表
     *
     * @param semesterId 学期编号
     * @return 校历节点列表
     */
    List<SemesterCalendarDO> getCalendarList(Long semesterId);

    /**
     * 创建校历节点
     *
     * @param createReqVO 创建信息
     * @return 校历节点编号
     */
    Long createCalendar(SemesterCalendarSaveReqVO createReqVO);

    /**
     * 更新校历节点
     *
     * @param updateReqVO 更新信息
     */
    void updateCalendar(SemesterCalendarSaveReqVO updateReqVO);

    /**
     * 删除校历节点
     *
     * @param id 校历节点编号
     */
    void deleteCalendar(Long id);

    /**
     * 归档 / 取消归档学期（归档后数据只读）
     *
     * 保留给人工纠偏使用；学期保存时会按日期自动归档历史学期。
     *
     * @param id       学期编号
     * @param archived 是否归档
     */
    void archiveSemester(Long id, Boolean archived);

    /**
     * 获得当前学期
     *
     * @return 当前学期，可能为 null
     */
    SemesterDO getCurrentSemester();

    /**
     * 获得学院名称（用于学期名称自动拼装，如「能工智人学院」）
     *
     * 取当前租户下的顶级院系名称；若名称不以「学院」结尾则自动补「学院」后缀。
     *
     * @return 学院名称
     */
    String getCollegeName();

    /**
     * 依据当前日期重算所有学期的「状态」与「当前学期」标记
     *
     * 由学期保存后与定时任务调用；不修改归档标记，避免覆盖人工纠偏。
     */
    void refreshSemesterStates();

}
