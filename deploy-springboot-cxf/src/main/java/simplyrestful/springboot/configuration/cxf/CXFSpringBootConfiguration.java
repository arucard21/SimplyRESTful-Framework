package simplyrestful.springboot.configuration.cxf;

import org.apache.coyote.http2.Http2Protocol;
import org.apache.cxf.jaxrs.openapi.OpenApiFeature;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.context.annotation.Bean;
import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;

import simplyrestful.api.framework.providers.ObjectMapperProvider;

@AutoConfiguration
public class CXFSpringBootConfiguration {
    @Bean
    @ConditionalOnMissingBean
    JacksonJsonProvider jacksonJsonProvider() {
        return new JacksonJsonProvider();
    }

    @Bean
    @ConditionalOnMissingBean
    ObjectMapperProvider objectMapperProvider() {
        return new ObjectMapperProvider();
    }

    @Bean
    OpenApiFeature openApiFeature() {
        return new OpenApiFeature();
    }

    @Bean
    @ConditionalOnMissingBean
    TomcatConnectorCustomizer http2UpgradeProtocol() {
        return (connector -> connector.addUpgradeProtocol(new Http2Protocol()));
    }
}
