package simplyrestful.springboot.configuration.cxf;

import org.apache.coyote.http2.Http2Protocol;
import org.apache.cxf.jaxrs.openapi.OpenApiFeature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import simplyrestful.api.framework.providers.JacksonHALJsonProvider;

@Configuration
@ComponentScan(basePackages = { "simplyrestful" })
public class CXFSpringBootConfiguration { /* Ensure that the simplyrestful package is scanned */
    @Bean
    @ConditionalOnMissingBean
    public JacksonJsonProvider jacksonJsonProvider() {
        return new JacksonJsonProvider();
    }

    @Bean
    @ConditionalOnMissingBean
    public JacksonHALJsonProvider jacksonHALJsonProvider() {
        return new JacksonHALJsonProvider();
    }

    @Bean
    @ConditionalOnMissingBean
    public OpenApiFeature swaggerFeature() {
        return new OpenApiFeature();
    }

    @Bean
    public TomcatConnectorCustomizer http2UpgradeProtocol() {
        return (connector -> connector.addUpgradeProtocol(new Http2Protocol()));
    }
}
