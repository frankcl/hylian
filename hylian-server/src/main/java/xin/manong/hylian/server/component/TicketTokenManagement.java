package xin.manong.hylian.server.component;

import jakarta.annotation.Resource;
import jakarta.ws.rs.NotAuthorizedException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import xin.manong.hylian.server.common.Constants;
import xin.manong.hylian.server.model.UserProfile;
import xin.manong.hylian.server.service.JWTService;
import xin.manong.hylian.server.service.TicketService;
import xin.manong.hylian.server.service.TokenService;

import java.util.Set;

/**
 * Ticket Token管理
 *
 * @author frankcl
 * @date 2026-04-08 10:56:01
 */
@Component
public class TicketTokenManagement {

    private static final Logger logger = LoggerFactory.getLogger(TicketTokenManagement.class);

    @Resource
    private JWTService jwtService;
    @Resource
    private TokenService tokenService;
    @Resource
    private TicketService ticketService;

    /**
     * 验证令牌有效性
     *
     * @param token 令牌
     * @return 有效返回true，否则返回false
     */
    public boolean verifyToken(String token) {
        if (StringUtils.isEmpty(token)) {
            logger.error("Token is empty");
            return false;
        }
        if (!tokenService.verifyToken(token)) {
            logger.error("Verify token failed");
            removeTokens(token);
            return false;
        }
        return true;
    }

    /**
     * 验证ticket有效性
     *
     * @param ticket 票据
     */
    public void verifyTicket(String ticket) {
        if (StringUtils.isEmpty(ticket)) {
            logger.warn("Ticket is empty");
            throw new NotAuthorizedException("Ticket为空");
        }
        if (!ticketService.verifyTicket(ticket)) {
            logger.error("Verify ticket failed");
            removeTicketTokensByTicket(ticket);
            throw new NotAuthorizedException("验证Ticket失败");
        }
        UserProfile userProfile = jwtService.decodeProfile(ticket);
        if (userProfile == null) {
            logger.error("Decode profile failed from ticket");
            removeTicketTokensByTicket(ticket);
            throw new NotAuthorizedException("非法Ticket");
        }
        String cachedTicket = ticketService.getTicket(userProfile.id);
        if (StringUtils.isEmpty(cachedTicket) || !ticket.equals(cachedTicket)) {
            logger.error("Cached ticket and provided ticket are not consistent");
            removeTicketTokensByTicket(ticket);
            throw new NotAuthorizedException("Ticket缓存失效");
        }
    }

    /**
     * 构建票据
     *
     * @param userProfile 用户信息
     * @return 票据
     */
    public String buildTicket(UserProfile userProfile) {
        String ticket = ticketService.buildTicket(userProfile, Constants.COOKIE_TICKET_EXPIRED_TIME_MS);
        ticketService.putTicket(userProfile.id, ticket);
        return ticket;
    }

    /**
     * 根据ticket构建token
     *
     * @param ticket 票据
     * @return 令牌
     */
    public String buildToken(String ticket) {
        UserProfile userProfile = jwtService.decodeProfile(ticket);
        String token = tokenService.buildToken(userProfile, Constants.CACHE_TOKEN_EXPIRED_TIME_MS);
        tokenService.putToken(token, ticket);
        ticketService.addToken(userProfile.id, token);
        ticketService.putTicket(userProfile.id, ticket);
        return token;
    }

    /**
     * 根据token获取ticket
     *
     * @param token 令牌
     * @return 票据
     */
    public String getTicketByToken(String token) {
        return tokenService.getTicket(token);
    }

    /**
     * 移除用户所有ticket和token信息
     *
     * @param ticketId 票据ID
     */
    public void removeTicketTokensById(String ticketId) {
        Set<String> tokenIds = ticketService.getTokens(ticketId);
        for (String tokenId : tokenIds) tokenService.removeTokenWithId(tokenId);
        ticketService.removeTokens(ticketId);
        ticketService.removeTicket(ticketId);
    }

    /**
     * 移除ticket相关资源
     * 1. 移除ticket相关token
     * 2. 移除ticket
     *
     * @param ticket 票据
     */
    public void removeTicketTokensByTicket(String ticket) {
        UserProfile userProfile = jwtService.decodeProfile(ticket);
        if (userProfile == null) return;
        removeTicketTokensById(userProfile.id);
    }

    /**
     * 移除token信息
     *
     * @param token 令牌
     */
    public void removeTokens(String token) {
        tokenService.removeToken(token);
        UserProfile userProfile = jwtService.decodeProfile(token);
        if (userProfile == null) return;
        ticketService.removeToken(userProfile.id, token);
    }
}
