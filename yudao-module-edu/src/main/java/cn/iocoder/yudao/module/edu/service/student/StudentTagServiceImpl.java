package cn.iocoder.yudao.module.edu.service.student;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.StudentTagPageReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.StudentTagSaveReqVO;
import cn.iocoder.yudao.module.edu.dal.dataobject.student.StudentTagDO;
import cn.iocoder.yudao.module.edu.dal.mysql.student.StudentTagMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.student.StudentTagRelMapper;
import cn.iocoder.yudao.module.edu.enums.student.TagColorEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_TAG_COLOR_INVALID;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_TAG_NAME_DUPLICATE;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_TAG_NOT_EXISTS;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_TAG_SORT_INVALID;

/**
 * 学生标签 Service 实现类
 *
 * 1. sort 由后端唯一维护，展示顺序 = (status 升序, sort 升序, id 升序)，与 sort 值一致；
 * 2. 任何写操作结束后都调用 {@link #renumber()} 归一化序号，保证连续递增且唯一；
 * 3. 归一化分两阶段写回（先负数区、再写最终值），避免唯一索引瞬时重复（见 rewriteSort）。
 */
@Service
@Validated
@Slf4j
public class StudentTagServiceImpl implements StudentTagService {

    @Resource
    private StudentTagMapper studentTagMapper;
    @Resource
    private StudentTagRelMapper studentTagRelMapper;

    @Override
    public Long createStudentTag(StudentTagSaveReqVO createReqVO) {
        // 1. 名称归一化（trim）+ 唯一校验
        String name = StrUtil.trim(createReqVO.getName());
        validateNameUnique(null, name);
        // 2. 颜色枚举校验（固定 16 色）
        validateColor(createReqVO.getColor());
        // 3. 组装实体，追加到现有标签末尾：sort = MAX(sort) + 1
        StudentTagDO tag = BeanUtils.toBean(createReqVO, StudentTagDO.class);
        tag.setName(name);
        tag.setStatus(createReqVO.getStatus() != null ? createReqVO.getStatus() : 0);
        tag.setSort(nextSort());
        insertTagSafely(tag);
        // 4. 归一化序号（保证连续递增且唯一）
        renumber();
        return tag.getId();
    }

    @Override
    public void updateStudentTag(StudentTagSaveReqVO updateReqVO) {
        // 1. 校验标签存在
        StudentTagDO oldTag = validateStudentTagExists(updateReqVO.getId());
        // 2. 名称归一化（trim）+ 唯一校验
        String name = StrUtil.trim(updateReqVO.getName());
        validateNameUnique(updateReqVO.getId(), name);
        // 3. 颜色枚举校验（固定 16 色）
        validateColor(updateReqVO.getColor());
        // 4. 组装更新对象；状态发生变化时先挪到末尾（sort = MAX(sort) + 1），后续统一归一化
        StudentTagDO updateDO = BeanUtils.toBean(updateReqVO, StudentTagDO.class);
        updateDO.setName(name);
        boolean statusChanged = updateReqVO.getStatus() != null
                && !Objects.equals(oldTag.getStatus(), updateReqVO.getStatus());
        if (statusChanged) {
            updateDO.setSort(nextSort());
        }
        try {
            if (studentTagMapper.updateById(updateDO) != 1) {
                throw exception(STUDENT_TAG_NOT_EXISTS);
            }
        } catch (DuplicateKeyException e) {
            throw exception(resolveDuplicate(e, STUDENT_TAG_NAME_DUPLICATE));
        }
        // 5. 状态变化后归一化：停用→沉底、启用→回到末尾（不恢复原位置）
        if (statusChanged) {
            renumber();
        }
    }

    @Override
    public void deleteStudentTag(Long id) {
        // 1. 校验标签存在
        StudentTagDO tag = validateStudentTagExists(id);
        // 2. 同步物理清理「学生-标签」关联，避免标签删除后留下脏关联
        //    物理删除原因：关联表唯一键为「租户 + 学生 + 标签」，走逻辑删除会在重复保存时触发唯一键冲突
        int relCount = studentTagRelMapper.deleteByTagId(id);
        // 3. 删除标签
        if (studentTagMapper.deleteById(id) != 1) {
            throw exception(STUDENT_TAG_NOT_EXISTS);
        }
        // 4. 补齐序号（其余标签自动前移，保持连续）
        renumber();
        // 5. 打点：便于排查标签删除后的关联清理情况
        log.info("[deleteStudentTag] 删除标签({}) 同步清理学生关联 {} 条", tag.getName(), relCount);
    }

    @Override
    public void reorderStudentTags(List<Long> orderedTagIds) {
        // 1. 必须是「当前全部标签」的一个完整排列（不重不漏），否则视为非法排序数据
        if (CollUtil.isEmpty(orderedTagIds)) {
            throw exception(STUDENT_TAG_SORT_INVALID);
        }
        Set<Long> allIds = CollectionUtils.convertSet(studentTagMapper.selectListAll(), StudentTagDO::getId);
        Set<Long> reqIds = new LinkedHashSet<>(orderedTagIds);
        if (allIds.size() != orderedTagIds.size() || reqIds.size() != orderedTagIds.size()
                || !allIds.containsAll(reqIds)) {
            throw exception(STUDENT_TAG_SORT_INVALID);
        }
        // 2. 按新顺序两阶段写回序号（先负数区，规避唯一索引瞬时冲突）
        rewriteSort(orderedTagIds);
        // 3. 归一化（按「启用在前、停用在后」重排，保证 sort 与展示顺序一致）
        renumber();
    }

    @Override
    public StudentTagDO getStudentTag(Long id) {
        return studentTagMapper.selectById(id);
    }

    @Override
    public List<StudentTagDO> getStudentTagList() {
        return studentTagMapper.selectListAll();
    }

    @Override
    public PageResult<StudentTagDO> getStudentTagPage(StudentTagPageReqVO pageReqVO) {
        return studentTagMapper.selectPage(pageReqVO);
    }

    @Override
    public List<StudentTagDO> getStudentTagListByIds(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return studentTagMapper.selectListByIds(ids);
    }

    // ==================== 内部方法 ====================

    /**
     * 校验标签存在，并返回该标签
     *
     * @param id 标签编号
     * @return 标签
     */
    private StudentTagDO validateStudentTagExists(Long id) {
        StudentTagDO tag = studentTagMapper.selectById(id);
        if (tag == null) {
            throw exception(STUDENT_TAG_NOT_EXISTS);
        }
        return tag;
    }

    /**
     * 校验标签名称在同租户下唯一（前后端双重校验；数据库侧由唯一索引兜底）
     *
     * @param id   标签编号（新增时传 null）
     * @param name 标签名称（已 trim）
     */
    private void validateNameUnique(Long id, String name) {
        if (StrUtil.isBlank(name)) {
            return;
        }
        StudentTagDO tag = studentTagMapper.selectByName(name);
        if (tag == null) {
            return;
        }
        if (id == null || !tag.getId().equals(id)) {
            throw exception(STUDENT_TAG_NAME_DUPLICATE);
        }
    }

    /**
     * 校验标签颜色属于固定 16 色
     *
     * @param color 颜色值
     */
    private void validateColor(String color) {
        if (!TagColorEnum.isValid(color)) {
            throw exception(STUDENT_TAG_COLOR_INVALID);
        }
    }

    /**
     * 计算「追加到末尾」时使用的序号
     *
     * @return 当前最大序号 + 1；无标签时为 0
     */
    private int nextSort() {
        Integer maxSort = studentTagMapper.selectMaxSort();
        return maxSort == null ? 0 : maxSort + 1;
    }

    /**
     * 插入标签，并把唯一键冲突转换为业务异常
     *
     * 并发下两个请求可能同时通过前置校验，此处依赖数据库唯一索引兜底：
     * 命中名称唯一键 → 名称重复；命中排序唯一键 → 排序数据失效。
     *
     * @param tag 标签
     */
    private void insertTagSafely(StudentTagDO tag) {
        try {
            studentTagMapper.insert(tag);
        } catch (DuplicateKeyException e) {
            throw exception(resolveDuplicate(e, STUDENT_TAG_NAME_DUPLICATE));
        }
    }

    /**
     * 解析唯一键冲突的具体错误码
     *
     * @param e           冲突异常
     * @param defaultCode 默认错误码
     * @return 错误码
     */
    private ErrorCode resolveDuplicate(DuplicateKeyException e, ErrorCode defaultCode) {
        String message = e.getMessage();
        if (message != null && message.contains("uk_tenant_sort")) {
            return STUDENT_TAG_SORT_INVALID;
        }
        return defaultCode;
    }

    /**
     * 序号归一化：按「启用在前、停用在后，同状态内按 sort、id 升序」重新分配 0..n-1
     *
     * 若当前序号已经连续且与展示顺序一致，则直接跳过，避免无谓写库。
     */
    private void renumber() {
        List<StudentTagDO> list = studentTagMapper.selectListAll();
        if (CollUtil.isEmpty(list)) {
            return;
        }
        // 已连续且唯一则无需重排
        boolean needRenumber = false;
        for (int i = 0; i < list.size(); i++) {
            Integer sort = list.get(i).getSort();
            if (sort == null || sort != i) {
                needRenumber = true;
                break;
            }
        }
        if (!needRenumber) {
            return;
        }
        rewriteSort(CollectionUtils.convertList(list, StudentTagDO::getId));
    }

    /**
     * 按给定顺序两阶段写回序号（先负数区、再写最终值），规避唯一索引瞬时冲突
     *
     * @param orderedIds 目标顺序的标签编号集合
     */
    private void rewriteSort(List<Long> orderedIds) {
        if (CollUtil.isEmpty(orderedIds)) {
            return;
        }
        // 阶段一：全部落到负数区（-index-1），此时与任何现有非负序号都不冲突
        List<StudentTagDO> phase1 = new ArrayList<>(orderedIds.size());
        for (int i = 0; i < orderedIds.size(); i++) {
            phase1.add(StudentTagDO.builder().id(orderedIds.get(i)).sort(-i - 1).build());
        }
        studentTagMapper.updateSortBatch(phase1);
        // 阶段二：写入最终连续序号 0..n-1
        List<StudentTagDO> phase2 = new ArrayList<>(orderedIds.size());
        for (int i = 0; i < orderedIds.size(); i++) {
            phase2.add(StudentTagDO.builder().id(orderedIds.get(i)).sort(i).build());
        }
        studentTagMapper.updateSortBatch(phase2);
    }

}
