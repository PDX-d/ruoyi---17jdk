package cn.iocoder.yudao.module.timetable.service;

import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.system.api.permission.PermissionApi;
import cn.iocoder.yudao.module.timetable.api.reference.ReferenceData;
import cn.iocoder.yudao.module.timetable.dal.dataobject.LessonDO;
import cn.iocoder.yudao.module.timetable.service.reference.TimetableReferences;
import cn.iocoder.yudao.module.timetable.support.TimetableException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

@Component
public class TimetableAccess {
    @Resource private PermissionApi permissions;
    @Resource private TimetableReferences references;
    @Resource private LessonRelations relations;

    public Long accountId() {
        Long id = SecurityFrameworkUtils.getLoginUserId();
        TimetableException.require(id != null, "请先登录");
        return id;
    }

    public boolean isAdmin() {
        return permissions.hasAnyRoles(accountId(), "super_admin")
                || permissions.hasAnyPermissions(accountId(), "timetable:admin");
    }

    public void permission(String permission) {
        TimetableException.require(permissions.hasAnyPermissions(accountId(), permission), "没有该课程表操作权限");
    }

    public void admin() {
        TimetableException.require(isAdmin(), "仅课程表管理员可以执行此操作");
    }

    public ReferenceData.Actor actor() {
        ReferenceData.Actor actor = references.get().getActor(accountId());
        TimetableException.require(actor != null, "登录账号尚未绑定教师、学生或辅导员身份");
        TimetableException.require("TEACHER".equals(actor.getType()) || "STUDENT".equals(actor.getType())
                || "COUNSELOR".equals(actor.getType()), "公共身份类型不支持");
        TimetableException.require(actor.getClassIds() != null
                && actor.getClassIds().stream().allMatch(id -> id != null && id > 0), "公共身份班级绑定无效");
        TimetableException.require(!"TEACHER".equals(actor.getType())
                || actor.getTeacherId() != null && actor.getTeacherId() > 0, "教师身份未绑定教师 ID");
        TimetableException.require(!"STUDENT".equals(actor.getType())
                || actor.getStudentId() != null && actor.getStudentId() > 0, "学生身份未绑定学生 ID");
        return actor;
    }

    public void scope(LambdaQueryWrapper<LessonDO> query) {
        if (isAdmin()) {
            return;
        }
        ReferenceData.Actor actor = actor();
        if ("TEACHER".equals(actor.getType())) {
            query.eq(LessonDO::getTeacherId, actor.getTeacherId());
        } else if (actor.getClassIds().isEmpty()) {
            query.eq(LessonDO::getId, -1L);
        } else {
            query.inSql(LessonDO::getId, relations.lessonIdsSql(actor.getClassIds()));
        }
    }

    public boolean canRead(LessonDO lesson, Collection<Long> classIds) {
        if (isAdmin()) {
            return true;
        }
        ReferenceData.Actor actor = actor();
        return "TEACHER".equals(actor.getType()) ? Objects.equals(actor.getTeacherId(), lesson.getTeacherId())
                : !Collections.disjoint(actor.getClassIds(), classIds);
    }

    public void read(LessonDO lesson, Collection<Long> classIds) {
        TimetableException.require(canRead(lesson, classIds), "课次不存在或不在可访问范围");
    }

    public void maintain(Collection<Long> classIds) {
        if (isAdmin()) {
            return;
        }
        ReferenceData.Actor actor = actor();
        TimetableException.require("COUNSELOR".equals(actor.getType()) && !classIds.isEmpty()
                && actor.getClassIds().containsAll(classIds), "仅可维护全部班级都属于本人管理范围的课次");
    }

    public void teacher(LessonDO lesson) {
        ReferenceData.Actor actor = actor();
        TimetableException.require("TEACHER".equals(actor.getType())
                && Objects.equals(actor.getTeacherId(), lesson.getTeacherId()), "教师仅可申请调整本人授课课次");
    }

    public void classTarget(Long classId) {
        if (!isAdmin()) {
            ReferenceData.Actor actor = actor();
            if (!"TEACHER".equals(actor.getType())) {
                TimetableException.require(actor.getClassIds().contains(classId), "目标班级不在可访问范围");
            }
        }
    }
}
