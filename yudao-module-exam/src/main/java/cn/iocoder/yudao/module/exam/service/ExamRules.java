package cn.iocoder.yudao.module.exam.service;

import static cn.iocoder.yudao.module.exam.support.ExamException.require;
import java.math.BigDecimal;
import java.time.LocalDateTime;

final class ExamRules {
    private ExamRules() {
    }

    static void id(Long value) {
        require(value != null && value > 0, "标识无效");
    }

    static void coordinates(BigDecimal latitude, BigDecimal longitude) {
        require(latitude != null && longitude != null
                && latitude.abs().compareTo(new BigDecimal("90")) <= 0
                && longitude.abs().compareTo(new BigDecimal("180")) <= 0, "WGS84坐标无效");
    }

    static void version(Long expected, Long actual) {
        cn.iocoder.yudao.module.exam.support.ExamException.require(expected != null && expected.equals(actual),
                cn.iocoder.yudao.module.exam.enums.ErrorCodeConstants.EXAM_CONFLICT, "数据版本已变化，请刷新后重试");
    }

    static LocalDateTime now() {
        timezone();
        return LocalDateTime.now(java.time.ZoneId.of("Asia/Shanghai"));
    }

    static void timezone() {
        require(java.time.ZoneId.systemDefault().getRules().equals(java.time.ZoneId.of("Asia/Shanghai").getRules()),
                "考务运行时区须与Asia/Shanghai一致，请设置-Duser.timezone=Asia/Shanghai");
    }

    static void valid(javax.validation.Validator validator, Object request) {
        require(request != null, "请求不能为空");
        java.util.Set<javax.validation.ConstraintViolation<Object>> errors = validator.validate(request);
        require(errors.isEmpty(), errors.isEmpty() ? "参数无效" : errors.iterator().next().getMessage());
    }

    static void page(cn.iocoder.yudao.framework.common.pojo.PageParam request) {
        require(request.getPageNo() != null && request.getPageNo() > 0
                && request.getPageSize() != null && request.getPageSize() > 0
                && request.getPageSize() <= 100, "分页参数无效，每页最多100条");
    }

    static void writeTransaction() {
        require(org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive()
                && !org.springframework.transaction.support.TransactionSynchronizationManager.isCurrentTransactionReadOnly()
                && Integer.valueOf(org.springframework.transaction.TransactionDefinition.ISOLATION_READ_COMMITTED).equals(
                org.springframework.transaction.support.TransactionSynchronizationManager.getCurrentTransactionIsolationLevel()),
                "考务写事务必须使用READ_COMMITTED");
    }
}
