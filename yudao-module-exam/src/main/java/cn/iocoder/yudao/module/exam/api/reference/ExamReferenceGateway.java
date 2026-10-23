package cn.iocoder.yudao.module.exam.api.reference;

import java.util.List;

public interface ExamReferenceGateway {
    ExamReferenceData.Catalog snapshot();
    ExamReferenceData.Actor actor(Long accountId);
    List<ExamReferenceData.Student> students(Long classId);
}

