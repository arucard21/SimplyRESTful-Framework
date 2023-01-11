package simplyrestful.springboot.configuration.fieldsfilter;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.ConditionalOnMissingFilterBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.filter.OrderedFilter;
import org.springframework.context.annotation.Bean;
import simplyrestful.api.framework.servlet.filters.JsonFieldsServletFilter;

@AutoConfiguration
public class JsonFieldsServletFilterConfiguration {
    @Bean
    @ConditionalOnMissingFilterBean
    FilterRegistrationBean<JsonFieldsServletFilter> jsonFieldsFilter() {
        FilterRegistrationBean<JsonFieldsServletFilter> jsonFieldsRegistration = new FilterRegistrationBean<JsonFieldsServletFilter>(new JsonFieldsServletFilter());
        jsonFieldsRegistration.setOrder(OrderedFilter.REQUEST_WRAPPER_FILTER_MAX_ORDER - 1);
        return jsonFieldsRegistration;
    }
}
