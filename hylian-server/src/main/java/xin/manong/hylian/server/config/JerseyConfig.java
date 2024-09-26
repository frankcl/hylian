package xin.manong.hylian.server.config;

import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJsonProvider;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;
import xin.manong.weapon.spring.web.ws.filter.WebResponseFilter;
import xin.manong.weapon.spring.web.ws.handler.ExceptionHandler;

/**
 * Jersey配置
 *
 * @author frankcl
 * @date 2022-08-24 13:00:20
 */
@Component
public class JerseyConfig extends ResourceConfig {

    public JerseyConfig() {
        packages("xin.manong.hylian.server");
        register(JacksonJsonProvider.class);
        register(WebResponseFilter.class);
        register(ExceptionHandler.class);
        register(MultiPartFeature.class);
    }
}
