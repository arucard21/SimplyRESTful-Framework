package simplyrestful.springboot.configuration.fieldsfilter;

import org.springframework.boot.autoconfigure.web.servlet.ConditionalOnMissingFilterBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import simplyrestful.api.framework.filters.JsonFieldsServletFilter;

@Configuration
public class JsonFieldsServletFilterConfiguration {
    @Bean
    @ConditionalOnMissingFilterBean
    public JsonFieldsServletFilter jsonFieldsFilter() {
        return new JsonFieldsServletFilter();
    }
}
