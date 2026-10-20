package cn.iocoder.yudao.module.edu.api.semester;

import cn.iocoder.yudao.module.edu.api.semester.dto.EduSemesterRespDTO;

/**
 * 学期 API 接口
 *
 * 供其它模块查询学期信息；跨模块只允许走 Api，禁止直接依赖本模块的 Mapper/DO/Service。
 */
public interface EduSemesterApi {

    /**
     * 获得当前学期
     *
     * @return 当前学期，可能为 null
     */
    EduSemesterRespDTO getCurrentSemester();

    /**
     * 获得指定编号的学期
     *
     * @param id 学期编号
     * @return 学期，不存在时返回 null
     */
    EduSemesterRespDTO getSemester(Long id);

}
