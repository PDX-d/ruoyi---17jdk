package cn.iocoder.yudao.module.edu.service.student;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.StudentImportExcelVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.StudentImportRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.StudentPageReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.StudentSaveReqVO;
import cn.iocoder.yudao.module.edu.dal.dataobject.dict.DictMajorDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.grade.ClassDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.grade.GradeDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.student.StudentDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.student.StudentTagDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.student.StudentTagRelDO;
import cn.iocoder.yudao.module.edu.dal.mysql.dict.DictMajorMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.grade.ClassMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.grade.GradeMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.student.StudentMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.student.StudentTagRelMapper;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.permission.PermissionApi;
import cn.iocoder.yudao.module.system.api.permission.RoleApi;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserCreateReqDTO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_BIRTH_DATE_FUTURE;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_CLASS_NOT_EXISTS;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_EDUCATION_SYSTEM_INVALID;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_GRADE_NOT_EXISTS;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_IMPORT_CLASS_NAME_NOT_EXISTS;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_IMPORT_DEPT_NAME_NOT_EXISTS;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_IMPORT_GRADE_NAME_NOT_EXISTS;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_IMPORT_LIST_EMPTY;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_IMPORT_MAJOR_NAME_NOT_EXISTS;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_MAJOR_NOT_EXISTS;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_NOT_EXISTS;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_NO_DUPLICATE;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_USER_CREATE_FAILED;

/**
 * 学生 Service 实现类
 *
 * 1. 创建学生时通过 system 模块 AdminUserApi 自动建号（用户名 = 密码 = 学号），建号失败则整体回滚；
 * 2. 标签关联使用物理删除维护（先物理删后批量插），见 StudentTagRelMapper#deleteByStudentId；
 * 3. Excel 导入逐条独立处理，不自动建号（见 importStudentList 内部注释）。
 */
@Service
@Validated
public class StudentServiceImpl implements StudentService {

    @Resource
    private StudentMapper studentMapper;

    @Resource
    private StudentTagRelMapper studentTagRelMapper;

    @Resource
    private StudentTagService studentTagService;

    @Resource
    private GradeMapper gradeMapper;

    @Resource
    private ClassMapper classMapper;

    @Resource
    private DictMajorMapper dictMajorMapper;

    @Resource
    private AdminUserApi adminUserApi;

    @Resource
    private RoleApi roleApi;

    @Resource
    private PermissionApi permissionApi;

    @Resource
    private DeptApi deptApi;

    @Resource
    private StudentChangeLogRecorder studentChangeLogRecorder;

    /** 学生登录账号自动分配的基础角色编码 */
    private static final String STUDENT_BASE_ROLE_CODE = "edu_student";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createStudent(StudentSaveReqVO createReqVO) {
        // 1. 校验学号唯一
        validateStudentNoUnique(null, createReqVO.getStudentNo());
        // 2. 校验年级、班级存在
        validateGradeAndClass(createReqVO.getGradeId(), createReqVO.getClassId());
        // 3. 插入学生
        StudentDO student = BeanUtils.toBean(createReqVO, StudentDO.class);
        // 3.1 校验专业存在并回填专业名称（专业为下拉选择，禁止自由输入）
        validateMajorAndFill(student, createReqVO.getMajorId());
        try {
            studentMapper.insert(student);
        } catch (DuplicateKeyException e) {
            throw exception(STUDENT_NO_DUPLICATE);
        }
        // 4. 自动建号（用户名 = 密码 = 学号），失败则回滚整个创建
        createStudentUser(student);
        // 5. 保存标签关联
        saveStudentTags(student.getId(), createReqVO.getTagIds());
        // 6. 记录变更日志（档案变更历史）
        studentChangeLogRecorder.recordCreate(student);
        return student.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStudent(StudentSaveReqVO updateReqVO) {
        // 1. 校验学生存在，并留存变更前的档案（用于变更日志 diff）
        StudentDO beforeStudent = studentMapper.selectById(updateReqVO.getId());
        if (beforeStudent == null) {
            throw exception(STUDENT_NOT_EXISTS);
        }
        // 2. 校验学号唯一（排除自身）
        validateStudentNoUnique(updateReqVO.getId(), updateReqVO.getStudentNo());
        // 3. 校验年级、班级存在
        validateGradeAndClass(updateReqVO.getGradeId(), updateReqVO.getClassId());
        // 4. 更新学生
        StudentDO updateStudent = BeanUtils.toBean(updateReqVO, StudentDO.class);
        // 4.1 校验专业存在并回填专业名称（专业为下拉选择，禁止自由输入）
        validateMajorAndFill(updateStudent, updateReqVO.getMajorId());
        try {
            if (studentMapper.updateById(updateStudent) != 1) {
                throw exception(STUDENT_NOT_EXISTS);
            }
        } catch (DuplicateKeyException e) {
            throw exception(STUDENT_NO_DUPLICATE);
        }
        // 5. 保存标签关联（先物理删后插）
        saveStudentTags(updateReqVO.getId(), updateReqVO.getTagIds());
        // 6. 记录变更日志（档案变更历史，逐字段 diff）
        studentChangeLogRecorder.recordUpdate(beforeStudent, updateStudent);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteStudent(Long id) {
        // 1. 校验学生存在，并留存档案（用于变更日志）
        StudentDO beforeStudent = studentMapper.selectById(id);
        if (beforeStudent == null) {
            throw exception(STUDENT_NOT_EXISTS);
        }
        // 2. 删除学生（逻辑删除）
        if (studentMapper.deleteById(id) != 1) {
            throw exception(STUDENT_NOT_EXISTS);
        }
        // 3. 物理删除该学生的标签关联
        studentTagRelMapper.deleteByStudentId(id);
        // 4. 记录变更日志（档案变更历史）
        studentChangeLogRecorder.recordDelete(beforeStudent);
    }

    @Override
    public StudentDO getStudent(Long id) {
        StudentDO student = studentMapper.selectById(id);
        // 学生本人只能查看自己的档案（矩阵：学生「仅本人数据」）
        StudentDO loginStudent = getLoginStudentOrNull();
        if (loginStudent != null
                && (student == null || !student.getId().equals(loginStudent.getId()))) {
            throw exception(STUDENT_NOT_EXISTS);
        }
        return student;
    }

    @Override
    public PageResult<StudentDO> getStudentPage(StudentPageReqVO pageReqVO) {
        // 「仅本人数据」过滤：若当前登录人本身就是学生（edu_student.user_id = 登录人），则强制只查自己。
        // 判断依据取自学生档案本身，不依赖角色编码（角色调整后依然生效），也不跨模块查询角色。
        StudentDO loginStudent = getLoginStudentOrNull();
        if (loginStudent != null) {
            pageReqVO.setUserId(loginStudent.getUserId());
        }
        return studentMapper.selectPage(pageReqVO);
    }

    @Override
    public List<StudentDO> getStudentListByIds(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return studentMapper.selectList(StudentDO::getId, ids);
    }

    @Override
    public List<Long> getStudentTagIds(Long studentId) {
        return CollectionUtils.convertList(studentTagRelMapper.selectListByStudentId(studentId),
                StudentTagRelDO::getTagId);
    }

    @Override
    public Map<Long, List<Long>> getStudentTagIdsMap(Collection<Long> studentIds) {
        if (CollUtil.isEmpty(studentIds)) {
            return Collections.emptyMap();
        }
        return CollectionUtils.convertMultiMap(studentTagRelMapper.selectListByStudentIds(studentIds),
                StudentTagRelDO::getStudentId, StudentTagRelDO::getTagId);
    }

    @Override
    public List<GradeDO> getGradeList() {
        return gradeMapper.selectListAll();
    }

    @Override
    public List<ClassDO> getClassList(Long gradeId) {
        if (gradeId == null) {
            return classMapper.selectList();
        }
        return classMapper.selectListByGradeId(gradeId);
    }

    @Override
    public List<DictMajorDO> getMajorList(Long gradeId) {
        // 数据源为专业表 dict_major
        // TODO: 字典中心模块待接入
        return dictMajorMapper.selectListByGradeId(gradeId);
    }

    @Override
    public List<GradeDO> getGradeListByIds(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return gradeMapper.selectList(GradeDO::getId, ids);
    }

    @Override
    public List<ClassDO> getClassListByIds(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return classMapper.selectList(ClassDO::getId, ids);
    }

    @Override
    public StudentImportRespVO importStudentList(List<StudentImportExcelVO> importStudents, boolean isUpdateSupport) {
        // 1. 参数校验
        if (CollUtil.isEmpty(importStudents)) {
            throw exception(STUDENT_IMPORT_LIST_EMPTY);
        }
        // 2. 逐行校验 / 建号 / 插入或更新
        // 【导入策略：逐条独立处理，合格的建号 + 落库，不合格的跳过并继续】
        //   本方法不能放在一个大事务里：否则某行失败会标记整体回滚，后续行的 catch-continue 会抛 UnexpectedRollbackException；
        //   建号规则同新增学生（用户名 = 密码 = 学号，学号需 4-30 位数字/字母、密码 4-16 位）。
        StudentImportRespVO respVO = StudentImportRespVO.builder()
                .createStudentNos(new ArrayList<>())
                .updateStudentNos(new ArrayList<>())
                .failureStudentNos(new LinkedHashMap<>())
                .build();
        for (int rowIndex = 0; rowIndex < importStudents.size(); rowIndex++) {
            StudentImportExcelVO importStudent = importStudents.get(rowIndex);
            // Excel 第 1 行为表头，故数据行号 = 下标 + 2
            int excelRow = rowIndex + 2;
            String studentNo = importStudent.getStudentNo();
            // 2.1 校验学号非空
            if (studentNo == null || studentNo.trim().isEmpty()) {
                respVO.getFailureStudentNos().put("", "第 " + excelRow + " 行：学号不能为空");
                continue;
            }
            // 2.2 名称 → 编号 反查 + 基础校验（学制合法、出生日期不得晚于今天）
            //     反查不到时给出「具体行 + 具体名称」，便于用户定位修正 Excel。
            Long gradeId;
            Long classId;
            Long majorId;
            Long deptId;
            try {
                gradeId = resolveGradeId(importStudent.getGradeName());
                classId = resolveClassId(importStudent.getClassName());
                majorId = resolveMajorId(importStudent.getMajorName());
                deptId = resolveDeptId(importStudent.getDeptName());
                validateEducationSystem(importStudent.getEducationSystem());
                validateBirthDate(importStudent.getBirthDate());
            } catch (ServiceException ex) {
                respVO.getFailureStudentNos().put(studentNo, "第 " + excelRow + " 行：" + ex.getMessage());
                continue;
            }
            // 2.3 判断学生是否已存在
            StudentDO existStudent = studentMapper.selectByStudentNo(studentNo);
            if (existStudent == null) {
                // 2.3.1 先建号（不合格则不落库，直接跳过该行）
                StudentDO student = BeanUtils.toBean(importStudent, StudentDO.class);
                fillResolvedIds(student, gradeId, classId, majorId, deptId);
                Long userId;
                try {
                    userId = createUserForStudent(student);
                } catch (Exception e) {
                    respVO.getFailureStudentNos().put(studentNo, "第 " + excelRow + " 行："
                            + STUDENT_USER_CREATE_FAILED.getMsg() + "（学号需 4-30 位数字/字母，密码 4-16 位）");
                    continue;
                }
                // 2.3.2 建号成功后再落库；回填「专业名称」冗余字段
                student.setUserId(userId);
                fillMajorName(student, majorId);
                try {
                    studentMapper.insert(student);
                } catch (DuplicateKeyException e) {
                    respVO.getFailureStudentNos().put(studentNo, "第 " + excelRow + " 行：" + STUDENT_NO_DUPLICATE.getMsg());
                    continue;
                }
                respVO.getCreateStudentNos().add(studentNo);
                continue;
            }
            // 2.4 已存在，判断是否允许更新
            if (!isUpdateSupport) {
                respVO.getFailureStudentNos().put(studentNo, "第 " + excelRow + " 行：" + STUDENT_NO_DUPLICATE.getMsg());
                continue;
            }
            // 2.5 更新学生（已存在但没有账号的，补建号；补建失败则该行按不合格跳过）
            StudentDO updateStudent = BeanUtils.toBean(importStudent, StudentDO.class);
            updateStudent.setId(existStudent.getId());
            updateStudent.setUserId(existStudent.getUserId());
            fillResolvedIds(updateStudent, gradeId, classId, majorId, deptId);
            if (existStudent.getUserId() == null) {
                try {
                    updateStudent.setUserId(createUserForStudent(updateStudent));
                } catch (Exception e) {
                    respVO.getFailureStudentNos().put(studentNo, "第 " + excelRow + " 行："
                            + STUDENT_USER_CREATE_FAILED.getMsg() + "（学号需 4-30 位数字/字母，密码 4-16 位）");
                    continue;
                }
            }
            // 回填「专业名称」冗余字段
            fillMajorName(updateStudent, majorId);
            try {
                studentMapper.updateById(updateStudent);
            } catch (DuplicateKeyException e) {
                respVO.getFailureStudentNos().put(studentNo, "第 " + excelRow + " 行：" + STUDENT_NO_DUPLICATE.getMsg());
                continue;
            }
            respVO.getUpdateStudentNos().add(studentNo);
        }
        // 3. 记录变更日志（档案变更历史：一次导入写一行摘要）
        studentChangeLogRecorder.recordImportSummary(respVO.getCreateStudentNos().size(),
                respVO.getUpdateStudentNos().size(), respVO.getFailureStudentNos().size());
        return respVO;
    }

    /**
     * 把导入反查得到的编号回填到学生 DO（年级 / 班级 / 专业 / 院系）
     */
    private void fillResolvedIds(StudentDO student, Long gradeId, Long classId, Long majorId, Long deptId) {
        student.setGradeId(gradeId);
        student.setClassId(classId);
        student.setMajorId(majorId);
        student.setDeptId(deptId);
    }

    /**
     * 年级名称 → 年级编号（为空时返回 null；填了但查不到则抛出带名称的业务异常）
     */
    private Long resolveGradeId(String gradeName) {
        if (isBlank(gradeName)) {
            return null;
        }
        GradeDO grade = gradeMapper.selectByName(gradeName.trim());
        if (grade == null) {
            throw exception(STUDENT_IMPORT_GRADE_NAME_NOT_EXISTS, gradeName);
        }
        return grade.getId();
    }

    /**
     * 班级名称 → 班级编号（为空时返回 null；填了但查不到则抛出带名称的业务异常）
     */
    private Long resolveClassId(String className) {
        if (isBlank(className)) {
            return null;
        }
        ClassDO clazz = classMapper.selectByName(className.trim());
        if (clazz == null) {
            throw exception(STUDENT_IMPORT_CLASS_NAME_NOT_EXISTS, className);
        }
        return clazz.getId();
    }

    /**
     * 专业名称 → 专业编号（为空时返回 null；填了但查不到则抛出带名称的业务异常）
     */
    private Long resolveMajorId(String majorName) {
        if (isBlank(majorName)) {
            return null;
        }
        DictMajorDO major = dictMajorMapper.selectByName(majorName.trim());
        if (major == null) {
            throw exception(STUDENT_IMPORT_MAJOR_NAME_NOT_EXISTS, majorName);
        }
        return major.getId();
    }

    /**
     * 院系名称 → 院系编号（为空时返回 null；填了但查不到则抛出带名称的业务异常）
     *
     * 院系数据源为 system 模块的部门表，通过 DeptApi 反查。
     */
    private Long resolveDeptId(String deptName) {
        if (isBlank(deptName)) {
            return null;
        }
        List<DeptRespDTO> depts = deptApi.getDeptListByName(Collections.singletonList(deptName.trim()));
        if (CollUtil.isEmpty(depts)) {
            throw exception(STUDENT_IMPORT_DEPT_NAME_NOT_EXISTS, deptName);
        }
        return depts.get(0).getId();
    }

    /**
     * 判断字符串是否为空（含纯空白）
     */
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * 获得当前登录人对应的学生档案（非学生登录时返回 null），用于「仅本人数据」过滤
     */
    private StudentDO getLoginStudentOrNull() {
        return getStudentByUserId(SecurityFrameworkUtils.getLoginUserId());
    }

    @Override
    public StudentDO getStudentByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        return studentMapper.selectByUserId(userId);
    }

    private void validateStudentNoUnique(Long id, String studentNo) {
        if (studentNo == null) {
            return;
        }
        StudentDO student = studentMapper.selectByStudentNo(studentNo);
        if (student == null) {
            return;
        }
        if (id == null || !student.getId().equals(id)) {
            throw exception(STUDENT_NO_DUPLICATE);
        }
    }

    /**
     * 校验年级、班级存在（仅当对应编号非空时校验）
     */
    private void validateGradeAndClass(Long gradeId, Long classId) {
        if (gradeId != null && gradeMapper.selectById(gradeId) == null) {
            throw exception(STUDENT_GRADE_NOT_EXISTS);
        }
        if (classId != null && classMapper.selectById(classId) == null) {
            throw exception(STUDENT_CLASS_NOT_EXISTS);
        }
    }

    /**
     * 校验专业存在，并回填「专业名称」冗余字段
     *
     * 数据源为占位表 dict_major。
     * TODO: 字典中心模块待接入
     */
    private void validateMajorAndFill(StudentDO student, Long majorId) {
        if (majorId == null) {
            student.setMajor(null);
            return;
        }
        DictMajorDO major = dictMajorMapper.selectById(majorId);
        if (major == null) {
            throw exception(STUDENT_MAJOR_NOT_EXISTS);
        }
        student.setMajor(major.getName());
    }

    /**
     * 回填「专业名称」冗余字段（已存在校验通过时使用，专业编号为空则不回填）
     */
    private void fillMajorName(StudentDO student, Long majorId) {
        if (majorId == null) {
            student.setMajor(null);
            return;
        }
        DictMajorDO major = dictMajorMapper.selectById(majorId);
        student.setMajor(major != null ? major.getName() : null);
    }

    /**
     * 校验学制合法性：仅保留 1二年制 / 3四年制 / 4五年制
     */
    private void validateEducationSystem(Integer educationSystem) {
        if (educationSystem != null && !Objects.equals(educationSystem, 1)
                && !Objects.equals(educationSystem, 3)
                && !Objects.equals(educationSystem, 4)) {
            throw exception(STUDENT_EDUCATION_SYSTEM_INVALID);
        }
    }

    /**
     * 校验出生日期不得晚于今天（为空时不校验）
     */
    private void validateBirthDate(LocalDate birthDate) {
        if (birthDate != null && birthDate.isAfter(LocalDate.now())) {
            throw exception(STUDENT_BIRTH_DATE_FUTURE);
        }
    }

    /**
     * 自动建号（新增学生流程用）：建号成功后回写 user_id
     *
     * 若学号不满足系统账号规则（4-30 位数字/字母）或密码规则（4-16 位），建号会失败并整体回滚。
     */
    private void createStudentUser(StudentDO student) {
        try {
            student.setUserId(createUserForStudent(student));
        } catch (Exception e) {
            throw exception(STUDENT_USER_CREATE_FAILED);
        }
        studentMapper.updateById(student);
    }

    /**
     * 创建学生的系统登录账号：用户名 = 密码 = 学号，并自动分配基础角色 edu_student，返回账号编号
     *
     * 跨模块调用 system 的 AdminUserApi / PermissionApi；供「新增学生」与「Excel 导入」复用；
     * 失败时抛出异常，由调用方决定是整体回滚（新增）还是跳过该行（导入）。
     */
    private Long createUserForStudent(StudentDO student) {
        AdminUserCreateReqDTO userReqDTO = new AdminUserCreateReqDTO();
        userReqDTO.setUsername(student.getStudentNo());   // 用户名 = 学号
        userReqDTO.setPassword(student.getStudentNo());   // 密码 = 学号
        userReqDTO.setNickname(student.getName());
        userReqDTO.setDeptId(student.getDeptId());
        userReqDTO.setMobile(student.getMobile());
        userReqDTO.setEmail(student.getEmail());
        userReqDTO.setSex(student.getGender());
        userReqDTO.setRemark("学生自动建号");
        Long userId = adminUserApi.createUser(userReqDTO);
        // 自动分配基础角色：edu_student
        Long studentRoleId = roleApi.getRoleIdByCode(STUDENT_BASE_ROLE_CODE);
        if (studentRoleId != null) {
            permissionApi.assignUserRole(userId, Collections.singletonList(studentRoleId));
        }
        return userId;
    }

    /**
     * 保存学生标签关联：先物理删除旧关联，再按 tagIds 批量插入
     *
     * 保存前过滤掉「标签表中已不存在」的编号，避免把已删除标签的孤儿关联重新写回。
     *
     * @param studentId 学生编号
     * @param tagIds    标签编号列表
     */
    private void saveStudentTags(Long studentId, List<Long> tagIds) {
        studentTagRelMapper.deleteByStudentId(studentId);
        if (CollUtil.isEmpty(tagIds)) {
            return;
        }
        // 1. 去重并保序，同时过滤掉「标签表中已不存在」的编号（即已删除的标签）
        List<Long> finalTagIds = new ArrayList<>(new LinkedHashSet<>(tagIds));
        Set<Long> validTagIds = CollectionUtils.convertSet(
                studentTagService.getStudentTagListByIds(finalTagIds), StudentTagDO::getId);
        finalTagIds.retainAll(validTagIds);
        if (CollUtil.isEmpty(finalTagIds)) {
            return;
        }
        // 2. 仅写入有效标签的关联
        List<StudentTagRelDO> relList = CollectionUtils.convertList(finalTagIds,
                tagId -> StudentTagRelDO.builder().studentId(studentId).tagId(tagId).build());
        studentTagRelMapper.insertBatch(relList);
    }

}
