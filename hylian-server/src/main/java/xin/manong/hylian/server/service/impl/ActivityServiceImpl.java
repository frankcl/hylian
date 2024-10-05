package xin.manong.hylian.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import xin.manong.hylian.model.Pager;
import xin.manong.hylian.server.common.Constants;
import xin.manong.hylian.server.converter.Converter;
import xin.manong.hylian.server.dao.mapper.ActiveRecordMapper;
import xin.manong.hylian.model.ActiveRecord;
import xin.manong.hylian.server.service.ActivityService;
import xin.manong.hylian.server.service.request.ActivitySearchRequest;
import xin.manong.hylian.server.util.Validator;

import javax.annotation.Resource;
import java.util.List;

/**
 * 活动记录服务实现
 *
 * @author frankcl
 * @date 2023-09-04 10:53:42
 */
@Service
public class ActivityServiceImpl implements ActivityService {

    private static final Logger logger = LoggerFactory.getLogger(ActivityServiceImpl.class);

    @Resource
    protected ActiveRecordMapper activeRecordMapper;

    @Override
    public boolean add(ActiveRecord activeRecord) {
        LambdaQueryWrapper<ActiveRecord> query = new LambdaQueryWrapper<>();
        query.eq(ActiveRecord::getSessionId, activeRecord.sessionId).eq(ActiveRecord::getAppId, activeRecord.appId);
        if (activeRecordMapper.selectCount(query) > 0) {
            logger.error("user has login for app[{}] and session[{}]", activeRecord.appId, activeRecord.sessionId);
            throw new IllegalStateException("用户已登录应用");
        }
        return activeRecordMapper.insert(activeRecord) > 0;
    }

    @Override
    public int removeExpires(Long maxUpdateTime) {
        LambdaQueryWrapper<ActiveRecord> query = new LambdaQueryWrapper<>();
        query.lt(ActiveRecord::getUpdateTime, maxUpdateTime);
        return activeRecordMapper.delete(query);
    }

    @Override
    public void remove(String ticketId) {
        LambdaQueryWrapper<ActiveRecord> query = new LambdaQueryWrapper<>();
        query.eq(ActiveRecord::getTicketId, ticketId);
        activeRecordMapper.delete(query);
    }

    @Override
    public boolean remove(String sessionId, String appId) {
        LambdaQueryWrapper<ActiveRecord> query = new LambdaQueryWrapper<>();
        query.eq(ActiveRecord::getSessionId, sessionId).eq(ActiveRecord::getAppId, appId);
        return activeRecordMapper.delete(query) > 0;
    }

    @Override
    public boolean isCheckin(String appId, String sessionId) {
        if (StringUtils.isEmpty(appId)) {
            logger.error("app id is empty");
            throw new RuntimeException("应用ID为空");
        }
        if (StringUtils.isEmpty(sessionId)) {
            logger.error("session id is empty");
            throw new RuntimeException("会话ID为空");
        }
        LambdaQueryWrapper<ActiveRecord> query = new LambdaQueryWrapper<>();
        query.eq(ActiveRecord::getAppId, appId).eq(ActiveRecord::getSessionId, sessionId);
        return activeRecordMapper.selectCount(query) > 0;
    }

    @Override
    public List<ActiveRecord> getWithTicket(String ticketId) {
        LambdaQueryWrapper<ActiveRecord> query = new LambdaQueryWrapper<>();
        query.eq(ActiveRecord::getTicketId, ticketId);
        return activeRecordMapper.selectList(query);
    }

    @Override
    public Pager<ActiveRecord> search(ActivitySearchRequest searchRequest) {
        if (searchRequest == null) searchRequest = new ActivitySearchRequest();
        if (searchRequest.current == null || searchRequest.current < 1) searchRequest.current = Constants.DEFAULT_CURRENT;
        if (searchRequest.size == null || searchRequest.size <= 0) searchRequest.size = Constants.DEFAULT_PAGE_SIZE;
        Validator.validateOrderBy(ActiveRecord.class, searchRequest);
        QueryWrapper<ActiveRecord> query = new QueryWrapper<>();
        searchRequest.prepareOrderBy(query);
        if (StringUtils.isNotEmpty(searchRequest.appId)) query.eq("app_id", searchRequest.appId);
        if (StringUtils.isNotEmpty(searchRequest.userId)) query.eq("user_id", searchRequest.userId);
        if (StringUtils.isNotEmpty(searchRequest.sessionId)) query.eq("session_id", searchRequest.sessionId);
        IPage<ActiveRecord> page = activeRecordMapper.selectPage(new Page<>(searchRequest.current, searchRequest.size), query);
        return Converter.convert(page);
    }
}
