# JAX-RS Providers
[![License: LGPL v3](https://img.shields.io/badge/License-LGPL%20v3-blue.svg?style=plastic)](https://www.gnu.org/licenses/lgpl-3.0)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.arucard21.simplyrestful/SimplyRESTful-resources/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/com.github.arucard21.simplyrestful/SimplyRESTful-resources)

Contains some useful JAX-RS Providers that can be used both server-side and client-side. 
It currently contains:
* `AcceptHeaderModifier`: Modifies the Accept HTTP header to ensure that if `application/json` is requested, any media type with
 * the `+json` structured suffix would also be considered acceptable. 
* `JsonFieldsFilterInterceptor`: Filters the keys of any JSON-based response body based on the fields provided in a `fields` query parameter. This uses the `fields-filter-json` library to do the actual filtering. You can provide multiple fields by separating them by comma's and you can specify nested fields by separating them with dots, e.g. `fields=field1,field2,field3.nestedField.moreNestedField`.
* `UriCustomizer`: Allows you to provide a custom URI as an HTTP header to override the auto-detected URI for the API. This requires the API to set the environment variable `SIMPLYRESTFUL_URI_HTTP_HEADER` with a value that matches the HTTP header containing the URI that should be used by the API, e.g. `X-ORIGINAL-URL`.
* `ObjectMapperProvider`: Provides an `ObjectMapper` configured for use with SimplyRESTful. It would automatically be used by `JacksonJsonProvider`, if both are registered with JAX-RS.
* `MediaTypeModule`: a Jackson module to serialize and deserialize a `jakarta.ws.rs.core.MediaType` object to and from a simple String representation. This can be registered automatically when using `findModules()` on the ObjectMapper (which is already done in `ObjectMapperProvider`).

This library also contains some classes that provide convenience for common functionality related to JAX-RS.
* `MediaTypeUtils`: Provides convenience for working with media type quality (q and qs) parameters. It also provides methods for detecting the media types that the API can produce.
* `QueryParamUtils`: Provides convenience for the fields and sort query parameters.
* `WebResourceUtils`: Provides convenience for detecting the absolute URI used to access the API. It also provides convenience to reliably parse a UUID from the last path segment of the absolute URI for a specific JAX-RS Web Resource.