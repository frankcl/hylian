package xin.manong.hylian.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xin.manong.hylian.model.Activity;
import xin.manong.hylian.server.config.ServerConfig;
import xin.manong.hylian.server.service.*;
import xin.manong.hylian.server.service.request.ActivitySearchRequest;
import xin.manong.hylian.server.service.request.UserSearchRequest;
import xin.manong.hylian.server.common.Constants;
import xin.manong.hylian.server.converter.Converter;
import xin.manong.hylian.server.dao.mapper.UserMapper;
import xin.manong.hylian.server.model.Pager;
import xin.manong.hylian.model.User;
import xin.manong.hylian.server.util.ModelValidator;
import xin.manong.weapon.aliyun.oss.OSSClient;
import xin.manong.weapon.aliyun.oss.OSSMeta;
import xin.manong.weapon.base.util.FileUtil;
import xin.manong.weapon.base.util.RandomID;

import javax.annotation.Resource;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * 用户服务实现
 *
 * @author frankcl
 * @date 2023-09-01 13:40:43
 */
@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Resource
    protected ServerConfig serverConfig;
    @Resource
    protected OSSClient ossClient;
    @Resource
    protected UserMapper userMapper;
    @Lazy
    @Resource
    protected TenantService tenantService;
    @Lazy
    @Resource
    protected UserRoleService userRoleService;
    @Lazy
    @Resource
    protected ActivityService activityService;
    @Lazy
    @Resource
    protected TicketService ticketService;
    @Lazy
    @Resource
    protected TokenService tokenService;

    @Override
    public User get(String id) {
        if (StringUtils.isEmpty(id)) {
            logger.error("user id is empty for getting");
            throw new BadRequestException("用户ID为空");
        }
        return userMapper.selectById(id);
    }

    @Override
    public User getByUserName(String username) {
        if (StringUtils.isEmpty(username)) {
            logger.error("username is empty");
            throw new BadRequestException("用户名为空");
        }
        UserSearchRequest searchRequest = new UserSearchRequest();
        searchRequest.current = 1;
        searchRequest.size = 1;
        searchRequest.username = username;
        Pager<User> pager = search(searchRequest);
        if (pager == null || pager.total == 0 || pager.records.isEmpty()) return null;
        return pager.records.get(0);
    }

    @Override
    public boolean add(User user) {
        if (tenantService.get(user.tenantId) == null) {
            logger.error("tenant[{}] is not found", user.tenantId);
            throw new NotFoundException("租户不存在");
        }
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
        query.eq(User::getId, user.id).or().eq(User::getUsername, user.username);
        if (userMapper.selectCount(query) > 0) {
            logger.error("user has existed for the same id[{}] or username[{}]", user.id, user.username);
            throw new IllegalStateException("用户已存在");
        }
        user.password = DigestUtils.md5Hex(user.password.trim());
        saveAvatar(user);
        return userMapper.insert(user) > 0;
    }

    @Override
    public boolean update(User user) {
        User prevUser = userMapper.selectById(user.id);
        if (prevUser == null) {
            logger.error("user is not found for id[{}]", user.id);
            throw new NotFoundException("用户不存在");
        }
        saveAvatar(user);
        user.username = null;
        if (user.password != null) user.password = DigestUtils.md5Hex(user.password);
        boolean result = userMapper.updateById(user) > 0;
        if (StringUtils.isNotEmpty(user.avatar) && result) deleteAvatar(prevUser);
        if (result && !prevUser.disabled && user.disabled) removeUserProfile(user);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(String id) {
        if (StringUtils.isEmpty(id)) {
            logger.error("user id is empty for deleting");
            throw new BadRequestException("用户ID为空");
        }
        User user = get(id);
        if (user == null) {
            logger.error("user[{}] is not found for deleting", id);
            throw new IllegalStateException("用户不存在");
        }
        boolean result = userMapper.deleteById(id) > 0;
        if (result) {
            userRoleService.deleteByUser(id);
            deleteAvatar(user);
            removeUserProfile(user);
        }
        return result;
    }

    @Override
    public Pager<User> search(UserSearchRequest searchRequest) {
        if (searchRequest == null) searchRequest = new UserSearchRequest();
        if (searchRequest.current == null || searchRequest.current < 1) searchRequest.current = Constants.DEFAULT_CURRENT;
        if (searchRequest.size == null || searchRequest.size <= 0) searchRequest.size = Constants.DEFAULT_PAGE_SIZE;
        ModelValidator.validateOrderBy(User.class, searchRequest);
        QueryWrapper<User> query = new QueryWrapper<>();
        searchRequest.prepareOrderBy(query);
        if (!StringUtils.isEmpty(searchRequest.username)) query.eq("username", searchRequest.username);
        if (!StringUtils.isEmpty(searchRequest.tenantId)) query.eq("tenant_id", searchRequest.tenantId);
        if (!StringUtils.isEmpty(searchRequest.name)) query.like("name", searchRequest.name);
        if (searchRequest.disabled != null) query.eq("disabled", searchRequest.disabled ? 1 : 0);
        IPage<User> page = userMapper.selectPage(new Page<>(searchRequest.current, searchRequest.size), query);
        return Converter.convert(page);
    }

    @Override
    public void removeUserProfile(String id) {
        Set<String> tokenIds = ticketService.getTokens(id);
        for (String tokenId : tokenIds) tokenService.removeTokenWithId(tokenId);
        ticketService.removeTokens(id);
        ticketService.removeTicket(id);
    }

    /**
     * 移除用户登录信息
     *
     * @param user 用户
     */
    private void removeUserProfile(User user) {
        ActivitySearchRequest request = new ActivitySearchRequest();
        request.userId = user.id;
        request.size = 100;
        Pager<Activity> pager = activityService.search(request);
        if (pager.records == null || pager.records.isEmpty()) return;
        Set<String> tickets = new HashSet<>();
        for (Activity activity : pager.records) {
            if (tickets.contains(activity.ticketId)) continue;
            removeUserProfile(activity.ticketId);
            tickets.add(activity.ticketId);
        }
    }

    /**
     * 删除头像
     *
     * @param user 用户信息
     */
    private void deleteAvatar(User user) {
        if (StringUtils.isEmpty(user.avatar)) return;
        OSSMeta meta = OSSClient.parseURL(user.avatar);
        if (meta != null) ossClient.deleteObject(meta.bucket, meta.key);
        else logger.warn("avatar[{}] is invalid", user.avatar);
    }

    /**
     * 转存头像
     *
     * @param user 用户信息
     */
    private void saveAvatar(User user) {
        if (StringUtils.isEmpty(user.avatar)) return;
        OSSMeta ossMeta = OSSClient.parseURL(user.avatar);
        if (ossMeta == null) {
            logger.error("avatar URL[{}] is invalid", user.avatar);
            throw new BadRequestException("头像URL非法");
        }
        InputStream inputStream = ossClient.getObjectStream(ossMeta.bucket, ossMeta.key);
        if (inputStream == null) {
            logger.error("reading avatar failed for {}", user.avatar);
            throw new BadRequestException("获取头像数据失败");
        }
        String suffix = FileUtil.getFileSuffix(ossMeta.key);
        String ossKey = String.format("%s%s%s", serverConfig.ossBaseDirectory, Constants.AVATAR_DIR, RandomID.build());
        if (StringUtils.isNotEmpty(suffix)) ossKey = String.format("%s.%s", ossKey, suffix);
        if (!ossClient.putObject(serverConfig.ossBucket, ossKey, inputStream)) {
            logger.error("transfer avatar failed");
            throw new InternalServerErrorException("转存头像失败");
        }
        user.avatar = OSSClient.buildURL(new OSSMeta(serverConfig.ossRegion, serverConfig.ossBucket, ossKey));
    }
}
