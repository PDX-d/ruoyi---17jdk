package cn.iocoder.yudao.module.exam.service;

import cn.iocoder.yudao.module.exam.controller.admin.vo.ExamVO.*;

public interface ExamCheckInService {
    CheckInResp create(CheckInReq request);
}
