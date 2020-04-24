package example.springboot;

import org.apache.cxf.jaxrs.ext.search.SearchContextProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import example.datastore.DataStore;

/**
 * Run the API server with the example endpoint and resource.
 *
 * @author RiaasM
 *
 */
@SpringBootApplication
public class ExampleCXFApplication {
    public static void main(String[] args) {
	SpringApplication.run(ExampleCXFApplication.class, args);
    }
    
    @Bean
    public SearchContextProvider searchContextProvider() {
	return new SearchContextProvider();
    }
    
    @Bean
    public DataStore datastore() {
    	return new DataStore();
    }
}
