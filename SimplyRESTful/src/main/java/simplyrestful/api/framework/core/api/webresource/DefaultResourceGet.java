package simplyrestful.api.framework.core.api.webresource;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import simplyrestful.api.framework.core.MediaTypeUtils;
import simplyrestful.api.framework.core.api.crud.DefaultRead;
import simplyrestful.api.framework.resources.HALResource;

public interface DefaultResourceGet<T extends HALResource> extends WebResourceBase<T>, DefaultRead<T> {
    /**
     * Retrieve a resource.
     *
     * @param id is the UUID part from the entire URI identifier of the resource.
     * @return the requested resource.
     */
    @Path("/{id}")
    @GET
    @Operation(description = "Retrieve a single resource")
    @Consumes
    default T getHALResource(
	    @Context Configuration configuration,
	    @Context UriInfo uriInfo,
	    @Context HttpHeaders httpHeaders,
	    @Parameter(description = "The identifier for the resource", required = true) @PathParam("id") @NotNull UUID id) {
	T resource = Optional.ofNullable(this.read(id)).orElseThrow(NotFoundException::new);

	List<MediaType> producibleMediaTypesWithQSValues = MediaTypeUtils.getProducibleMediaTypes(configuration).stream()
		.filter(producibleMediaType ->
			(MediaTypeUtils.withoutQualityParameters(producibleMediaType).equals(new MediaType(
				MediaTypeUtils.TYPE_APPLICATION,
				MediaTypeUtils.APPLICATION_HAL_JSON_SUBTYPE,
				Map.of(MediaTypeUtils.APPLICATION_HAL_JSON_PARAMETER_PROFILE, resource.getProfile().toString()))) ||
			MediaTypeUtils.withoutQualityParameters(producibleMediaType).equals(resource.getCustomJsonMediaType())))
		.collect(Collectors.toList());
	MediaType selected = MediaTypeUtils.selectMediaType(producibleMediaTypesWithQSValues, httpHeaders.getAcceptableMediaTypes());

	if(MediaTypeUtils.APPLICATION_HAL_JSON_TYPE.isCompatible(selected)) {
	    resource.setSelf(createLink(
		    getAbsoluteWebResourceURI(uriInfo, id),
		    MediaTypeUtils.APPLICATION_HAL_JSON,
		    resource.getProfile()));
	}
	else {
	    resource.setSelf(createLink(
		    getAbsoluteWebResourceURI(uriInfo, id),
		    resource.getCustomJsonMediaType().toString(),
		    null));
	}
	return resource;
    }
}
