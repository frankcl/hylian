package xin.manong.hylian.server.config;

import lombok.Data;

/**
 * JWT配置信息
 *
 * @author frankcl
 * @date 2023-08-31 19:18:12
 */
@Data
public class JWTConfig {

    public String secretHS256;
}
