package example.microprofile.openliberty;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 *
 */
@Path("/hello")
public class HelloController {
    @GET
    @Produces("text/plain")
    public String sayHello() {
        return "Hello World";
    }
}
