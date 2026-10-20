package cn.iocoder.yudao.module.edu.controller.admin.student;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.StudentTagReorderReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.StudentTagRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.StudentTagSaveReqVO;
import cn.iocoder.yudao.module.edu.dal.dataobject.student.StudentTagDO;
import cn.iocoder.yudao.module.edu.service.student.StudentTagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - 学生标签管理
 */
@Tag(name = "管理后台 - 学生标签管理")
@RestController
@RequestMapping("/edu/student/tag")
@Validated
public class StudentTagController {

    @Resource
    private StudentTagService studentTagService;

    @GetMapping("/list")
    @Operation(summary = "获得学生标签列表")
    @PreAuthorize("@ss.hasPermission('edu:student:tag:query')")
    public CommonResult<List<StudentTagRespVO>> getStudentTagList() {
        List<StudentTagDO> list = studentTagService.getStudentTagList();
        return success(CollectionUtils.convertList(list, tag -> BeanUtils.toBean(tag, StudentTagRespVO.class)));
    }

    @GetMapping("/get")
    @Operation(summary = "获得学生标签")
    @Parameter(name = "id", description = "标签编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('edu:student:tag:query')")
    public CommonResult<StudentTagRespVO> getStudentTag(@RequestParam("id") Long id) {
        StudentTagDO tag = studentTagService.getStudentTag(id);
        return success(BeanUtils.toBean(tag, StudentTagRespVO.class));
    }

    @PostMapping("/create")
    @Operation(summary = "创建学生标签")
    @PreAuthorize("@ss.hasPermission('edu:student:tag:create')")
    public CommonResult<Long> createStudentTag(@Valid @RequestBody StudentTagSaveReqVO createReqVO) {
        return success(studentTagService.createStudentTag(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新学生标签")
    @PreAuthorize("@ss.hasPermission('edu:student:tag:update')")
    public CommonResult<Boolean> updateStudentTag(@Valid @RequestBody StudentTagSaveReqVO updateReqVO) {
        studentTagService.updateStudentTag(updateReqVO);
        return success(true);
    }

    @PutMapping("/reorder")
    @Operation(summary = "批量重排学生标签（拖拽排序）")
    @PreAuthorize("@ss.hasPermission('edu:student:tag:update')")
    public CommonResult<Boolean> reorderStudentTag(@Valid @RequestBody StudentTagReorderReqVO reorderReqVO) {
        studentTagService.reorderStudentTags(reorderReqVO.getTagIds());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除学生标签")
    @Parameter(name = "id", description = "标签编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('edu:student:tag:delete')")
    public CommonResult<Boolean> deleteStudentTag(@RequestParam("id") Long id) {
        studentTagService.deleteStudentTag(id);
        return success(true);
    }

}
