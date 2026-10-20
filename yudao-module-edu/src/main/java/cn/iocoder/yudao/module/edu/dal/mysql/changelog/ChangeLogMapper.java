package cn.iocoder.yudao.module.edu.dal.mysql.changelog;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.edu.controller.admin.changelog.vo.ChangeLogPageReqVO;
import cn.iocoder.yudao.module.edu.dal.dataobject.changelog.ChangeLogDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 变更日志 Mapper
 */
@Mapper
public interface ChangeLogMapper extends BaseMapperX<ChangeLogDO> {

    /**
     * 变更日志分页查询
     *
     * @param reqVO 分页查询条件
     * @return 变更日志分页
     */
    default PageResult<ChangeLogDO> selectPage(ChangeLogPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ChangeLogDO>()
                .eqIfPresent(ChangeLogDO::getBizModule, reqVO.getBizModule())
                .eqIfPresent(ChangeLogDO::getBizId, reqVO.getBizId())
                .likeIfPresent(ChangeLogDO::getBizNo, reqVO.getBizNo())
                .likeIfPresent(ChangeLogDO::getBizName, reqVO.getBizName())
                .eqIfPresent(ChangeLogDO::getAction, reqVO.getAction())
                .likeIfPresent(ChangeLogDO::getOperatorName, reqVO.getOperatorName())
                .betweenIfPresent(ChangeLogDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ChangeLogDO::getId));
    }

}
