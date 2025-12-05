package xin.manong.hylian.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
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
import xin.manong.hylian.server.wechat.NoticeUserAudit;
import xin.manong.weapon.aliyun.oss.OSSClient;
import xin.manong.weapon.aliyun.oss.OSSMeta;
import xin.manong.weapon.base.util.FileUtil;
import xin.manong.weapon.base.util.RandomID;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CountDownLatch;

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
    private ServerConfig serverConfig;
    @Resource
    private OSSClient ossClient;
    @Resource
    private UserMapper userMapper;
    @Lazy
    @Resource
    private TenantService tenantService;
    @Lazy
    @Resource
    private UserRoleService userRoleService;
    @Lazy
    @Resource
    private AppUserService appUserService;
    @Lazy
    @Resource
    private ActivityService activityService;
    @Lazy
    @Resource
    private TicketService ticketService;
    @Lazy
    @Resource
    private TokenService tokenService;
    @Lazy
    @Resource
    private WechatService wechatService;

    @Override
    public User get(String id) {
        if (StringUtils.isEmpty(id)) throw new BadRequestException("用户ID为空");
        return userMapper.selectById(id);
    }

    @Override
    public User getByWxOpenid(String wxOpenid) {
        if (StringUtils.isEmpty(wxOpenid)) throw new BadRequestException("微信openid为空");
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
        query.eq(User::getWxOpenid, wxOpenid);
        return userMapper.selectOne(query);
    }

    @Override
    public List<User> batchGet(List<String> ids) {
        if (ids == null || ids.isEmpty()) return new ArrayList<>();
        List<User> users = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch countDownLatch = new CountDownLatch(ids.size());
        ids.stream().parallel().forEach(id -> {
            try {
                User user = userMapper.selectById(id);
                if (user != null) users.add(user);
            } catch (Exception e) {
                logger.warn(e.getMessage(), e);
            } finally {
                countDownLatch.countDown();
            }
        });
        try {
            countDownLatch.await();
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        return new ArrayList<>(users);
    }

    @Override
    public User getByUserName(String username) {
        if (StringUtils.isEmpty(username)) throw new BadRequestException("用户名为空");
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
        query.eq(User::getUsername, username);
        return userMapper.selectOne(query);
    }

    @Override
    public boolean add(User user) {
        if (tenantService.get(user.tenantId) == null) throw new NotFoundException("租户不存在");
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
        query.eq(User::getId, user.id).or().eq(User::getUsername, user.username);
        if (userMapper.selectCount(query) > 0) throw new IllegalStateException("用户名已存在");
        if (StringUtils.isNotEmpty(user.wxOpenid)) {
            query = new LambdaQueryWrapper<>();
            query.eq(User::getWxOpenid, user.wxOpenid);
            if (userMapper.selectCount(query) > 0) throw new IllegalStateException("微信用户已存在");
        }
        user.password = DigestUtils.md5Hex(user.password.trim());
        saveAvatar(user);
        return userMapper.insert(user) > 0;
    }

    @Override
    public boolean update(User user) {
        if (user.username != null) {
            LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
            query.ne(User::getId, user.id).eq(User::getUsername, user.username);
            if (userMapper.selectCount(query) > 0) throw new BadRequestException("用户名已存在");
        }
        User prevUser = userMapper.selectById(user.id);
        if (prevUser == null) throw new NotFoundException("用户不存在");
        avatarNeedUpdate(user, prevUser);
        saveAvatar(user);
        if (user.password != null) user.password = DigestUtils.md5Hex(user.password);
        boolean result = userMapper.updateById(user) > 0;
        if (StringUtils.isNotEmpty(user.avatar) && result) deleteAvatar(prevUser);
        if (result && !prevUser.disabled && user.disabled != null && user.disabled) {
            removeUserProfile(user);
            sendWechatNotice(prevUser, "账号禁用");
        }
        if (result && user.disabled != null && !user.disabled &&
                prevUser.disabled != null && prevUser.disabled) {
            sendWechatNotice(prevUser, "审核通过");
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(String id) {
        if (StringUtils.isEmpty(id)) throw new BadRequestException("用户ID为空");
        User user = get(id);
        if (user == null) throw new NotFoundException("用户不存在");
        boolean success = userMapper.deleteById(id) > 0;
        if (success) {
            userRoleService.deleteByUser(id);
            appUserService.deleteByUser(id);
            deleteAvatar(user);
            removeUserProfile(user);
            activityService.removeByUserId(user.id);
        }
        return success;
    }

    @Override
    public boolean removeAvatar(String id) {
        User user = get(id);
        if (user == null) throw new NotFoundException("用户不存在");
        deleteAvatar(user);
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, user.id).set(User::getAvatar, null);
        return userMapper.update(updateWrapper) > 0;
    }

    @Override
    public Pager<User> search(UserSearchRequest searchRequest) {
        if (searchRequest == null) searchRequest = new UserSearchRequest();
        if (searchRequest.pageNum == null || searchRequest.pageNum < 1) searchRequest.pageNum = Constants.DEFAULT_PAGE_NUM;
        if (searchRequest.pageSize == null || searchRequest.pageSize <= 0) searchRequest.pageSize = Constants.DEFAULT_PAGE_SIZE;
        ModelValidator.validateOrderBy(User.class, searchRequest);
        QueryWrapper<User> query = new QueryWrapper<>();
        searchRequest.prepareOrderBy(query);
        searchRequest.idList = ModelValidator.validateListField(searchRequest.ids, String.class);
        if (!StringUtils.isEmpty(searchRequest.username)) query.eq("username", searchRequest.username);
        if (!StringUtils.isEmpty(searchRequest.tenantId)) query.eq("tenant_id", searchRequest.tenantId);
        if (!StringUtils.isEmpty(searchRequest.name)) query.like("name", searchRequest.name);
        if (searchRequest.idList != null) query.in("id", searchRequest.idList);
        if (searchRequest.disabled != null) query.eq("disabled", searchRequest.disabled ? 1 : 0);
        if (searchRequest.registerMode != null) query.eq("register_mode", searchRequest.registerMode);
        if (searchRequest.bindWechat != null) {
            if (searchRequest.bindWechat) query.isNotNull("wx_openid").ne("wx_openid", "");
            else query.and(wrapper -> wrapper.eq("wx_openid", "").or().isNull("wx_openid"));
        }
        IPage<User> page = userMapper.selectPage(new Page<>(searchRequest.pageNum, searchRequest.pageSize), query);
        return Converter.convert(page);
    }

    @Override
    public List<User> getUsers() {
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
        query.eq(User::getDisabled, false);
        return userMapper.selectList(query);
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
        request.pageSize = 100;
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
        else logger.warn("Avatar:{} is invalid", user.avatar);
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
            logger.error("Avatar URL:{} is invalid", user.avatar);
            throw new BadRequestException("头像URL非法");
        }
        InputStream inputStream = ossClient.getObjectStream(ossMeta.bucket, ossMeta.key);
        if (inputStream == null) {
            logger.error("Reading avatar failed for {}", user.avatar);
            throw new BadRequestException("获取头像数据失败");
        }
        String suffix = FileUtil.getFileSuffix(ossMeta.key);
        String ossKey = String.format("%s%s%s", serverConfig.ossBaseDirectory, Constants.AVATAR_DIR, RandomID.build());
        if (StringUtils.isNotEmpty(suffix)) ossKey = String.format("%s.%s", ossKey, suffix);
        if (!ossClient.putObject(serverConfig.ossBucket, ossKey, inputStream)) {
            logger.error("Transfer avatar failed");
            throw new InternalServerErrorException("转存头像失败");
        }
        ossClient.deleteObject(ossMeta.bucket, ossMeta.key);
        user.avatar = OSSClient.buildURL(new OSSMeta(serverConfig.ossRegion, serverConfig.ossBucket, ossKey));
    }

    /**
     * 如果更新头像和现存头像一致则不更新
     *
     * @param user 更新信息
     * @param prevUser 当前信息
     */
    private void avatarNeedUpdate(User user, User prevUser) {
        if (StringUtils.isEmpty(prevUser.avatar) || StringUtils.isEmpty(user.avatar)) return;
        OSSMeta prevMeta = OSSClient.parseURL(prevUser.avatar);
        if (prevMeta == null) return;
        OSSMeta meta = OSSClient.parseURL(user.avatar);
        if (meta != null && meta.equals(prevMeta)) user.avatar = null;
    }

    /**
     * 发送微信通知
     *
     * @param user 用户信息
     * @param status 通知状态
     */
    private void sendWechatNotice(User user, String status) {
        if (StringUtils.isEmpty(user.wxOpenid)) return;
        String openid = user.wxOpenid;
        if (openid.startsWith(WechatServiceImpl.OPENID_PREFIX)) {
            openid = openid.substring(WechatServiceImpl.OPENID_PREFIX.length());
        }
        NoticeUserAudit noticeUserAudit = new NoticeUserAudit(user.name, status);
        if (!wechatService.sendMessage(openid, serverConfig.wechatNoticeUserAudit,
                noticeUserAudit.toMap())) {
            logger.warn("Send user audit notice failed for user:{}", user.name);
        }
    }
}
