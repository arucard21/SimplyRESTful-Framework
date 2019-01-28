# SimplyRESTful Client
[![License: LGPL v3](https://img.shields.io/badge/License-LGPL%20v3-blue.svg?style=plastic)](https://www.gnu.org/licenses/lgpl-3.0)

A generic client for accessing APIs created with the SimplyRESTful suite of libraries and frameworks.

## Usage
Note: not yet available

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
