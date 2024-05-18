package simplyrestful.springboot.configuration.jersey;

import org.apache.coyote.http2.Http2Protocol;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jersey.ResourceConfigCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;

import io.swagger.v3.jaxrs2.integration.resources.AcceptHeaderOpenApiResource;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import simplyrestful.api.framework.filters.AcceptHeaderModifier;
import simplyrestful.api.framework.filters.JsonFieldsFilterInterceptor;
import simplyrestful.api.framework.filters.UriCustomizer;
import simplyrestful.api.framework.providers.ObjectMapperProvider;
import simplyrestful.api.framework.servicedocument.WebResourceRoot;

@AutoConfiguration
public class JerseySpringBootConfiguration implements ResourceConfigCustomizer {
    @Override
    public void customize(ResourceConfig config) {
        config.register(WebResourceRoot.class);
        config.register(JacksonJsonProvider.class);
        config.register(ObjectMapperProvider.class);
        config.register(UriCustomizer.class);
        config.register(JsonFieldsFilterInterceptor.class);
        config.register(AcceptHeaderModifier.class);
        config.register(OpenApiResource.class);
        config.register(AcceptHeaderOpenApiResource.class);
        config.property(ServerProperties.WADL_FEATURE_DISABLE, true);
        config.property(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true);
    }

    @Bean
    @ConditionalOnMissingBean
    TomcatConnectorCustomizer http2UpgradeProtocol() {
        return (connector -> connector.addUpgradeProtocol(new Http2Protocol()));
    }
}
