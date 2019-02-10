package simplyrestful.springboot.configuration.cxf;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages= {"simplyrestful"})
public class CXFSpringBootConfiguration{ /* Ensure that the simplyrestful package is scanned */ }
