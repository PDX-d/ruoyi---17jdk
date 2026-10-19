package cn.iocoder.yudao.module.timetable.service.reference;

import cn.iocoder.yudao.module.timetable.api.reference.TimetableReferenceGateway;
import cn.iocoder.yudao.module.timetable.support.TimetableException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class TimetableReferences {
    @Resource
    private ObjectProvider<TimetableReferenceGateway> providers;

    public TimetableReferenceGateway get() {
        TimetableReferenceGateway provider = providers.getIfAvailable();
        TimetableException.require(provider != null, "课程表公共数据尚未接入，禁止使用模拟 ID 兜底");
        return provider;
    }
}
