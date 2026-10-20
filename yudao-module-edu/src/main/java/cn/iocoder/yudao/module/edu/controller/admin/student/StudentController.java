package cn.iocoder.yudao.module.edu.controller.admin.student;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.StudentClassRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.StudentExportExcelVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.StudentGradeRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.StudentImportExcelVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.StudentImportRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.StudentMajorRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.StudentPageReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.StudentRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.StudentSaveReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.StudentTagRespVO;
import cn.iocoder.yudao.module.edu.dal.dataobject.grade.ClassDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.grade.GradeDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.student.StudentDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.student.StudentTagDO;
import cn.iocoder.yudao.module.edu.service.student.StudentService;
import cn.iocoder.yudao.module.edu.service.student.StudentTagService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - 学生管理
 */
@Tag(name = "管理后台 - 学生管理")
@RestController
@RequestMapping("/edu/student")
@Validated
public class StudentController {

    @Resource
    private StudentService studentService;

    @Resource
    private StudentTagService studentTagService;

    @Resource
    private DeptApi deptApi;

    @PostMapping("/create")
    @Operation(summary = "创建学生")
    @PreAuthorize("@ss.hasPermission('edu:student:create')")
    public CommonResult<Long> createStudent(@Valid @RequestBody StudentSaveReqVO createReqVO) {
        return success(studentService.createStudent(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新学生")
    @PreAuthorize("@ss.hasPermission('edu:student:update')")
    public CommonResult<Boolean> updateStudent(@Valid @RequestBody StudentSaveReqVO updateReqVO) {
        studentService.updateStudent(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除学生")
    @Parameter(name = "id", description = "学生编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('edu:student:delete')")
    public CommonResult<Boolean> deleteStudent(@RequestParam("id") Long id) {
        studentService.deleteStudent(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得学生")
    @Parameter(name = "id", description = "学生编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('edu:student:query')")
    public CommonResult<StudentRespVO> getStudent(@RequestParam("id") Long id) {
        StudentDO student = studentService.getStudent(id);
        if (student == null) {
            return success(null);
        }
        return success(buildRespVOList(Collections.singletonList(student)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得学生分页")
    @PreAuthorize("@ss.hasPermission('edu:student:query')")
    public CommonResult<PageResult<StudentRespVO>> getStudentPage(@Valid StudentPageReqVO pageReqVO) {
        PageResult<StudentDO> pageResult = studentService.getStudentPage(pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(BeanUtils.toBean(pageResult, StudentRespVO.class));
        }
        return success(new PageResult<>(buildRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出学生")
    @PreAuthorize("@ss.hasPermission('edu:student:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportStudentList(@Validated StudentPageReqVO exportReqVO,
                                  HttpServletResponse response) throws IOException {
        exportReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<StudentDO> list = studentService.getStudentPage(exportReqVO).getList();
        // 输出 Excel：使用 StudentExportExcelVO（中文表头 + 字典文字 + 名称列），与导入模板列一一对应
        List<StudentExportExcelVO> exportList = CollectionUtils.convertList(buildRespVOList(list), this::convertToExportVO);
        ExcelUtils.write(response, "学生数据.xlsx", "数据", StudentExportExcelVO.class, exportList);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入学生模板")
    public void importTemplate(HttpServletResponse response) throws IOException {
        // 手动创建导出 demo（列与 StudentImportExcelVO 字段一一对应）：
        // 性别 / 政治面貌 / 学制 / 状态填「字典值」，专业 / 院系 / 年级 / 班级填「名称」（导入时按名称反查编号）。
        List<StudentImportExcelVO> list = Arrays.asList(
                StudentImportExcelVO.builder().studentNo("20230101").name("张三").gender(1).idCard("110101200001011234")
                        .birthDate(LocalDate.of(2004, 1, 1)).nation("汉族").politicalStatus(3).nativePlace("北京市")
                        .majorName("软件工程").deptName("计算机学院").educationSystem(3).gradeName("2023级").className("软件工程2301班")
                        .enrollmentYear(2023).dormitory("1号楼101").mobile("15601691300").email("student@iocoder.cn").status(0)
                        .guardianName("张父").guardianMobile("15601691301").remark("备注").build(),
                StudentImportExcelVO.builder().studentNo("20230102").name("李四").gender(2).idCard("110101200001011235")
                        .birthDate(LocalDate.of(2004, 2, 2)).nation("汉族").politicalStatus(3).nativePlace("北京市")
                        .majorName("计算机科学与技术").deptName("计算机学院").educationSystem(3).gradeName("2023级").className("计算机2301班")
                        .enrollmentYear(2023).dormitory("1号楼102").mobile("15601701300").email("student2@iocoder.cn").status(0)
                        .guardianName("李母").guardianMobile("15601701301").remark("备注").build()
        );
        // 输出
        ExcelUtils.write(response, "学生导入模板.xlsx", "学生列表", StudentImportExcelVO.class, list);
    }

    @PostMapping("/import")
    @Operation(summary = "导入学生")
    @Parameters({
            @Parameter(name = "file", description = "Excel 文件", required = true),
            @Parameter(name = "updateSupport", description = "是否支持更新，默认为 false", example = "true")
    })
    @PreAuthorize("@ss.hasPermission('edu:student:import')")
    public CommonResult<StudentImportRespVO> importExcel(@RequestParam("file") MultipartFile file,
                                                         @RequestParam(value = "updateSupport", required = false, defaultValue = "false") Boolean updateSupport) throws Exception {
        List<StudentImportExcelVO> list = ExcelUtils.read(file, StudentImportExcelVO.class);
        return success(studentService.importStudentList(list, updateSupport));
    }

    @GetMapping("/grade-list")
    @Operation(summary = "获得年级下拉列表")
    @PreAuthorize("@ss.hasPermission('edu:student:query')")
    public CommonResult<List<StudentGradeRespVO>> getGradeList() {
        return success(CollectionUtils.convertList(studentService.getGradeList(),
                grade -> BeanUtils.toBean(grade, StudentGradeRespVO.class)));
    }

    @GetMapping("/class-list")
    @Operation(summary = "获得班级下拉列表")
    @Parameter(name = "gradeId", description = "年级编号", example = "1")
    @PreAuthorize("@ss.hasPermission('edu:student:query')")
    public CommonResult<List<StudentClassRespVO>> getClassList(@RequestParam(value = "gradeId", required = false) Long gradeId) {
        return success(CollectionUtils.convertList(studentService.getClassList(gradeId),
                clazz -> BeanUtils.toBean(clazz, StudentClassRespVO.class)));
    }

    @GetMapping("/major-list")
    @Operation(summary = "获得专业下拉列表（依赖年级联动）")
    @Parameter(name = "gradeId", description = "年级编号", example = "1")
    @PreAuthorize("@ss.hasPermission('edu:student:query')")
    public CommonResult<List<StudentMajorRespVO>> getMajorList(@RequestParam(value = "gradeId", required = false) Long gradeId) {
        // 专业下拉数据源当前为占位表 dict_major
        // TODO: 字典中心模块待接入
        return success(CollectionUtils.convertList(studentService.getMajorList(gradeId),
                major -> BeanUtils.toBean(major, StudentMajorRespVO.class)));
    }

    /**
     * 学生 Response VO → Excel 导出 VO 转换（编号由字典转换器渲染为文字，名称列已回填）
     */
    private StudentExportExcelVO convertToExportVO(StudentRespVO resp) {
        return StudentExportExcelVO.builder()
                .studentNo(resp.getStudentNo())
                .name(resp.getName())
                .gender(resp.getGender())
                .idCard(resp.getIdCard())
                .birthDate(resp.getBirthDate())
                .nation(resp.getNation())
                .politicalStatus(resp.getPoliticalStatus())
                .nativePlace(resp.getNativePlace())
                .majorName(resp.getMajorName())
                .deptName(resp.getDeptName())
                .educationSystem(resp.getEducationSystem())
                .gradeName(resp.getGradeName())
                .className(resp.getClassName())
                .enrollmentYear(resp.getEnrollmentYear())
                .dormitory(resp.getDormitory())
                .mobile(resp.getMobile())
                .email(resp.getEmail())
                .status(resp.getStatus())
                .guardianName(resp.getGuardianName())
                .guardianMobile(resp.getGuardianMobile())
                .remark(resp.getRemark())
                .build();
    }

    /**
     * 批量拼接学生 Response VO：填充院系名称、年级名称、班级名称、标签编号与标签列表，避免 N+1 查询
     */
    private List<StudentRespVO> buildRespVOList(List<StudentDO> list) {
        List<StudentRespVO> respList = BeanUtils.toBean(list, StudentRespVO.class);
        if (CollUtil.isEmpty(respList)) {
            return respList;
        }
        // 1. 批量查询院系、年级、班级、标签关联
        Set<Long> deptIds = CollectionUtils.convertSet(list, StudentDO::getDeptId);
        Set<Long> gradeIds = CollectionUtils.convertSet(list, StudentDO::getGradeId);
        Set<Long> classIds = CollectionUtils.convertSet(list, StudentDO::getClassId);
        Set<Long> studentIds = CollectionUtils.convertSet(list, StudentDO::getId);
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(deptIds);
        Map<Long, GradeDO> gradeMap = CollectionUtils.convertMap(studentService.getGradeListByIds(gradeIds), GradeDO::getId);
        Map<Long, ClassDO> classMap = CollectionUtils.convertMap(studentService.getClassListByIds(classIds), ClassDO::getId);
        Map<Long, List<Long>> tagIdsMap = studentService.getStudentTagIdsMap(studentIds);
        // 1.1 专业名称冗余存储在 edu_student.major
        // TODO: 字典中心模块待接入（届时可改为按 majorId 批量查字典表）
        Map<Long, String> majorNameMap = new HashMap<>();
        list.forEach(student -> {
            if (student.getMajor() != null) {
                majorNameMap.put(student.getId(), student.getMajor());
            }
        });
        // 2. 批量查询标签详情
        Set<Long> allTagIds = new HashSet<>();
        tagIdsMap.values().forEach(allTagIds::addAll);
        Map<Long, StudentTagDO> tagMap = CollectionUtils.convertMap(
                studentTagService.getStudentTagListByIds(allTagIds), StudentTagDO::getId);
        // 3. 拼接返回
        respList.forEach(respVO -> {
            DeptRespDTO dept = deptMap.get(respVO.getDeptId());
            if (dept != null) {
                respVO.setDeptName(dept.getName());
            }
            GradeDO grade = gradeMap.get(respVO.getGradeId());
            if (grade != null) {
                respVO.setGradeName(grade.getName());
            }
            ClassDO clazz = classMap.get(respVO.getClassId());
            if (clazz != null) {
                respVO.setClassName(clazz.getName());
            }
            respVO.setMajorName(majorNameMap.get(respVO.getId()));
            // 只保留「标签表中仍然存在」的标签，避免已删除标签的孤儿关联被回显并写回
            List<Long> tagIds = new ArrayList<>();
            List<StudentTagRespVO> tags = new ArrayList<>();
            for (Long tagId : tagIdsMap.getOrDefault(respVO.getId(), Collections.emptyList())) {
                StudentTagDO tag = tagMap.get(tagId);
                if (tag != null) {
                    tagIds.add(tagId);
                    tags.add(BeanUtils.toBean(tag, StudentTagRespVO.class));
                }
            }
            respVO.setTagIds(tagIds);
            respVO.setTags(tags);
        });
        return respList;
    }

}
