package xin.manong.hylian.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import xin.manong.hylian.server.model.Pager;
import xin.manong.hylian.server.common.Constants;
import xin.manong.hylian.server.converter.Converter;
import xin.manong.hylian.server.dao.mapper.ActivityMapper;
import xin.manong.hylian.model.Activity;
import xin.manong.hylian.server.service.ActivityService;
import xin.manong.hylian.server.service.request.ActivitySearchRequest;
import xin.manong.hylian.server.util.ModelValidator;

import java.util.List;

/**
 * 活动记录服务实现
 *
 * @author frankcl
 * @date 2023-09-04 10:53:42
 */
@Service
public class ActivityServiceImpl implements ActivityService {

    @Resource
    protected ActivityMapper activityMapper;

    @Override
    public boolean add(Activity activity) {
        LambdaQueryWrapper<Activity> query = new LambdaQueryWrapper<>();
        query.eq(Activity::getSessionId, activity.sessionId).eq(Activity::getAppId, activity.appId);
        if (activityMapper.selectCount(query) > 0) throw new IllegalStateException("活动记录已存在");
        return activityMapper.insert(activity) > 0;
    }

    @Override
    public boolean update(Activity activity) {
        Activity prevActivity = activityMapper.selectById(activity.id);
        if (prevActivity == null) throw new NotFoundException("活动记录不存在");
        return activityMapper.updateById(activity) > 0;
    }

    @Override
    public int removeExpires(Long maxUpdateTime) {
        LambdaQueryWrapper<Activity> query = new LambdaQueryWrapper<>();
        query.lt(Activity::getUpdateTime, maxUpdateTime);
        return activityMapper.delete(query);
    }

    @Override
    public void remove(String ticketId) {
        LambdaQueryWrapper<Activity> query = new LambdaQueryWrapper<>();
        query.eq(Activity::getTicketId, ticketId);
        activityMapper.delete(query);
    }

    @Override
    public boolean remove(String sessionId, String appId) {
        LambdaQueryWrapper<Activity> query = new LambdaQueryWrapper<>();
        query.eq(Activity::getSessionId, sessionId).eq(Activity::getAppId, appId);
        return activityMapper.delete(query) > 0;
    }

    @Override
    public Activity get(String appId, String ticketId) {
        if (StringUtils.isEmpty(appId)) throw new BadRequestException("应用ID为空");
        if (StringUtils.isEmpty(ticketId)) throw new BadRequestException("票据ID为空");
        LambdaQueryWrapper<Activity> query = new LambdaQueryWrapper<>();
        query.eq(Activity::getAppId, appId).eq(Activity::getTicketId, ticketId);
        return activityMapper.selectOne(query);
    }

    @Override
    public List<Activity> getWithTicket(String ticketId) {
        LambdaQueryWrapper<Activity> query = new LambdaQueryWrapper<>();
        query.eq(Activity::getTicketId, ticketId);
        return activityMapper.selectList(query);
    }

    @Override
    public Pager<Activity> search(ActivitySearchRequest searchRequest) {
        if (searchRequest == null) searchRequest = new ActivitySearchRequest();
        if (searchRequest.current == null || searchRequest.current < 1) searchRequest.current = Constants.DEFAULT_CURRENT;
        if (searchRequest.size == null || searchRequest.size <= 0) searchRequest.size = Constants.DEFAULT_PAGE_SIZE;
        ModelValidator.validateOrderBy(Activity.class, searchRequest);
        QueryWrapper<Activity> query = new QueryWrapper<>();
        searchRequest.prepareOrderBy(query);
        if (StringUtils.isNotEmpty(searchRequest.appId)) query.eq("app_id", searchRequest.appId);
        if (StringUtils.isNotEmpty(searchRequest.userId)) query.eq("user_id", searchRequest.userId);
        if (StringUtils.isNotEmpty(searchRequest.sessionId)) query.eq("session_id", searchRequest.sessionId);
        if (searchRequest.appIds != null) query.in("app_id", searchRequest.appIds);
        IPage<Activity> page = activityMapper.selectPage(new Page<>(searchRequest.current, searchRequest.size), query);
        return Converter.convert(page);
    }
}
