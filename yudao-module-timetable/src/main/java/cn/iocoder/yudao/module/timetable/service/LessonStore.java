package cn.iocoder.yudao.module.timetable.service;

import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.timetable.controller.admin.vo.LessonSaveReqVO;
import cn.iocoder.yudao.module.timetable.dal.dataobject.ChangeRequestDO;
import cn.iocoder.yudao.module.timetable.dal.dataobject.LessonDO;
import cn.iocoder.yudao.module.timetable.dal.mysql.ChangeRequestMapper;
import cn.iocoder.yudao.module.timetable.dal.mysql.LessonMapper;
import cn.iocoder.yudao.module.timetable.support.TimetableException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;

@Component
public class LessonStore {
    @Resource private LessonMapper mapper;
    @Resource private ChangeRequestMapper changes;
    @Resource private LessonRelations relations;

    public LambdaQueryWrapper<LessonDO> query() {
        return new LambdaQueryWrapper<LessonDO>().eq(LessonDO::getTenantId, TenantContextHolder.getRequiredTenantId());
    }

    public LessonDO find(Long id) {
        TimetableException.require(id != null && id > 0, "课次 ID 无效");
        LessonDO lesson = mapper.selectOne(query().eq(LessonDO::getId, id));
        TimetableException.require(lesson != null, "课次不存在或不在可访问范围");
        return lesson;
    }

    public LessonSaveReqVO request(LessonDO lesson) {
        LessonSaveReqVO request = new LessonSaveReqVO();
        BeanUtils.copyProperties(lesson, request);
        request.setClassIds(relations.getClassIds(lesson.getId()));
        return request;
    }

    public void noPending(Long lessonId) {
        TimetableException.require(changes.selectCount(new LambdaQueryWrapper<ChangeRequestDO>()
                .eq(ChangeRequestDO::getTenantId, TenantContextHolder.getRequiredTenantId())
                .eq(ChangeRequestDO::getLessonId, lessonId).eq(ChangeRequestDO::getStatus, 0)) == 0,
                "该课次存在待审批申请");
    }

    public void update(LessonDO lesson, Long originalVersion) {
        lesson.setVersion(originalVersion + 1);
        int affected = mapper.update(lesson, new LambdaUpdateWrapper<LessonDO>()
                .eq(LessonDO::getTenantId, TenantContextHolder.getRequiredTenantId())
                .eq(LessonDO::getId, lesson.getId()).eq(LessonDO::getVersion, originalVersion));
        TimetableException.require(affected == 1, "课次版本已变化，请刷新后重试");
    }
}
