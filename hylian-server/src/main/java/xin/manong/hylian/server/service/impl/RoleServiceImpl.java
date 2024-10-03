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
import xin.manong.hylian.model.Role;
import xin.manong.hylian.server.common.Constants;
import xin.manong.hylian.server.converter.Converter;
import xin.manong.hylian.server.dao.mapper.RoleMapper;
import xin.manong.hylian.server.service.RoleService;
import xin.manong.hylian.server.service.request.RoleSearchRequest;
import xin.manong.hylian.server.util.Validator;

import javax.annotation.Resource;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * 角色服务实现
 *
 * @author frankcl
 * @date 2023-10-13 15:19:20
 */
@Service
public class RoleServiceImpl implements RoleService {

    private static final Logger logger = LoggerFactory.getLogger(RoleServiceImpl.class);

    @Resource
    protected RoleMapper roleMapper;

    @Override
    public Role get(String id) {
        if (StringUtils.isEmpty(id)) {
            logger.error("role id is empty for getting");
            throw new BadRequestException("角色ID为空");
        }
        return roleMapper.selectById(id);
    }

    @Override
    public List<Role> batchGet(List<String> ids) {
        if (ids == null || ids.isEmpty()) return new ArrayList<>();
        List<Role> roles = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch countDownLatch = new CountDownLatch(ids.size());
        ids.stream().parallel().forEach(id -> {
            try {
                Role role = roleMapper.selectById(id);
                if (role != null) roles.add(role);
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
        return new ArrayList<>(roles);
    }

    @Override
    public boolean add(Role role) {
        LambdaQueryWrapper<Role> query = new LambdaQueryWrapper<>();
        query.eq(Role::getAppId, role.appId).eq(Role::getName, role.name);
        if (roleMapper.selectCount(query) > 0) {
            logger.error("role has existed for the same name[{}]", role.name);
            throw new IllegalStateException("角色存在");
        }
        return roleMapper.insert(role) > 0;
    }

    @Override
    public boolean update(Role role) {
        if (roleMapper.selectById(role.id) == null) {
            logger.error("role is not found for id[{}]", role.id);
            throw new NotFoundException("角色不存在");
        }
        return roleMapper.updateById(role) > 0;
    }

    @Override
    public boolean delete(String id) {
        if (StringUtils.isEmpty(id)) {
            logger.error("role id is empty for deleting");
            throw new BadRequestException("角色ID为空");
        }
        return roleMapper.deleteById(id) > 0;
    }

    @Override
    public Pager<Role> search(RoleSearchRequest searchRequest) {
        if (searchRequest == null) searchRequest = new RoleSearchRequest();
        if (searchRequest.current == null || searchRequest.current < 1) searchRequest.current = Constants.DEFAULT_CURRENT;
        if (searchRequest.size == null || searchRequest.size <= 0) searchRequest.size = Constants.DEFAULT_PAGE_SIZE;
        Validator.validateOrderBy(Role.class, searchRequest);
        QueryWrapper<Role> query = new QueryWrapper<>();
        searchRequest.prepareOrderBy(query);
        if (!StringUtils.isEmpty(searchRequest.name)) query.like("name", searchRequest.name);
        if (!StringUtils.isEmpty(searchRequest.appId)) query.eq("app_id", searchRequest.appId);
        IPage<Role> page = roleMapper.selectPage(new Page<>(searchRequest.current, searchRequest.size), query);
        return Converter.convert(page);
    }
}
