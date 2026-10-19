package cn.iocoder.yudao.module.edu.service.counselor;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.edu.controller.admin.counselor.vo.CounselorPageReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.counselor.vo.CounselorSaveReqVO;
import cn.iocoder.yudao.module.edu.dal.dataobject.counselor.CounselorClassDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.counselor.CounselorDO;
import cn.iocoder.yudao.module.edu.dal.mysql.counselor.CounselorClassMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.counselor.CounselorMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.*;

/**
 * 辅导员 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class CounselorServiceImpl implements CounselorService {

    @Resource
    private CounselorMapper counselorMapper;

    @Resource
    private CounselorClassMapper counselorClassMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCounselor(CounselorSaveReqVO createReqVO) {
        // 1. 校验工号唯一
        validateJobNoUnique(null, createReqVO.getJobNo());
        // 2. 校验所带班级有效性（一个班级只能有一个主辅导员）
        validateClassNames(null, createReqVO.getClassNames());
        // 3. 插入辅导员
        CounselorDO counselor = BeanUtils.toBean(createReqVO, CounselorDO.class);
        try {
            counselorMapper.insert(counselor);
        } catch (DuplicateKeyException e) {
            throw exception(COUNSELOR_JOB_NO_DUPLICATE);
        }
        // 4. 保存所带班级
        saveCounselorClasses(counselor.getId(), createReqVO.getClassNames());
        return counselor.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCounselor(CounselorSaveReqVO updateReqVO) {
        // 1. 锁定辅导员行，避免与 deleteCounselor 并发产生孤儿班级数据
        CounselorDO counselor = counselorMapper.selectByIdForUpdate(updateReqVO.getId());
        if (counselor == null) {
            throw exception(COUNSELOR_NOT_EXISTS);
        }
        // 2. 校验工号唯一
        validateJobNoUnique(updateReqVO.getId(), updateReqVO.getJobNo());
        // 3. 校验所带班级有效性（一个班级只能有一个主辅导员）
        validateClassNames(updateReqVO.getId(), updateReqVO.getClassNames());
        // 4. 更新辅导员
        CounselorDO updateObj = BeanUtils.toBean(updateReqVO, CounselorDO.class);
        try {
            if (counselorMapper.updateById(updateObj) != 1) {
                throw exception(COUNSELOR_NOT_EXISTS);
            }
        } catch (DuplicateKeyException e) {
            throw exception(COUNSELOR_JOB_NO_DUPLICATE);
        }
        // 5. 保存所带班级
        saveCounselorClasses(updateReqVO.getId(), updateReqVO.getClassNames());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCounselor(Long id) {
        // 1. 锁定辅导员行，避免与 updateCounselor 并发产生孤儿班级数据
        CounselorDO counselor = counselorMapper.selectByIdForUpdate(id);
        if (counselor == null) {
            throw exception(COUNSELOR_NOT_EXISTS);
        }
        // 2. 校验名下没有未移交的班级
        if (counselorClassMapper.selectCountByCounselorId(id) > 0) {
            throw exception(COUNSELOR_CLASS_NOT_TRANSFERRED);
        }
        // 3. 删除辅导员
        if (counselorMapper.deleteById(id) != 1) {
            throw exception(COUNSELOR_NOT_EXISTS);
        }
    }

    @Override
    public CounselorDO getCounselor(Long id) {
        return counselorMapper.selectById(id);
    }

    @Override
    public PageResult<CounselorDO> getCounselorPage(CounselorPageReqVO pageReqVO) {
        return counselorMapper.selectPage(pageReqVO);
    }

    @Override
    public List<String> getCounselorClassNames(Long counselorId) {
        return CollectionUtils.convertList(counselorClassMapper.selectListByCounselorId(counselorId),
                CounselorClassDO::getClassName);
    }

    @Override
    public Map<Long, List<String>> getCounselorClassNamesMap(Collection<Long> counselorIds) {
        if (CollUtil.isEmpty(counselorIds)) {
            return Collections.emptyMap();
        }
        return CollectionUtils.convertMultiMap(counselorClassMapper.selectListByCounselorIds(counselorIds),
                CounselorClassDO::getCounselorId, CounselorClassDO::getClassName);
    }


    private void validateJobNoUnique(Long id, String jobNo) {
        if (jobNo == null) {
            return;
        }
        CounselorDO counselor = counselorMapper.selectByJobNo(jobNo);
        if (counselor == null) {
            return;
        }
        if (id == null || !counselor.getId().equals(id)) {
            throw exception(COUNSELOR_JOB_NO_DUPLICATE);
        }
    }

    /**
     * 校验所带班级是否已被其他辅导员担任主辅导员
     *
     * @param counselorId 当前辅导员编号（新增时为 null）
     * @param classNames  班级名称列表
     */
    private void validateClassNames(Long counselorId, List<String> classNames) {
        if (CollUtil.isEmpty(classNames)) {
            return;
        }
        for (String className : classNames) {
            List<CounselorClassDO> list = counselorClassMapper.selectListByClassName(className);
            boolean assignedByOther = list.stream()
                    .anyMatch(item -> !Objects.equals(item.getCounselorId(), counselorId));
            if (assignedByOther) {
                throw exception(COUNSELOR_CLASS_ALREADY_ASSIGNED, className);
            }
        }
    }

    /**
     * 保存辅导员的所带班级：先删除旧关联，再插入新关联
     *
     * @param counselorId 辅导员编号
     * @param classNames  班级名称列表
     */
    private void saveCounselorClasses(Long counselorId, List<String> classNames) {
        counselorClassMapper.deleteByCounselorId(counselorId);
        if (CollUtil.isEmpty(classNames)) {
            return;
        }
        List<CounselorClassDO> classList = CollectionUtils.convertList(classNames, className ->
                CounselorClassDO.builder().counselorId(counselorId).className(className).build());
        try {
            counselorClassMapper.insertBatch(classList);
        } catch (DuplicateKeyException e) {
            throw exception(COUNSELOR_CLASS_DUPLICATE);
        }
    }

}
