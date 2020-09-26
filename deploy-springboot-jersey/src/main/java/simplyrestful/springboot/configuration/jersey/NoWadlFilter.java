package simplyrestful.springboot.configuration.jersey;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.Path;

import org.glassfish.jersey.server.wadl.internal.WadlResource;

import io.swagger.v3.core.model.ApiDescription;
import io.swagger.v3.oas.models.Operation;
import simplyrestful.api.framework.core.swagger.SimplyRESTfulOpenApiFilter;

public class NoWadlFilter extends SimplyRESTfulOpenApiFilter{
    private static final String PATH_ROOT_RELATIVE_START = "/";

    /**
     * Remove the documentation for the WADL feature (which is still included even when it is disabled in Jersey).
     */
    @Override
    public Optional<Operation> filterOperation(
	    Operation operation,
	    ApiDescription api,
	    Map<String, List<String>> params,
	    Map<String, String> cookies,
	    Map<String, List<String>> headers) {
	Path[] paths = WadlResource.class.getAnnotationsByType(Path.class);
	if(paths.length == 1) {
	    String path = paths[0].value();
	    String rootRelativePath = (path.startsWith(PATH_ROOT_RELATIVE_START)) ? path : PATH_ROOT_RELATIVE_START + path;
	    if (api.getPath().startsWith(rootRelativePath)){
		return Optional.empty();		
	    }
	}
	return super.filterOperation(operation, api, params, cookies, headers);
    }
}
