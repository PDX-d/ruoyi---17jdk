package cn.iocoder.yudao.module.edu.api.semester;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.edu.api.semester.dto.EduSemesterRespDTO;
import cn.iocoder.yudao.module.edu.dal.dataobject.semester.SemesterDO;
import cn.iocoder.yudao.module.edu.service.semester.SemesterService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 学期 API 实现类
 *
 * 对外暴露学期查询能力，内部复用 {@link SemesterService}，返回 DTO 避免其它模块依赖本模块的 DO。
 */
@Service
public class EduSemesterApiImpl implements EduSemesterApi {

    @Resource
    private SemesterService semesterService;

    @Override
    public EduSemesterRespDTO getCurrentSemester() {
        SemesterDO semester = semesterService.getCurrentSemester();
        return BeanUtils.toBean(semester, EduSemesterRespDTO.class);
    }

    @Override
    public EduSemesterRespDTO getSemester(Long id) {
        SemesterDO semester = semesterService.getSemester(id);
        return BeanUtils.toBean(semester, EduSemesterRespDTO.class);
    }

}
