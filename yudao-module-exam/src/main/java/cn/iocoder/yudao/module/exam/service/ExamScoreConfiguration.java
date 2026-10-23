package cn.iocoder.yudao.module.exam.service;

import cn.iocoder.yudao.module.exam.dal.dataobject.*;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import static cn.iocoder.yudao.module.exam.support.ExamException.require;

@Component
public class ExamScoreConfiguration {
    @Resource private ExamStore store;

    public void replace(Long sessionId, List<ExamScoreItemDO> items) {
        require(items != null && !items.isEmpty(), "成绩分项不能为空");
        Set<String> names = new HashSet<>();
        BigDecimal total = BigDecimal.ZERO;
        for (ExamScoreItemDO item : items) {
            require(item != null && item.getName() != null && !item.getName().trim().isEmpty()
                    && item.getName().trim().length() <= 50 && names.add(item.getName().trim()), "分项名称无效或重复");
            require(item.getWeight() != null && item.getWeight().compareTo(BigDecimal.ZERO) >= 0
                    && item.getWeight().compareTo(new BigDecimal("100")) <= 0
                    && item.getWeight().stripTrailingZeros().scale() <= 2, "分项权重必须为0至100且至多两位小数");
            total = total.add(item.getWeight());
        }
        require(total.compareTo(new BigDecimal("100")) == 0, "分项权重合计必须等于100");
        store.items.delete(ExamStore.<ExamScoreItemDO>query().eq("session_id", sessionId));
        int sort = 0;
        for (ExamScoreItemDO item : items) {
            item.setId(null);
            item.setTenantId(ExamStore.tenant());
            item.setSessionId(sessionId);
            item.setName(item.getName().trim());
            item.setSort(sort++);
            store.items.insert(item);
        }
    }

    public List<ExamScoreItemDO> defaults() {
        ExamScoreItemDO usual = new ExamScoreItemDO();
        usual.setName("平时成绩");
        usual.setWeight(new BigDecimal("30"));
        ExamScoreItemDO finalExam = new ExamScoreItemDO();
        finalExam.setName("考试成绩");
        finalExam.setWeight(new BigDecimal("70"));
        return Arrays.asList(usual, finalExam);
    }
}
