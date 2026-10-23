package cn.iocoder.yudao.module.exam.service;

import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.system.api.permission.PermissionApi;
import cn.iocoder.yudao.module.exam.api.reference.ExamReferenceData.*;
import cn.iocoder.yudao.module.exam.dal.dataobject.ExamSessionDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;
import static cn.iocoder.yudao.module.exam.enums.ErrorCodeConstants.EXAM_FORBIDDEN;

@Component
public class ExamAccess {
    @Resource private PermissionApi permissions;
    @Resource private ExamReferences references;

    private static void require(boolean value, String message) {
        cn.iocoder.yudao.module.exam.support.ExamException.require(value, EXAM_FORBIDDEN, message);
    }

    public Long accountId() {
        TenantContextHolder.getRequiredTenantId();
        Long id = SecurityFrameworkUtils.getLoginUserId();
        require(id != null, "请先登录");
        return id;
    }

    public boolean isAdmin() {
        return permissions.hasAnyRoles(accountId(), "super_admin")
                || permissions.hasAnyPermissions(accountId(), "exam:admin");
    }

    public void permission(String permission) {
        require(permissions.hasAnyPermissions(accountId(), permission), "没有该考务操作权限");
    }

    public void admin(String permission) {
        permission(permission);
        require(isAdmin(), "仅考务管理员可以执行此操作");
    }

    public Scope scope() {
        if (isAdmin()) {
            return new Scope(true, null, Collections.emptyList());
        }
        Actor actor = references.gateway().actor(accountId());
        require(actor != null && actor.getClassIds() != null, "账号未绑定有效公共身份");
        require("TEACHER".equals(actor.getType()) || "COUNSELOR".equals(actor.getType())
                || "STUDENT".equals(actor.getType()), "公共身份类型不支持");
        require(actor.getClassIds().stream().allMatch(id -> id != null && id > 0), "公共班级绑定无效");
        if ("TEACHER".equals(actor.getType())) {
            ExamRules.id(actor.getTeacherId());
        }
        if ("STUDENT".equals(actor.getType())) {
            ExamRules.id(actor.getStudentId());
        }
        return new Scope(false, actor, references.catalog().getTeachingPairs());
    }

    public Scope staff(String permission) {
        permission(permission);
        return staffScope();
    }

    public Scope venueOptions() {
        require(permissions.hasAnyPermissions(accountId(), "exam:venue:query", "exam:session:query"), "没有考场选项查询权限");
        return staffScope();
    }

    private Scope staffScope() {
        Scope scope = scope();
        require(scope.admin || !"STUDENT".equals(scope.actor.getType()), "学生仅可查询本人考试和签到");
        return scope;
    }

    public void maintain(Collection<Long> classIds) {
        scope().maintain(classIds);
    }

    public Long student(String permission) {
        permission(permission);
        Actor actor = references.gateway().actor(accountId());
        require(actor != null && "STUDENT".equals(actor.getType()), "仅学生可访问本人考试签到");
        ExamRules.id(actor.getStudentId());
        return actor.getStudentId();
    }

    public static final class Scope {
        private final boolean admin;
        private final Actor actor;
        private final List<TeachingPair> pairs;

        private Scope(boolean admin, Actor actor, List<TeachingPair> pairs) {
            this.admin = admin;
            this.actor = actor;
            this.pairs = pairs;
        }

        public boolean isAdmin() {
            return admin;
        }

        public boolean canRead(ExamSessionDO session, Long classId) {
            if (admin) {
                return true;
            }
            if ("COUNSELOR".equals(actor.getType())) {
                return actor.getClassIds().contains(classId);
            }
            return "TEACHER".equals(actor.getType()) && pairs.stream().filter(Objects::nonNull)
                    .anyMatch(pair -> Objects.equals(actor.getTeacherId(), pair.getTeacherId())
                            && Objects.equals(session.getSemesterId(), pair.getSemesterId())
                            && Objects.equals(session.getCourseId(), pair.getCourseId())
                            && Objects.equals(classId, pair.getClassId()));
        }

        public void maintain(Collection<Long> classIds) {
            require(classIds != null && !classIds.isEmpty(), "考试班级不能为空");
            require(admin || "COUNSELOR".equals(actor.getType()) && actor.getClassIds().containsAll(classIds),
                    "仅可维护全部班级均归本人管理的考试");
        }

        public void grade(ExamSessionDO session, Long classId) {
            require(admin || "COUNSELOR".equals(actor.getType()) && canRead(session, classId), "成绩不在可写范围");
        }

        public void sessionQuery(QueryWrapper<ExamSessionDO> query) {
            sessionQuery(query, null);
        }

        public void sessionQuery(QueryWrapper<ExamSessionDO> query, Long classId) {
            if (admin) {
                return;
            }
            if ("COUNSELOR".equals(actor.getType())) {
                if (classId != null && !actor.getClassIds().contains(classId)) {
                    query.eq("id", -1L);
                    return;
                }
                query.inSql("id", classSessions(actor.getClassIds()));
                return;
            }
            List<TeachingPair> allowed = pairs.stream().filter(Objects::nonNull)
                    .filter(pair -> Objects.equals(actor.getTeacherId(), pair.getTeacherId())
                            && pair.getSemesterId() != null && pair.getCourseId() != null && pair.getClassId() != null
                            && (classId == null || classId.equals(pair.getClassId())))
                    .collect(Collectors.toList());
            if (!"TEACHER".equals(actor.getType()) || allowed.isEmpty()) {
                query.eq("id", -1L);
                return;
            }
            query.and(group -> allowed.forEach(pair -> group.or(branch -> branch
                    .eq("semester_id", pair.getSemesterId()).eq("course_id", pair.getCourseId())
                    .inSql("id", classSessions(Collections.singleton(pair.getClassId()))))));
        }

        private String classSessions(Collection<Long> classes) {
            String ids = classes.stream().filter(Objects::nonNull).filter(id -> id > 0)
                    .map(String::valueOf).collect(Collectors.joining(","));
            if (ids.isEmpty()) {
                ids = "-1";
            }
            return "select session_id from exam_session_class where tenant_id="
                    + TenantContextHolder.getRequiredTenantId() + " and deleted=0 and class_id in (" + ids + ")";
        }
    }
}
