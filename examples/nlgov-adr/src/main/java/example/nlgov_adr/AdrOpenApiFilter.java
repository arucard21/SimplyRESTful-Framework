package example.nlgov_adr;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.swagger.v3.core.model.ApiDescription;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import simplyrestful.api.framework.swagger.SimplyRESTfulOpenApiFilter;

public class AdrOpenApiFilter extends SimplyRESTfulOpenApiFilter {
	/**
	 * Add the API-Version HTTP header to every response, as required by the NLGov ADR.
	 */
	@Override
    public Optional<ApiResponse> filterResponse(ApiResponse response, Operation operation, ApiDescription api, Map<String, List<String>> params, Map<String, String> cookies, Map<String, List<String>> headers) {
		response.addHeaderObject("API-Version", new Header()
				.description("Contains the full version number of the entire API since the URL contains only the major version number")
				.schema(new Schema<String>().type("string").example("1.0.0")));
        return super.filterResponse(response, operation, api, params, cookies, headers);
    }
}
