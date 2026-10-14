package cn.iocoder.yudao.module.timetable.service;

import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.timetable.dal.dataobject.LessonClassDO;
import cn.iocoder.yudao.module.timetable.dal.mysql.LessonClassMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class LessonRelations {
    private static final int BATCH_SIZE = 1000;
    @Resource private LessonClassMapper mapper;

    public List<Long> getClassIds(Long lessonId) {
        return mapper.selectList(new LambdaQueryWrapper<LessonClassDO>()
                .eq(LessonClassDO::getTenantId, TenantContextHolder.getRequiredTenantId())
                .eq(LessonClassDO::getLessonId, lessonId).orderByAsc(LessonClassDO::getClassId))
                .stream().map(LessonClassDO::getClassId).collect(Collectors.toList());
    }

    public Map<Long, List<Long>> getClassIdsMap(Collection<Long> lessonIds) {
        List<Long> distinctIds = new ArrayList<>(new LinkedHashSet<>(lessonIds));
        Map<Long, List<Long>> classIds = new HashMap<>();
        for (int offset = 0; offset < distinctIds.size(); offset += BATCH_SIZE) {
            List<Long> batch = distinctIds.subList(offset, Math.min(offset + BATCH_SIZE, distinctIds.size()));
            List<LessonClassDO> rows = mapper.selectList(new LambdaQueryWrapper<LessonClassDO>()
                    .eq(LessonClassDO::getTenantId, TenantContextHolder.getRequiredTenantId())
                    .eq(LessonClassDO::getDeleted, false).in(LessonClassDO::getLessonId, batch)
                    .orderByAsc(LessonClassDO::getClassId));
            rows.forEach(row -> classIds.computeIfAbsent(row.getLessonId(), id -> new ArrayList<>()).add(row.getClassId()));
        }
        return classIds;
    }

    public void replace(Long lessonId, List<Long> classIds) {
        Long tenantId = TenantContextHolder.getRequiredTenantId();
        mapper.removeCurrentRelations(lessonId, tenantId);
        for (Long classId : classIds) {
            LessonClassDO relation = new LessonClassDO();
            relation.setLessonId(lessonId);
            relation.setClassId(classId);
            relation.setTenantId(tenantId);
            mapper.insert(relation);
        }
    }

    public String lessonIdsSql(java.util.Collection<Long> classIds) {
        String ids = classIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        return "SELECT lesson_id FROM timetable_lesson_class WHERE deleted = 0 AND tenant_id = "
                + TenantContextHolder.getRequiredTenantId() + " AND class_id IN (" + ids + ")";
    }
}
