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

@Named
@Path("")
@Hidden
public class WebResourceRoot{
	@Context
	private UriInfo uriInfo;

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
