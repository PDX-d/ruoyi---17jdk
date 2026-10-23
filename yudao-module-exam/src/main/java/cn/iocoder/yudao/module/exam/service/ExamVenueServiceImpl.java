package cn.iocoder.yudao.module.exam.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.exam.controller.admin.vo.ExamVO.*;
import cn.iocoder.yudao.module.exam.dal.dataobject.ExamVenueDO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import javax.validation.Validator;
import java.util.List;
import java.util.stream.Collectors;
import static cn.iocoder.yudao.module.exam.support.ExamException.require;

@Service
public class ExamVenueServiceImpl implements ExamVenueService {
    @Resource private ExamStore store;
    @Resource private ExamAccess access;
    @Resource private ExamGuard guard;
    @Resource private Validator validator;

    @Override
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public Long create(VenueSaveReq request) {
        access.admin("exam:venue:create");
        ExamRules.valid(validator, request);
        require(request.getId() == null, "创建考场不能指定标识");
        guard.lock();
        access.admin("exam:venue:create");
        ExamVenueDO venue = new ExamVenueDO();
        BeanUtils.copyProperties(request, venue);
        venue.setTenantId(ExamStore.tenant());
        venue.setVersion(0L);
        store.venues.insert(venue);
        return venue.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void update(VenueSaveReq request) {
        access.admin("exam:venue:update");
        ExamRules.valid(validator, request);
        guard.lock();
        ExamVenueDO venue = store.venue(request.getId(), true);
        access.admin("exam:venue:update");
        ExamRules.version(request.getVersion(), venue.getVersion());
        BeanUtils.copyProperties(request, venue);
        venue.setVersion(request.getVersion() + 1);
        require(store.venues.update(venue, ExamStore.<ExamVenueDO>query().eq("id", venue.getId())
                .eq("version", request.getVersion())) == 1, "考场版本已变化");
    }

    @Override
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void delete(Long id, Long version) {
        access.admin("exam:venue:delete");
        guard.lock();
        ExamVenueDO venue = store.venue(id, true);
        access.admin("exam:venue:delete");
        ExamRules.version(version, venue.getVersion());
        require(store.sessions.countAllVenueReferences(ExamStore.tenant(), id) == 0, "考场已被考试引用，只能停用");
        require(store.venues.delete(ExamStore.<ExamVenueDO>query().eq("id", id).eq("version", version)) == 1,
                "考场版本已变化");
    }

    @Override
    public VenueResp get(Long id) {
        access.admin("exam:venue:query");
        return response(store.venue(id, false));
    }

    @Override
    public PageResult<VenueResp> page(VenueQueryReq request) {
        access.admin("exam:venue:query");
        ExamRules.valid(validator, request);
        ExamRules.page(request);
        PageResult<ExamVenueDO> result = store.venues.selectPage(request, ExamStore.<ExamVenueDO>query()
                .like(request.getName() != null, "name", request.getName())
                .eq(request.getStatus() != null, "status", request.getStatus()).orderByDesc("id"));
        return new PageResult<>(result.getList().stream().map(this::response).collect(Collectors.toList()), result.getTotal());
    }

    @Override
    public List<VenueResp> simpleList() {
        access.venueOptions();
        return store.venues.selectList(ExamStore.<ExamVenueDO>query().eq("status", 0).orderByAsc("name", "id"))
                .stream().map(this::response).collect(Collectors.toList());
    }

    private VenueResp response(ExamVenueDO venue) {
        VenueResp result = new VenueResp();
        BeanUtils.copyProperties(venue, result);
        return result;
    }
}
