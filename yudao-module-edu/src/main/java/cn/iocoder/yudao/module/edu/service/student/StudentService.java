package cn.iocoder.yudao.module.edu.service.student;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.StudentImportExcelVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.StudentImportRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.StudentPageReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.StudentSaveReqVO;
import cn.iocoder.yudao.module.edu.dal.dataobject.dict.DictMajorDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.grade.ClassDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.grade.GradeDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.student.StudentDO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 学生 Service 接口
 */
public interface StudentService {

    /**
     * 创建学生
     *
     * 创建成功后会通过 system 模块的 AdminUserApi 自动建号（用户名 = 密码 = 学号），并回写 user_id。
     *
     * @param createReqVO 创建信息
     * @return 学生编号
     */
    Long createStudent(StudentSaveReqVO createReqVO);

    /**
     * 更新学生
     *
     * @param updateReqVO 更新信息
     */
    void updateStudent(StudentSaveReqVO updateReqVO);

    /**
     * 删除学生
     *
     * @param id 学生编号
     */
    void deleteStudent(Long id);

    /**
     * 获得学生
     *
     * @param id 学生编号
     * @return 学生
     */
    StudentDO getStudent(Long id);

    /**
     * 按系统账号编号获得学生（非学生账号返回 null）
     *
     * 供「仅本人数据」过滤使用：学生登录后只能看到自己的档案与变更记录。
     *
     * @param userId 系统账号编号，可为空
     * @return 学生
     */
    StudentDO getStudentByUserId(Long userId);

    /**
     * 获得学生分页
     *
     * @param pageReqVO 分页查询
     * @return 学生分页
     */
    PageResult<StudentDO> getStudentPage(StudentPageReqVO pageReqVO);

    /**
     * 获得学生编号集合对应的学生列表
     *
     * @param ids 学生编号集合
     * @return 学生列表
     */
    List<StudentDO> getStudentListByIds(Collection<Long> ids);

    /**
     * 获得某学生的标签编号列表
     *
     * @param studentId 学生编号
     * @return 标签编号列表
     */
    List<Long> getStudentTagIds(Long studentId);

    /**
     * 批量获得学生编号与标签编号列表的 Map
     *
     * @param studentIds 学生编号集合
     * @return 学生编号与标签编号列表的 Map
     */
    Map<Long, List<Long>> getStudentTagIdsMap(Collection<Long> studentIds);

    /**
     * 获得全部年级（供学生表单下拉）
     *
     * @return 年级列表
     */
    List<GradeDO> getGradeList();

    /**
     * 获得班级列表（供学生表单下拉）
     *
     * @param gradeId 年级编号，可为空（为空时返回全部班级）
     * @return 班级列表
     */
    List<ClassDO> getClassList(Long gradeId);

    /**
     * 获得专业列表（供学生表单下拉，专业依赖年级联动）
     *
     * 数据源当前为占位表 dict_major。
     * TODO: 字典中心模块待接入
     *
     * @param gradeId 年级编号，可为空（为空时返回全部专业）
     * @return 专业列表
     */
    List<DictMajorDO> getMajorList(Long gradeId);

    /**
     * 批量获得年级列表（供 Controller 拼接年级名称，避免 Controller 直接访问 DAL）
     *
     * @param ids 年级编号集合
     * @return 年级列表
     */
    List<GradeDO> getGradeListByIds(Collection<Long> ids);

    /**
     * 批量获得班级列表（供 Controller 拼接班级名称，避免 Controller 直接访问 DAL）
     *
     * @param ids 班级编号集合
     * @return 班级列表
     */
    List<ClassDO> getClassListByIds(Collection<Long> ids);

    /**
     * 导入学生
     *
     * 逐条独立处理：合格的自动建号（用户名 = 密码 = 学号）并分配基础角色后落库；
     * 不合格的（学号不满足账号规则、学制非法、出生日期为未来值、学号重复等）跳过并继续，
     * 失败原因带「第 N 行」提示返回给前端。
     *
     * @param importStudents 导入的学生列表
     * @param isUpdateSupport 是否支持更新（学号已存在时）
     * @return 导入结果
     */
    StudentImportRespVO importStudentList(List<StudentImportExcelVO> importStudents, boolean isUpdateSupport);

}
