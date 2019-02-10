# SimplyRESTful-Framework
A suite of libraries and frameworks for creating and deploying a RESTful API

Provides a simple and easy way to create a RESTful API using HAL+JSON as media type.

The basic premise is that SimplyRESTful will allow you to implement RESTful API's without requiring in-depth knowledge about REST. It does this by taking care of the REST-related parts of your API with common or best-practice implementations. This includes things like data formatting, collection paging, proper use of HTTP methods and versioning. Some of these can't be implemented (entirely) in the framework, in which case documentation and example code should provide guidance on how best to implement this.

This means you shouldn't have to think about how exactly a RESTful API should be implemented, as most of these details are already take care of. This leaves more time to focus on the design of your resources, which [should require significant effort](http://roy.gbiv.com/untangled/2008/rest-apis-must-be-hypertext-driven) when creating a RESTful API. As the name implies, using this suite of libraries and frameworks should allow you to create a RESTful API. When needed, SimplyRESTful tries to conform to the constraints of the REST architectural style though it focuses most heavily on the constraint of "Uniform Interface" as that is exactly what SimplyRESTful tries to provide.

This suite of libraries and frameworks provides a high degree of convenience and customizability. You can choose to use only the main framework, which can be used with any JAX-RS framework, implementing your own mechanisms to deploy the API and persist its data. But SimplyRESTful also provides libraries and frameworks that provide the code and configuration to use a specific JAX-RS framework, deployment method or mechanism for persistence. For example, you can choose to implement your SimplyRESTful API using the Jersey JAX-RS framework and persist the data using Spring Data Repositories. This use case is provided as [an example](/examples/springboot-jersey-nomapping-springdata) where the API resource is persisted as-is, without any mapping. Of course, convenience for persisting these resources after mapping them to backend entities will also provided (Note: this is not yet available).

## Usage
* Design your API resource
* Implement this API resource using the SimplyRESTful dependency that provides the desired level of convenience and customizability.
* Follow the instructions provided for that dependency and deploy your API

## Process for SimplyRESTful API clients
The process for clients to use a SimplyRESTful API adheres to the following [rule for RESTful APIs](http://roy.gbiv.com/untangled/2008/rest-apis-must-be-hypertext-driven):

> A REST API should be entered with no prior knowledge beyond the initial URI (bookmark) and set of standardized media types that are appropriate for the intended audience (i.e., expected to be understood by any client that might use the API).

The standardized media type here would refer mainly to the HAL-based resource that you designed, as identified by the profile URI. The documentation for this HAL-based resource should be available at the location indicated by the profile URI. Technically, the standardized media type refers to the combination of both the HAL+JSON media type and the resource's profile.

In order to do something useful, the client would need to know which resources it needs to access based on the documentation of the standardized media types. This is why the documentation should explain not just how to process the resources but also what they can be used for. Both of these should be provided in the documentation of the resource profile for SimplyRESTful APIs.

The process for a client is as follows:
* Perform a GET request on the base URI of the API (the initial URI known by the client)
    * This provides the client with a service document which contains at least 1 link with the link relation "describedby". This link refers to the OpenAPI Specification document that describes this API.
* Perform a GET request on the "describedby" link to retrieve the OpenAPI Specification (OAS) document. This may conform to version 2.x or 3.x of the OpenAPI Specification and should be parsed accordingly.
* The client can find the URI for the resource it needs from the [`paths`](https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.1.md#pathsObject) object in the OAS document. The client should match the media type of the resource, including its profile attribute (e.g. `application/hal+json; profile=https://arucard21.github.io/SimplyRESTful/HALCollection/v1/`), to the `200` key of the [Response Body Objects](https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.1.md#responsesObject) for the `get` [operation](https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.1.md#operationObject) on a path.
    * This specific path location can be used since a SimplyRESTful API will always use the same media type for all operations, and will likely always include a GET operation. Of course, if GET is not available, another operation should be used for which either the request or response object may need to be inspected.
* Using the `path` object's key as URI, the client can now access the API for the resource it needs to use. Additional functionality for this resource should be described in the documentation and should therefore already be known to the client. This may include information on how paging is done for this resource or how fields can be filtered on the resource.

Using this process, the client should be able to use the SimplyRESTful API fully without needing to know more than the initial URI and the set of standardized media types.
