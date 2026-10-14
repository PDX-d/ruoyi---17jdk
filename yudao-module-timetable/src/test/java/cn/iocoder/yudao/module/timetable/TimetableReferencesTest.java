package cn.iocoder.yudao.module.timetable;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.timetable.api.reference.TimetableReferenceGateway;
import cn.iocoder.yudao.module.timetable.service.reference.TimetableReferences;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class TimetableReferencesTest {
    @Test
    void missingGatewayFailsClosedInsteadOfInventingMockIds() {
        DefaultListableBeanFactory beans = new DefaultListableBeanFactory();
        TimetableReferences references = new TimetableReferences();
        ReflectionTestUtils.setField(references, "providers", beans.getBeanProvider(TimetableReferenceGateway.class));
        ServiceException error = assertThrows(ServiceException.class, references::get);
        assertTrue(error.getMessage().contains("公共数据尚未接入"));
    }

    @Test
    void usesExplicitlyRegisteredGateway() {
        DefaultListableBeanFactory beans = new DefaultListableBeanFactory();
        TimetableReferenceGateway gateway = mock(TimetableReferenceGateway.class);
        beans.registerSingleton("testGateway", gateway);
        TimetableReferences references = new TimetableReferences();
        ReflectionTestUtils.setField(references, "providers", beans.getBeanProvider(TimetableReferenceGateway.class));
        assertSame(gateway, references.get());
    }
}
