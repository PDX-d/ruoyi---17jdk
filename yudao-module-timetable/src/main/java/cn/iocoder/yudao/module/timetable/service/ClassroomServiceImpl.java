package cn.iocoder.yudao.module.timetable.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.timetable.controller.admin.vo.*;
import cn.iocoder.yudao.module.timetable.dal.dataobject.ClassroomDO;
import cn.iocoder.yudao.module.timetable.dal.mysql.ClassroomMapper;
import cn.iocoder.yudao.module.timetable.support.TimetableException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import javax.annotation.Resource;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ClassroomServiceImpl implements ClassroomService {
    private static final int BATCH_SIZE = 1000;
    @Resource private ClassroomMapper mapper;
    @Resource private TimetableAccess access;
    @Resource private ScheduleGuard guard;

    private LambdaQueryWrapper<ClassroomDO> query() {
        return new LambdaQueryWrapper<ClassroomDO>()
                .eq(ClassroomDO::getTenantId, TenantContextHolder.getRequiredTenantId());
    }

    @Override
    public ClassroomDO find(Long id) {
        TimetableException.require(id != null && id > 0, "教室 ID 无效");
        ClassroomDO classroom = mapper.selectOne(query().eq(ClassroomDO::getId, id));
        TimetableException.require(classroom != null, "教室不存在");
        return classroom;
    }

    @Override
    public Map<Long, ClassroomDO> findMap(Collection<Long> ids) {
        TimetableException.require(ids.stream().allMatch(id -> id != null && id > 0), "教室 ID 无效");
        List<Long> distinctIds = new ArrayList<>(new LinkedHashSet<>(ids));
        Map<Long, ClassroomDO> classrooms = new HashMap<>();
        for (int offset = 0; offset < distinctIds.size(); offset += BATCH_SIZE) {
            List<Long> batch = distinctIds.subList(offset, Math.min(offset + BATCH_SIZE, distinctIds.size()));
            mapper.selectList(query().eq(ClassroomDO::getDeleted, false).in(ClassroomDO::getId, batch))
                    .forEach(classroom -> classrooms.put(classroom.getId(), classroom));
        }
        return classrooms;
    }

    private void unique(ClassroomSaveReqVO request) {
        TimetableException.require(mapper.selectCount(query().eq(ClassroomDO::getCode, request.getCode())
                .ne(request.getId() != null, ClassroomDO::getId, request.getId())) == 0, "教室编号已存在");
    }

    @Override
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public Long create(ClassroomSaveReqVO request) {
        access.permission("timetable:classroom:create");
        access.admin();
        TimetableException.require(request.getId() == null, "创建教室不能指定 ID");
        guard.lock();
        unique(request);
        ClassroomDO classroom = new ClassroomDO();
        BeanUtils.copyProperties(request, classroom);
        classroom.setTenantId(TenantContextHolder.getRequiredTenantId());
        mapper.insert(classroom);
        return classroom.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void update(ClassroomSaveReqVO request) {
        access.permission("timetable:classroom:update");
        access.admin();
        guard.lock();
        ClassroomDO classroom = find(request.getId());
        unique(request);
        BeanUtils.copyProperties(request, classroom);
        mapper.updateById(classroom);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void delete(Long id) {
        access.permission("timetable:classroom:delete");
        access.admin();
        guard.lock();
        find(id);
        TimetableException.require(mapper.countReferences(id, TenantContextHolder.getRequiredTenantId()) == 0,
                "教室已被课次或历史申请引用，只能停用不能删除");
        mapper.delete(query().eq(ClassroomDO::getId, id));
    }

    @Override
    public ClassroomRespVO get(Long id) {
        access.permission("timetable:classroom:query");
        access.admin();
        return response(find(id));
    }

    @Override
    public PageResult<ClassroomRespVO> page(ClassroomPageReqVO request) {
        access.permission("timetable:classroom:query");
        access.admin();
        TimetableException.require(request.getPageSize() > 0, "请使用有效分页大小");
        PageResult<ClassroomDO> page = mapper.selectPage(request, query()
                .like(request.getName() != null, ClassroomDO::getName, request.getName())
                .eq(request.getStatus() != null, ClassroomDO::getStatus, request.getStatus())
                .orderByAsc(ClassroomDO::getCode));
        return new PageResult<>(page.getList().stream().map(this::response).collect(Collectors.toList()), page.getTotal());
    }

    @Override
    public List<ClassroomRespVO> simpleList() {
        access.permission("timetable:classroom:query");
        if (!access.isAdmin()) {
            TimetableException.require(!"STUDENT".equals(access.actor().getType()), "学生不能查询排课可选教室");
        }
        return mapper.selectList(query().eq(ClassroomDO::getStatus, 0).orderByAsc(ClassroomDO::getCode))
                .stream().map(this::response).collect(Collectors.toList());
    }

    private ClassroomRespVO response(ClassroomDO classroom) {
        ClassroomRespVO response = new ClassroomRespVO();
        BeanUtils.copyProperties(classroom, response);
        return response;
    }
}
