package cn.iocoder.yudao.server.exam;

import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.exam.api.reference.ExamReferenceData;
import cn.iocoder.yudao.module.exam.api.reference.ExamReferenceGateway;
import cn.iocoder.yudao.module.exam.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.exam.support.ExamException;
import cn.iocoder.yudao.module.timetable.api.reference.ReferenceData;
import cn.iocoder.yudao.module.timetable.api.reference.TimetableReferenceGateway;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.ObjectProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

final class ExamMockGateway implements ExamReferenceGateway {
    private final ExamMockProperties properties;
    private final ObjectProvider<TimetableReferenceGateway> timetableGateway;

    ExamMockGateway(ExamMockProperties properties, ObjectProvider<TimetableReferenceGateway> timetableGateway) {
        this.properties = properties;
        this.timetableGateway = timetableGateway;
    }

    private void require(boolean condition, String message) {
        ExamException.require(condition, ErrorCodeConstants.EXAM_REFERENCE_UNAVAILABLE, message);
    }

    private TimetableReferenceGateway source() {
        require(properties.getTenantId() != null
                && Objects.equals(properties.getTenantId(), TenantContextHolder.getRequiredTenantId()),
                "考务模拟数据必须显式配置当前测试租户");
        TimetableReferenceGateway source = timetableGateway.getIfAvailable();
        require(source != null, "考务模拟模式缺少课表公共数据适配器");
        return source;
    }

    @Override
    public ExamReferenceData.Catalog snapshot() {
        ReferenceData.Catalog source = source().getCatalog();
        require(source != null && source.getSemesters() != null && source.getCourses() != null
                && source.getClasses() != null && source.getTeachers() != null, "课表公共数据目录不完整");
        require(properties.getTeachingPairs() != null && !properties.getTeachingPairs().isEmpty(),
                "考务模拟模式尚未配置精准授课关联");
        ExamReferenceData.Catalog result = new ExamReferenceData.Catalog();
        result.setMock(true);
        result.setCurrentSemesterId(source.getCurrentSemesterId());
        List<ExamReferenceData.Semester> semesters = new ArrayList<>();
        for (ReferenceData.Semester item : source.getSemesters()) {
            require(item != null && item.getId() != null, "公共学期资料不完整");
            ExamReferenceData.Semester target = new ExamReferenceData.Semester();
            BeanUtils.copyProperties(item, target);
            semesters.add(target);
        }
        List<ExamReferenceData.Course> courses = new ArrayList<>();
        for (ReferenceData.Course item : source.getCourses()) {
            require(item != null && item.getId() != null && item.getClassIds() != null
                    && item.getTeacherIds() != null, "公共课程资料不完整");
            ExamReferenceData.Course target = new ExamReferenceData.Course();
            BeanUtils.copyProperties(item, target);
            target.setClassIds(new LinkedHashSet<>(item.getClassIds()));
            courses.add(target);
        }
        List<ExamReferenceData.TeachingPair> pairs = new ArrayList<>();
        for (ExamReferenceData.TeachingPair item : properties.getTeachingPairs()) {
            require(item != null && item.getSemesterId() != null && item.getTeacherId() != null
                    && item.getCourseId() != null && item.getClassId() != null, "考务精准授课关联缺少字段");
            ReferenceData.Course course = source.getCourses().stream()
                    .filter(value -> Objects.equals(value.getId(), item.getCourseId())).findFirst().orElse(null);
            require(source.getSemesters().stream().anyMatch(value -> Objects.equals(value.getId(), item.getSemesterId()))
                    && source.getTeachers().containsKey(item.getTeacherId()) && source.getClasses().containsKey(item.getClassId())
                    && course != null && course.getTeacherIds().contains(item.getTeacherId())
                    && course.getClassIds().contains(item.getClassId()), "考务精准授课关联引用不存在或不匹配");
            ExamReferenceData.TeachingPair target = new ExamReferenceData.TeachingPair();
            BeanUtils.copyProperties(item, target);
            pairs.add(target);
        }
        result.setSemesters(semesters);
        result.setCourses(courses);
        result.setClasses(new HashMap<>(source.getClasses()));
        result.setTeachingPairs(pairs);
        return result;
    }

    @Override
    public ExamReferenceData.Actor actor(Long accountId) {
        ReferenceData.Actor actor = source().getActor(accountId);
        require(actor != null && actor.getClassIds() != null, "当前账号未显式绑定完整公共身份");
        ExamReferenceData.Actor result = new ExamReferenceData.Actor();
        BeanUtils.copyProperties(actor, result);
        result.setClassIds(new LinkedHashSet<>(actor.getClassIds()));
        if ("STUDENT".equals(actor.getType())) {
            require(actor.getStudentId() != null && properties.getStudents().containsKey(actor.getStudentId()),
                    "学生账号缺少显式学生资料绑定");
        }
        return result;
    }

    @Override
    public List<ExamReferenceData.Student> students(Long classId) {
        TimetableReferenceGateway source = source();
        require(source.getClassName(classId) != null, "班级不存在");
        List<ReferenceData.Student> members = source.getClassStudents(classId);
        require(members != null, "班级学生名册尚未接入");
        List<ExamReferenceData.Student> result = new ArrayList<>();
        Set<Long> ids = new HashSet<>();
        Set<String> numbers = new HashSet<>();
        for (ReferenceData.Student member : members) {
            require(member != null && member.getId() != null && ids.add(member.getId()), "班级学生ID缺失或重复");
            ExamReferenceData.Student configured = properties.getStudents().get(member.getId());
            require(configured != null && Objects.equals(configured.getId(), member.getId())
                    && Objects.equals(configured.getClassId(), classId), "学生资料缺失或班级绑定不匹配");
            require(configured.getName() != null && !configured.getName().trim().isEmpty()
                    && configured.getStudentNo() != null && !configured.getStudentNo().trim().isEmpty()
                    && numbers.add(configured.getStudentNo()), "学生姓名学号缺失或班内学号重复");
            ExamReferenceData.Student target = new ExamReferenceData.Student();
            BeanUtils.copyProperties(configured, target);
            result.add(target);
        }
        return result;
    }
}
