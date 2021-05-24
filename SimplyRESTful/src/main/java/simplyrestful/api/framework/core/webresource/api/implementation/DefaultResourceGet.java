package simplyrestful.api.framework.core.webresource.api.implementation;

import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import simplyrestful.api.framework.core.MediaTypeUtils;
import simplyrestful.api.framework.core.WebResourceUtils;
import simplyrestful.api.framework.core.api.crud.DefaultRead;
import simplyrestful.api.framework.resources.HALResource;
import simplyrestful.api.framework.webresource.api.ResourceGet;

public interface DefaultResourceGet<T extends HALResource> extends ResourceGet<T>, DefaultRead<T> {
    /**
     * Retrieve a resource.
     *
     * @param id is the UUID part from the entire URI identifier of the resource.
     * @return the requested resource.
     */
    @Operation(description = "Retrieve a single resource")
    default T getHALResource(
	    ResourceInfo resourceInfo,
	    UriInfo uriInfo,
	    HttpHeaders httpHeaders,
	    @Parameter(description = "The identifier for the resource", required = true)
	    UUID id) {
        T resource = Optional.ofNullable(this.read(id)).orElseThrow(NotFoundException::new);
        MediaType selected = MediaTypeUtils.selectMediaType(resourceInfo, httpHeaders);
        if (MediaTypeUtils.APPLICATION_HAL_JSON_TYPE.isCompatible(selected)) {
            resource.setSelf(
                    WebResourceUtils.createLink(
                            WebResourceUtils.getAbsoluteWebResourceURI(resourceInfo, uriInfo, id),
                            MediaTypeUtils.APPLICATION_HAL_JSON,
                            resource.getProfile()));
        } else {
            resource.setSelf(
                    WebResourceUtils.createLink(
                            WebResourceUtils.getAbsoluteWebResourceURI(resourceInfo, uriInfo, id),
                            resource.getCustomJsonMediaType().toString(),
                            null));
        }
        return resource;
    }
}
