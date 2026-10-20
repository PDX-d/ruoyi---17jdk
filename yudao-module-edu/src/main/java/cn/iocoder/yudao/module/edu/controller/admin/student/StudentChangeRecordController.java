package cn.iocoder.yudao.module.edu.controller.admin.student;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.StudentChangeApproveReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.StudentChangeRecordPageReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.StudentChangeRecordRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.StudentChangeRecordSaveReqVO;
import cn.iocoder.yudao.module.edu.dal.dataobject.student.StudentChangeRecordDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.student.StudentDO;
import cn.iocoder.yudao.module.edu.service.student.StudentApprovalService;
import cn.iocoder.yudao.module.edu.service.student.StudentChangeRecordService;
import cn.iocoder.yudao.module.edu.service.student.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - 学生变更记录管理
 *
 * 变更记录的提交/审批由 {@link StudentApprovalService} 处理。
 */
@Tag(name = "管理后台 - 学生变更记录管理")
@RestController
@RequestMapping("/edu/student/change")
@Validated
public class StudentChangeRecordController {

    @Resource
    private StudentApprovalService studentApprovalService;

    @Resource
    private StudentChangeRecordService studentChangeRecordService;

    @Resource
    private StudentService studentService;

    @PostMapping("/create")
    @Operation(summary = "提交学生变更申请")
    @PreAuthorize("@ss.hasPermission('edu:student:update')")
    public CommonResult<Long> createChangeRecord(@Valid @RequestBody StudentChangeRecordSaveReqVO createReqVO) {
        return success(studentApprovalService.submitChangeRecord(createReqVO));
    }

    @GetMapping("/page")
    @Operation(summary = "获得学生变更记录分页")
    @PreAuthorize("@ss.hasPermission('edu:student:query')")
    public CommonResult<PageResult<StudentChangeRecordRespVO>> getChangeRecordPage(@Valid StudentChangeRecordPageReqVO pageReqVO) {
        PageResult<StudentChangeRecordDO> pageResult = studentChangeRecordService.getChangeRecordPage(pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(BeanUtils.toBean(pageResult, StudentChangeRecordRespVO.class));
        }
        // 批量填充学生姓名
        Map<Long, String> studentNameMap = CollectionUtils.convertMap(
                studentService.getStudentListByIds(CollectionUtils.convertSet(pageResult.getList(), StudentChangeRecordDO::getStudentId)),
                StudentDO::getId, StudentDO::getName);
        return success(BeanUtils.toBean(pageResult, StudentChangeRecordRespVO.class,
                respVO -> respVO.setStudentName(studentNameMap.get(respVO.getStudentId()))));
    }

    @PutMapping("/approve")
    @Operation(summary = "审批学生变更申请")
    @PreAuthorize("@ss.hasPermission('edu:student:update')")
    public CommonResult<Boolean> approveChangeRecord(@Valid @RequestBody StudentChangeApproveReqVO approveReqVO) {
        studentApprovalService.approveChangeRecord(approveReqVO);
        return success(true);
    }

}
