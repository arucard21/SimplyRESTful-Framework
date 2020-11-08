package simplyrestful.api.framework.core.filters;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.MediaType;

import jakarta.inject.Named;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import simplyrestful.api.framework.core.DefaultWebResource;

@Named
@WebFilter("*")
public class JsonFieldsFilter extends HttpFilter {
    private static final long serialVersionUID = 6825636135376615562L;
    private static final String MEDIA_TYPE_STRUCTURE_SUFFIX_JSON = "+json";
    private static final String FIELDS_VALUE_ALL = "all";

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
	if(!isJsonCompatibleMediaType(response.getContentType())) {
	    return;
	}
	List<String> fields = Arrays.asList(request.getParameterValues(DefaultWebResource.QUERY_PARAM_FIELDS));
	if (!fields.contains(FIELDS_VALUE_ALL) && !fields.isEmpty()) {
	    response.getWriter().write(keepOnlyRequestedFields(request.getInputStream(), fields));
	}
        super.doFilter(request, response, chain);
    }

    private boolean isJsonCompatibleMediaType(String contentType) {
	if(contentType != null &&
		(
			MediaType.valueOf(contentType).isCompatible(MediaType.APPLICATION_JSON_TYPE) ||
			MediaType.valueOf(contentType).getSubtype().endsWith(MEDIA_TYPE_STRUCTURE_SUFFIX_JSON))) {
	    return true;
	}
	return false;
    }

    private String keepOnlyRequestedFields(InputStream requestInputStream, List<String> fields) throws IOException {
	throw new ServerErrorException("This API does not yet filter fields", 501);
    }
}
