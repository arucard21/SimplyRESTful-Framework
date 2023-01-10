package simplyrestful.springboot.configuration.cxf;

import org.apache.coyote.http2.Http2Protocol;
import org.apache.cxf.jaxrs.openapi.OpenApiFeature;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;

import simplyrestful.api.framework.providers.ObjectMapperProvider;

@AutoConfiguration
@ComponentScan(basePackages = { "simplyrestful" })
public class CXFSpringBootConfiguration { /* Ensure that the simplyrestful package is scanned */
    @Bean
    @ConditionalOnMissingBean
    public JacksonJsonProvider jacksonJsonProvider() {
        return new JacksonJsonProvider();
    }

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapperProvider objectMapperProvider() {
        return new ObjectMapperProvider();
    }

    @Bean
    @ConditionalOnMissingBean
    public OpenApiFeature swaggerFeature() {
        return new OpenApiFeature();
    }

    @Bean
    @ConditionalOnMissingBean
    public TomcatConnectorCustomizer http2UpgradeProtocol() {
        return (connector -> connector.addUpgradeProtocol(new Http2Protocol()));
    }
}
