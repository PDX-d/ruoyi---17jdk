package cn.iocoder.yudao.module.exam.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.exam.controller.admin.vo.ExamVO.*;

public interface ExamMyExamService {
    PageResult<MyExamResp> page(ExamQueryReq request);
    MyExamResp get(Long id);
}
