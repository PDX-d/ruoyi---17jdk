package cn.iocoder.yudao.module.edu.service.counselor;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.edu.controller.admin.counselor.vo.CounselorPageReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.counselor.vo.CounselorSaveReqVO;
import cn.iocoder.yudao.module.edu.dal.dataobject.counselor.CounselorDO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 辅导员 Service 接口
 *
 * @author 芋道源码
 */
public interface CounselorService {

    /**
     * 创建辅导员
     *
     * @param createReqVO 创建信息
     * @return 辅导员编号
     */
    Long createCounselor(CounselorSaveReqVO createReqVO);

    /**
     * 更新辅导员
     *
     * @param updateReqVO 更新信息
     */
    void updateCounselor(CounselorSaveReqVO updateReqVO);

    /**
     * 删除辅导员
     *
     * 删除前校验该辅导员名下是否仍有班级未移交
     *
     * @param id 辅导员编号
     */
    void deleteCounselor(Long id);

    /**
     * 获得辅导员
     *
     * @param id 辅导员编号
     * @return 辅导员
     */
    CounselorDO getCounselor(Long id);

    /**
     * 获得辅导员分页
     *
     * @param pageReqVO 分页查询
     * @return 辅导员分页
     */
    PageResult<CounselorDO> getCounselorPage(CounselorPageReqVO pageReqVO);

    /**
     * 获得辅导员所带班级名称列表
     *
     * @param counselorId 辅导员编号
     * @return 班级名称列表
     */
    List<String> getCounselorClassNames(Long counselorId);

    /**
     * 获得辅导员所带班级名称 Map
     *
     * @param counselorIds 辅导员编号集合
     * @return 辅导员编号与班级名称列表的 Map
     */
    Map<Long, List<String>> getCounselorClassNamesMap(Collection<Long> counselorIds);

}
