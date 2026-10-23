package cn.iocoder.yudao.module.exam.service;

import cn.iocoder.yudao.module.exam.controller.admin.vo.ExamVO.*;
import java.util.List;

public interface ExamReferenceService {
    ReferenceOptionsResp options();
    List<StudentResp> students(Long semesterId, Long courseId, Long classId);
}
