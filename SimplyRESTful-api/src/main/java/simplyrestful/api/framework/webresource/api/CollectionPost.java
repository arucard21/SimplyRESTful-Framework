package simplyrestful.api.framework.webresource.api;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.POST;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import simplyrestful.api.framework.resources.HALResource;

public interface CollectionPost<T extends HALResource> {
    /**
     * Create a resource.
     *
     * @param resource is a resource that should be created.
     * @return a "201 Created" response for the resource that was created,
     *         containing its URI identifier in the Location header, if the resource
     *         was correctly created. A "409 Conflict" response is returned if the
     *         resource contains an self-link that refers to an existing resource.
     */
    @POST
    Response postHALResource(
            @Context
            ResourceInfo resourceInfo,
            @Context
            UriInfo uriInfo,
            @NotNull
            @Valid
            T resource);
}
