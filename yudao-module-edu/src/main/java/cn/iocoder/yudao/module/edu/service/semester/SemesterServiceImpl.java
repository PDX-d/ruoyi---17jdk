package cn.iocoder.yudao.module.edu.service.semester;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.edu.controller.admin.semester.vo.SemesterCalendarSaveReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.semester.vo.SemesterPageReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.semester.vo.SemesterSaveReqVO;
import cn.iocoder.yudao.module.edu.dal.dataobject.semester.SemesterCalendarDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.semester.SemesterDO;
import cn.iocoder.yudao.module.edu.dal.mysql.semester.SemesterCalendarMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.semester.SemesterMapper;
import cn.iocoder.yudao.module.edu.enums.semester.SemesterStatusEnum;
import cn.iocoder.yudao.module.edu.enums.semester.SemesterTermEnum;
import cn.iocoder.yudao.module.edu.enums.student.TagColorEnum;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.SEMESTER_ARCHIVED;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.SEMESTER_CALENDAR_COLOR_INVALID;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.SEMESTER_CALENDAR_DATE_INVALID;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.SEMESTER_CALENDAR_DATE_OUT_OF_RANGE;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.SEMESTER_CALENDAR_NOT_EXISTS;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.SEMESTER_CURRENT_CANNOT_ARCHIVE;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.SEMESTER_NOT_EXISTS;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.SEMESTER_SCHOOL_YEAR_TERM_DUPLICATE;

/**
 * 学期 Service 实现类
 *
 * 学期名称 / 学年 / 当前学期 / 状态 / 归档均由日期推导，不接收前端传值：
 * 1. 名称 = 学院名称 + 学年 + 学期；学年 = 起始年份 + 结束年份；
 * 2. 当前学期按「开始日期 <= 今天 <= 结束日期」判定并保证全局唯一；
 * 3. 状态按日期推导（0 未开始 / 1 进行中 / 2 已结束），已结束自动归档；
 * 4. 「学年 + 学期」唯一；归档后（archived = 1）学期及其校历节点只读。
 */
@Service
@Validated
@Slf4j
public class SemesterServiceImpl implements SemesterService {

    /**
     * 学院名称后缀：顶级院系名若不以「学院」结尾，自动补齐
     */
    private static final String COLLEGE_NAME_SUFFIX = "学院";

    @Resource
    private SemesterMapper semesterMapper;

    @Resource
    private SemesterCalendarMapper semesterCalendarMapper;

    @Resource
    private DeptApi deptApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSemester(SemesterSaveReqVO createReqVO) {
        // 1. 归一化学年 / 学期名称（不信任前端传值）
        String schoolYear = buildSchoolYear(createReqVO.getSchoolYearStart(), createReqVO.getSchoolYearEnd());
        // 2. 校验「学年 + 学期」唯一
        validateSchoolYearTermUnique(null, schoolYear, createReqVO.getTerm());
        // 3. 插入学期：状态 / 当前学期 / 归档全部由日期推导
        SemesterDO semester = BeanUtils.toBean(createReqVO, SemesterDO.class);
        semester.setSchoolYear(schoolYear);
        semester.setName(buildSemesterName(schoolYear, createReqVO.getTerm()));
        applyDerivedState(semester);
        try {
            semesterMapper.insert(semester);
        } catch (DuplicateKeyException e) {
            throw exception(SEMESTER_SCHOOL_YEAR_TERM_DUPLICATE);
        }
        // 4. 落库后重算全表：本条可能成为「当前学期」，也可能让旧学期退役
        refreshSemesterStates();
        return semester.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSemester(SemesterSaveReqVO updateReqVO) {
        // 1. 校验学期存在，且未归档（归档后只读）
        validateSemesterNotArchived(updateReqVO.getId());
        // 2. 归一化学年 / 学期名称（不信任前端传值）
        String schoolYear = buildSchoolYear(updateReqVO.getSchoolYearStart(), updateReqVO.getSchoolYearEnd());
        // 3. 校验「学年 + 学期」唯一
        validateSchoolYearTermUnique(updateReqVO.getId(), schoolYear, updateReqVO.getTerm());
        // 4. 更新学期：状态 / 当前学期 / 归档全部由日期推导
        SemesterDO updateObj = BeanUtils.toBean(updateReqVO, SemesterDO.class);
        updateObj.setSchoolYear(schoolYear);
        updateObj.setName(buildSemesterName(schoolYear, updateReqVO.getTerm()));
        applyDerivedState(updateObj);
        try {
            if (semesterMapper.updateById(updateObj) != 1) {
                throw exception(SEMESTER_NOT_EXISTS);
            }
        } catch (DuplicateKeyException e) {
            throw exception(SEMESTER_SCHOOL_YEAR_TERM_DUPLICATE);
        }
        // 5. 落库后重算全表，保证「当前学期」唯一且与日期一致
        refreshSemesterStates();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSemester(Long id) {
        // 1. 校验学期存在，且未归档（归档后只读）
        validateSemesterNotArchived(id);
        // 2. 先删除学期下的校历节点，避免孤儿数据
        semesterCalendarMapper.deleteBySemesterId(id);
        // 3. 删除学期
        if (semesterMapper.deleteById(id) != 1) {
            throw exception(SEMESTER_NOT_EXISTS);
        }
        // 4. 重算全表（被删的若原为当前学期，需要把标记交给其它符合条件的学期）
        refreshSemesterStates();
    }

    @Override
    public SemesterDO getSemester(Long id) {
        return fillDerivedFields(semesterMapper.selectById(id));
    }

    @Override
    public PageResult<SemesterDO> getSemesterPage(SemesterPageReqVO pageReqVO) {
        PageResult<SemesterDO> pageResult = semesterMapper.selectPage(pageReqVO);
        // 状态由起止日期动态推导，保证列表展示的状态始终与当前日期一致
        pageResult.getList().forEach(this::fillDerivedFields);
        return pageResult;
    }

    @Override
    public List<SemesterCalendarDO> getCalendarList(Long semesterId) {
        return semesterCalendarMapper.selectListBySemesterId(semesterId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCalendar(SemesterCalendarSaveReqVO createReqVO) {
        // 1. 校验所属学期存在，且未归档（归档后只读）
        SemesterDO semester = validateSemesterNotArchived(createReqVO.getSemesterId());
        // 2. 校验节点日期落在学期区间内、颜色为固定 16 色
        validateCalendarReq(createReqVO, semester);
        // 3. 插入校历节点（周次由后端按开始日期自动推导，避免前端算错）
        SemesterCalendarDO calendar = BeanUtils.toBean(createReqVO, SemesterCalendarDO.class);
        calendar.setWeekNo(calculateWeekNo(semester, calendar.getStartDate(), calendar.getWeekNo()));
        semesterCalendarMapper.insert(calendar);
        return calendar.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCalendar(SemesterCalendarSaveReqVO updateReqVO) {
        // 1. 校验校历节点存在
        validateCalendarExists(updateReqVO.getId());
        // 2. 校验所属学期存在，且未归档（归档后只读）
        SemesterDO semester = validateSemesterNotArchived(updateReqVO.getSemesterId());
        // 3. 校验节点日期落在学期区间内、颜色为固定 16 色
        validateCalendarReq(updateReqVO, semester);
        // 4. 更新校历节点
        SemesterCalendarDO updateObj = BeanUtils.toBean(updateReqVO, SemesterCalendarDO.class);
        updateObj.setWeekNo(calculateWeekNo(semester, updateObj.getStartDate(), updateObj.getWeekNo()));
        if (semesterCalendarMapper.updateById(updateObj) != 1) {
            throw exception(SEMESTER_CALENDAR_NOT_EXISTS);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCalendar(Long id) {
        // 1. 校验校历节点存在
        SemesterCalendarDO calendar = semesterCalendarMapper.selectById(id);
        if (calendar == null) {
            throw exception(SEMESTER_CALENDAR_NOT_EXISTS);
        }
        // 2. 校验所属学期未归档（归档后只读）
        validateSemesterNotArchived(calendar.getSemesterId());
        // 3. 删除校历节点
        if (semesterCalendarMapper.deleteById(id) != 1) {
            throw exception(SEMESTER_CALENDAR_NOT_EXISTS);
        }
    }

    @Override
    public SemesterDO getCurrentSemester() {
        return fillDerivedFields(semesterMapper.selectCurrent());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void archiveSemester(Long id, Boolean archived) {
        // 1. 校验学期存在
        validateSemesterExists(id);
        // 2. 当前学期不允许归档，避免把正在使用的学期置为只读
        if (Boolean.TRUE.equals(archived)) {
            SemesterDO current = semesterMapper.selectCurrent();
            if (current != null && current.getId().equals(id)) {
                throw exception(SEMESTER_CURRENT_CANNOT_ARCHIVE);
            }
        }
        // 3. 仅更新归档标记，避免覆盖其它字段
        SemesterDO updateObj = new SemesterDO();
        updateObj.setId(id);
        updateObj.setArchived(Boolean.TRUE.equals(archived));
        semesterMapper.updateById(updateObj);
    }

    @Override
    public String getCollegeName() {
        // 取当前租户下的顶级院系作为「学院」。框架自带的演示院系建立时间更早，
        // 故按编号取最大的顶级院系，避免把框架演示名当成学院名。
        List<DeptRespDTO> rootDepts = deptApi.getRootDeptList();
        String name = CollUtil.isEmpty(rootDepts) ? null
                : rootDepts.stream()
                        .filter(dept -> dept.getId() != null)
                        .max(Comparator.comparing(DeptRespDTO::getId))
                        .map(DeptRespDTO::getName)
                        .orElse(null);
        if (StrUtil.isBlank(name)) {
            // 未维护顶级院系时兜底返回空串，学期名称将退化为「2025-2026学年第一学期」
            return StrUtil.EMPTY;
        }
        // 顶级院系名通常不含「学院」二字（如「能工智人」），这里统一补齐后缀
        return name.endsWith(COLLEGE_NAME_SUFFIX) ? name : name + COLLEGE_NAME_SUFFIX;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refreshSemesterStates() {
        LocalDate today = LocalDate.now();
        List<SemesterDO> allList = semesterMapper.selectListAll();
        if (CollUtil.isEmpty(allList)) {
            return;
        }
        // 1. 状态重算：仅当与库中不一致时才写库，避免无意义的更新
        for (SemesterDO semester : allList) {
            Integer status = SemesterStatusEnum.calculate(semester.getStartDate(), semester.getEndDate(), today);
            if (!Objects.equals(status, semester.getStatus())) {
                SemesterDO updateObj = new SemesterDO();
                updateObj.setId(semester.getId());
                updateObj.setStatus(status);
                semesterMapper.updateById(updateObj);
                semester.setStatus(status);
            }
        }
        // 2. 当前学期标记重算：找出「今天落在其区间内」的学期作为唯一当前学期
        SemesterDO shouldCurrent = allList.stream().filter(semester -> isWithin(semester, today)).findFirst().orElse(null);
        for (SemesterDO semester : allList) {
            boolean expected = shouldCurrent != null && shouldCurrent.getId().equals(semester.getId());
            if (!Objects.equals(Boolean.TRUE.equals(semester.getIsCurrent()), expected)) {
                SemesterDO updateObj = new SemesterDO();
                updateObj.setId(semester.getId());
                updateObj.setIsCurrent(expected);
                semesterMapper.updateById(updateObj);
                semester.setIsCurrent(expected);
            }
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 拼装学年字符串（2025 + 2026 → 2025-2026）
     */
    private String buildSchoolYear(Integer startYear, Integer endYear) {
        return startYear + "-" + endYear;
    }

    /**
     * 拼装学期名称：学院名称 + 学年 + 学期（如「能工智人学院2025-2026学年第一学期」）
     */
    private String buildSemesterName(String schoolYear, Integer term) {
        String termName = SemesterTermEnum.getName(term);
        // 学期名兜底为「第一学期」，避免 term 非法时出现 null
        return getCollegeName() + schoolYear + "学年" + (termName != null ? termName : "第一学期");
    }

    /**
     * 依据日期推导并写入「状态 / 当前学期 / 归档」三个派生字段
     *
     * 归档规则：已结束的学期自动归档（只读），未开始 / 进行中不归档；人工可取消归档纠偏。
     *
     * @param semester 待落库的学期对象
     */
    private void applyDerivedState(SemesterDO semester) {
        LocalDate today = LocalDate.now();
        semester.setStatus(SemesterStatusEnum.calculate(semester.getStartDate(), semester.getEndDate(), today));
        semester.setIsCurrent(isWithin(semester, today));
        semester.setArchived(Objects.equals(semester.getStatus(), SemesterStatusEnum.FINISHED.getStatus()));
    }

    /**
     * 判断今天是否落在学期的起止区间内（即「当前学期」的判定条件）
     */
    private boolean isWithin(SemesterDO semester, LocalDate today) {
        return semester.getStartDate() != null && semester.getEndDate() != null
                && !today.isBefore(semester.getStartDate()) && !today.isAfter(semester.getEndDate());
    }

    /**
     * 填充派生字段：状态 + 学年起止年份（供前端展示与表单回显）
     */
    private SemesterDO fillDerivedFields(SemesterDO semester) {
        if (semester == null) {
            return null;
        }
        semester.setStatus(SemesterStatusEnum.calculate(semester.getStartDate(), semester.getEndDate(), LocalDate.now()));
        // 学年 → 起止年份（2025-2026 → 2025 / 2026），供表单双年份下拉回显
        if (StrUtil.isNotBlank(semester.getSchoolYear()) && semester.getSchoolYear().contains("-")) {
            String[] parts = semester.getSchoolYear().split("-");
            if (parts.length == 2) {
                semester.setSchoolYearStart(parseIntQuietly(parts[0]));
                semester.setSchoolYearEnd(parseIntQuietly(parts[1]));
            }
        }
        return semester;
    }

    private Integer parseIntQuietly(String value) {
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 计算教学周次：以学期开始日期为第 1 周起点，每 7 天为 1 周
     *
     * 前端已显式传入 weekNo 时尊重传入值。
     *
     * @param semester  所属学期
     * @param startDate 节点开始日期
     * @param weekNo    前端传入的周次（可为空）
     * @return 教学周次
     */
    private Integer calculateWeekNo(SemesterDO semester, LocalDate startDate, Integer weekNo) {
        if (weekNo != null || startDate == null || semester.getStartDate() == null) {
            return weekNo;
        }
        // 与学期开始日期的天数差 / 7 + 1；早于学期开始日期时按第 1 周处理
        long days = java.time.temporal.ChronoUnit.DAYS.between(semester.getStartDate(), startDate);
        return days < 0 ? 1 : (int) (days / 7 + 1);
    }

    private SemesterDO validateSemesterExists(Long id) {
        SemesterDO semester = semesterMapper.selectById(id);
        if (semester == null) {
            throw exception(SEMESTER_NOT_EXISTS);
        }
        return semester;
    }

    /**
     * 校验学期存在且未归档（归档后只读）
     *
     * @param id 学期编号
     * @return 学期
     */
    private SemesterDO validateSemesterNotArchived(Long id) {
        SemesterDO semester = validateSemesterExists(id);
        if (Boolean.TRUE.equals(semester.getArchived())) {
            throw exception(SEMESTER_ARCHIVED);
        }
        return semester;
    }

    /**
     * 校验校历节点的日期区间与颜色
     *
     * 1. 节点结束日期不得早于开始日期；
     * 2. 节点日期必须落在所属学期的起止区间内（校历的最小/最大编辑日期即学期起止日期）；
     * 3. 颜色必须为固定 16 色之一（与标签共用同一套色板，禁止自由输入）。
     */
    private void validateCalendarReq(SemesterCalendarSaveReqVO reqVO, SemesterDO semester) {
        LocalDate startDate = reqVO.getStartDate();
        LocalDate endDate = reqVO.getEndDate();
        // 1. 结束日期不得早于开始日期（允许同一天，表示单日节点）
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw exception(SEMESTER_CALENDAR_DATE_INVALID);
        }
        // 2. 不得超出学期区间
        if (startDate != null && (semester.getStartDate() == null || semester.getEndDate() == null
                || startDate.isBefore(semester.getStartDate()) || startDate.isAfter(semester.getEndDate()))) {
            throw exception(SEMESTER_CALENDAR_DATE_OUT_OF_RANGE, semester.getStartDate(), semester.getEndDate());
        }
        if (endDate != null && (semester.getStartDate() == null || semester.getEndDate() == null
                || endDate.isBefore(semester.getStartDate()) || endDate.isAfter(semester.getEndDate()))) {
            throw exception(SEMESTER_CALENDAR_DATE_OUT_OF_RANGE, semester.getStartDate(), semester.getEndDate());
        }
        // 3. 颜色必须来自固定 16 色
        if (StrUtil.isNotBlank(reqVO.getColor()) && !TagColorEnum.isValid(reqVO.getColor())) {
            throw exception(SEMESTER_CALENDAR_COLOR_INVALID);
        }
    }

    private void validateCalendarExists(Long id) {
        if (semesterCalendarMapper.selectById(id) == null) {
            throw exception(SEMESTER_CALENDAR_NOT_EXISTS);
        }
    }

    /**
     * 校验「学年 + 学期」唯一
     *
     * @param id         学期编号（新增时为 null）
     * @param schoolYear 学年
     * @param term       学期
     */
    private void validateSchoolYearTermUnique(Long id, String schoolYear, Integer term) {
        if (schoolYear == null || term == null) {
            return;
        }
        SemesterDO semester = semesterMapper.selectBySchoolYearAndTerm(schoolYear, term);
        if (semester == null) {
            return;
        }
        if (id == null || !semester.getId().equals(id)) {
            throw exception(SEMESTER_SCHOOL_YEAR_TERM_DUPLICATE);
        }
    }

}
