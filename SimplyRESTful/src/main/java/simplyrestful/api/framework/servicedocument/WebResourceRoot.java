package simplyrestful.api.framework.servicedocument;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.inject.Named;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import simplyrestful.api.framework.resources.ApiServiceDocument;
import simplyrestful.api.framework.resources.Link;

/**
 * A WebResource at the root of the API, which provides a service document that describes the API.
 *
 * The service document is intended as the living documentation for the API. It currently provides this
 * through the OpenAPI Specification document, which it links to. The reason for having a service
 * document, even when the OpenAPI Specification document already documents the API, is that it is
 * located at the root of the API. This is the main entry point to the API and should provide sufficient
 * guidance on how to use it.
 *
 * Unlike most WebResources, there is no collection nor any IDs for this API resource since there is only
 * one service document for the entire API.
 */
@Named
@Path("")
@Hidden
public class WebResourceRoot{
	@Context
	private UriInfo uriInfo;

	/**
	 * Retrieve the service document describing this API.
	 *
	 * @return the service document describing this API.
	 */
	@Produces(ApiServiceDocument.MEDIA_TYPE_JSON)
	@GET
	public ApiServiceDocument getServiceDocument() {
		ApiServiceDocument serviceDocument = new ApiServiceDocument();
		Link descriptionLink = new Link(uriInfo.getRequestUriBuilder().path("openapi.json").build(), MediaType.APPLICATION_JSON_TYPE);
		serviceDocument.setDescribedBy(descriptionLink);
		Link selfLink = new Link(uriInfo.getRequestUriBuilder().build(), serviceDocument.customJsonMediaType());
		serviceDocument.setSelf(selfLink);
		return serviceDocument;
	}
}
