package cn.iocoder.yudao.module.edu.service.student;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.biz.system.dict.dto.DictDataRespDTO;
import cn.iocoder.yudao.module.edu.dal.dataobject.changelog.ChangeLogDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.dict.DictMajorDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.grade.ClassDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.grade.GradeDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.student.StudentDO;
import cn.iocoder.yudao.module.edu.dal.mysql.dict.DictMajorMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.grade.ClassMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.grade.GradeMapper;
import cn.iocoder.yudao.module.edu.enums.changelog.ChangeLogActionEnum;
import cn.iocoder.yudao.module.edu.service.changelog.ChangeLogService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.dict.DictDataApi;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 学生变更日志记录器（档案变更历史 · 学生域）
 *
 * 把学生档案的变更翻译成 edu_change_log 记录行，供 StudentService / StudentApprovalService 埋点复用。
 *
 * 记录规则：
 * 1. 新增 / 删除 / 导入：写一行摘要；
 * 2. 普通修改：逐字段 diff，每个变更字段写一行；
 * 3. 学籍状态变更 / 复学 / 调班（审批落地时）：每个涉及字段写一行。
 */
@Component
public class StudentChangeLogRecorder {

    /** 业务模块名（edu_change_log.biz_module） */
    public static final String BIZ_MODULE = "学生管理";

    /** 字典类型：性别 */
    private static final String DICT_SEX = "system_user_sex";
    /** 字典类型：政治面貌 */
    private static final String DICT_POLITICAL_STATUS = "edu_political_status";
    /** 字典类型：学制 */
    private static final String DICT_EDUCATION_SYSTEM = "edu_education_system";
    /** 字典类型：学籍状态 */
    private static final String DICT_STUDENT_STATUS = "edu_student_status";

    @Resource
    private ChangeLogService changeLogService;

    @Resource
    private DictDataApi dictDataApi;

    @Resource
    private GradeMapper gradeMapper;

    @Resource
    private ClassMapper classMapper;

    @Resource
    private DictMajorMapper dictMajorMapper;

    @Resource
    private DeptApi deptApi;

    /**
     * 记录「新增学生」
     */
    public void recordCreate(StudentDO student) {
        ChangeLogDO log = newBaseLog(student, ChangeLogActionEnum.CREATE.getAction());
        log.setRemark(String.format("新增学生：%s（学号 %s）", student.getName(), student.getStudentNo()));
        changeLogService.createChangeLog(log);
    }

    /**
     * 记录「删除学生」
     */
    public void recordDelete(StudentDO student) {
        ChangeLogDO log = newBaseLog(student, ChangeLogActionEnum.DELETE.getAction());
        log.setRemark(String.format("删除学生：%s（学号 %s）", student.getName(), student.getStudentNo()));
        changeLogService.createChangeLog(log);
    }

    /**
     * 记录「修改学生」：逐字段 diff，无变化则不记录
     *
     * @param before 变更前的学生（主表原值）
     * @param after  变更后的学生（提交值）
     */
    public void recordUpdate(StudentDO before, StudentDO after) {
        List<ChangeLogDO> logs = new ArrayList<>();
        Map<String, String> dictCache = new HashMap<>();
        addDiff(logs, before, after, "studentNo", "学号",
                before.getStudentNo(), after.getStudentNo(), null, dictCache);
        addDiff(logs, before, after, "name", "姓名",
                before.getName(), after.getName(), null, dictCache);
        addDiff(logs, before, after, "gender", "性别",
                before.getGender(), after.getGender(), dict(DICT_SEX), dictCache);
        addDiff(logs, before, after, "idCard", "身份证号",
                before.getIdCard(), after.getIdCard(), null, dictCache);
        addDiff(logs, before, after, "birthDate", "出生日期",
                before.getBirthDate(), after.getBirthDate(), null, dictCache);
        addDiff(logs, before, after, "nation", "民族",
                before.getNation(), after.getNation(), null, dictCache);
        addDiff(logs, before, after, "politicalStatus", "政治面貌",
                before.getPoliticalStatus(), after.getPoliticalStatus(), dict(DICT_POLITICAL_STATUS), dictCache);
        addDiff(logs, before, after, "nativePlace", "籍贯",
                before.getNativePlace(), after.getNativePlace(), null, dictCache);
        addDiff(logs, before, after, "gradeId", "年级",
                before.getGradeId(), after.getGradeId(), this::gradeName, dictCache);
        addDiff(logs, before, after, "classId", "班级",
                before.getClassId(), after.getClassId(), this::className, dictCache);
        addDiff(logs, before, after, "majorId", "专业",
                before.getMajorId(), after.getMajorId(), this::majorName, dictCache);
        addDiff(logs, before, after, "deptId", "所属院系",
                before.getDeptId(), after.getDeptId(), this::deptName, dictCache);
        addDiff(logs, before, after, "educationSystem", "学制",
                before.getEducationSystem(), after.getEducationSystem(), dict(DICT_EDUCATION_SYSTEM), dictCache);
        addDiff(logs, before, after, "enrollmentYear", "入学年份",
                before.getEnrollmentYear(), after.getEnrollmentYear(), null, dictCache);
        addDiff(logs, before, after, "admissionDate", "入学日期",
                before.getAdmissionDate(), after.getAdmissionDate(), null, dictCache);
        addDiff(logs, before, after, "graduateDate", "毕业日期",
                before.getGraduateDate(), after.getGraduateDate(), null, dictCache);
        addDiff(logs, before, after, "dormitory", "宿舍",
                before.getDormitory(), after.getDormitory(), null, dictCache);
        addDiff(logs, before, after, "mobile", "手机号",
                before.getMobile(), after.getMobile(), null, dictCache);
        addDiff(logs, before, after, "email", "邮箱",
                before.getEmail(), after.getEmail(), null, dictCache);
        addDiff(logs, before, after, "status", "学籍状态",
                before.getStatus(), after.getStatus(), dict(DICT_STUDENT_STATUS), dictCache);
        addDiff(logs, before, after, "photoUrl", "证件照",
                before.getPhotoUrl(), after.getPhotoUrl(), null, dictCache);
        addDiff(logs, before, after, "idCardFrontUrl", "身份证正面",
                before.getIdCardFrontUrl(), after.getIdCardFrontUrl(), null, dictCache);
        addDiff(logs, before, after, "idCardBackUrl", "身份证反面",
                before.getIdCardBackUrl(), after.getIdCardBackUrl(), null, dictCache);
        addDiff(logs, before, after, "guardianName", "监护人姓名",
                before.getGuardianName(), after.getGuardianName(), null, dictCache);
        addDiff(logs, before, after, "guardianMobile", "监护人手机号",
                before.getGuardianMobile(), after.getGuardianMobile(), null, dictCache);
        addDiff(logs, before, after, "address", "家庭住址",
                before.getAddress(), after.getAddress(), null, dictCache);
        addDiff(logs, before, after, "emergencyContact", "紧急联系人",
                before.getEmergencyContact(), after.getEmergencyContact(), null, dictCache);
        addDiff(logs, before, after, "emergencyMobile", "紧急联系人电话",
                before.getEmergencyMobile(), after.getEmergencyMobile(), null, dictCache);
        addDiff(logs, before, after, "remark", "备注",
                before.getRemark(), after.getRemark(), null, dictCache);
        changeLogService.createChangeLogBatch(logs);
    }

    /**
     * 记录「审批落地」引起的变更（学籍状态变更 / 复学 / 调班）
     *
     * @param before 变更前的学生
     * @param after  变更后的学生
     * @param action 操作类型（见 {@link ChangeLogActionEnum}）
     */
    public void recordApprovedChange(StudentDO before, StudentDO after, Integer action) {
        List<ChangeLogDO> logs = new ArrayList<>();
        Map<String, String> dictCache = new HashMap<>();
        if (Objects.equals(action, ChangeLogActionEnum.CLASS_CHANGE.getAction())) {
            addDiff(logs, before, after, "classId", "班级",
                    before.getClassId(), after.getClassId(), this::className, dictCache);
        }
        if (Objects.equals(action, ChangeLogActionEnum.STATUS_CHANGE.getAction())
                || Objects.equals(action, ChangeLogActionEnum.RESUME.getAction())) {
            addDiff(logs, before, after, "status", "学籍状态",
                    before.getStatus(), after.getStatus(), dict(DICT_STUDENT_STATUS), dictCache);
        }
        // 审批落地未产生实际变化时，也留一条摘要（避免「审批通过但无痕迹」）
        if (CollUtil.isEmpty(logs)) {
            ChangeLogDO log = newBaseLog(after, action);
            log.setRemark("审批通过，未产生字段变化");
            logs.add(log);
        }
        changeLogService.createChangeLogBatch(logs);
    }

    /**
     * 记录「Excel 导入」批次摘要（一次导入写一行，避免大批量导入撑爆日志表）
     *
     * @param createCount 新增条数
     * @param updateCount 更新条数
     * @param failureCount 失败条数
     */
    public void recordImportSummary(int createCount, int updateCount, int failureCount) {
        ChangeLogDO log = ChangeLogDO.builder()
                .bizModule(BIZ_MODULE)
                .action(ChangeLogActionEnum.IMPORT.getAction())
                .remark(String.format("导入学生：新增 %d 条、更新 %d 条、失败 %d 条",
                        createCount, updateCount, failureCount))
                .build();
        changeLogService.createChangeLog(log);
    }

    // ==================== 内部实现 ====================

    /**
     * 字段值解析器（把原始值转成「文字」；cache 为字典标签缓存，非字典解析器可忽略）
     */
    @FunctionalInterface
    private interface ValueResolver {
        String resolve(Object raw, Map<String, String> dictCache);
    }

    /**
     * 追加一条字段级变更记录（值相同则跳过）
     *
     * afterRaw 为空也跳过：与 MyBatis-Plus updateById 忽略 null 字段一致，避免日志与库不一致。
     */
    private void addDiff(List<ChangeLogDO> logs, StudentDO before, StudentDO after,
                         String fieldName, String fieldLabel,
                         Object beforeRaw, Object afterRaw,
                         ValueResolver resolver, Map<String, String> dictCache) {
        if (Objects.equals(beforeRaw, afterRaw) || afterRaw == null) {
            return;
        }
        ChangeLogDO log = newBaseLog(after, ChangeLogActionEnum.UPDATE.getAction());
        log.setFieldName(fieldName);
        log.setFieldLabel(fieldLabel);
        log.setBeforeValue(resolveText(beforeRaw, resolver, dictCache));
        log.setAfterValue(resolveText(afterRaw, resolver, dictCache));
        logs.add(log);
    }

    /**
     * 构造变更日志基础信息（业务模块 / 主键 / 编号 / 名称）
     */
    private ChangeLogDO newBaseLog(StudentDO student, Integer action) {
        return ChangeLogDO.builder()
                .bizModule(BIZ_MODULE)
                .bizId(student.getId())
                .bizNo(student.getStudentNo())
                .bizName(student.getName())
                .action(action)
                .build();
    }

    private String resolveText(Object raw, ValueResolver resolver, Map<String, String> dictCache) {
        if (raw == null) {
            return null;
        }
        if (resolver != null) {
            return resolver.resolve(raw, dictCache);
        }
        return String.valueOf(raw);
    }

    /**
     * 字典解析器（编号 → 字典文字；查不到时退回原始值）
     *
     * dictCache 为字典标签缓存（key = dictType#value），避免重复查询。
     */
    private ValueResolver dict(String dictType) {
        return (raw, cache) -> {
            if (raw == null) {
                return null;
            }
            String value = String.valueOf(raw);
            String label = cache != null ? cache.get(dictType + "#" + value) : null;
            if (label == null) {
                label = lookupDictLabel(dictType, value);
                if (cache != null && label != null) {
                    cache.put(dictType + "#" + value, label);
                }
            }
            return label != null ? label : value;
        };
    }

    private String lookupDictLabel(String dictType, String value) {
        List<DictDataRespDTO> dictDatas = dictDataApi.getDictDataList(dictType);
        if (CollUtil.isEmpty(dictDatas)) {
            return null;
        }
        return dictDatas.stream()
                .filter(item -> Objects.equals(String.valueOf(item.getValue()), value))
                .map(DictDataRespDTO::getLabel)
                .findFirst().orElse(null);
    }

    private String gradeName(Object raw, Map<String, String> dictCache) {
        Long id = toLong(raw);
        GradeDO grade = id != null ? gradeMapper.selectById(id) : null;
        return grade != null ? grade.getName() : String.valueOf(raw);
    }

    private String className(Object raw, Map<String, String> dictCache) {
        Long id = toLong(raw);
        ClassDO clazz = id != null ? classMapper.selectById(id) : null;
        return clazz != null ? clazz.getName() : String.valueOf(raw);
    }

    private String majorName(Object raw, Map<String, String> dictCache) {
        Long id = toLong(raw);
        DictMajorDO major = id != null ? dictMajorMapper.selectById(id) : null;
        return major != null ? major.getName() : String.valueOf(raw);
    }

    private String deptName(Object raw, Map<String, String> dictCache) {
        Long id = toLong(raw);
        if (id == null) {
            return String.valueOf(raw);
        }
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(Collections.singleton(id));
        DeptRespDTO dept = deptMap.get(id);
        return dept != null ? dept.getName() : String.valueOf(raw);
    }

    private Long toLong(Object raw) {
        if (raw == null) {
            return null;
        }
        if (raw instanceof Number) {
            return ((Number) raw).longValue();
        }
        try {
            return Long.valueOf(String.valueOf(raw));
        } catch (NumberFormatException e) {
            return null;
        }
    }

}
