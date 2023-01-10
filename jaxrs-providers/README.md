# JAX-RS Providers
[![License: LGPL v3](https://img.shields.io/badge/License-LGPL%20v3-blue.svg?style=plastic)](https://www.gnu.org/licenses/lgpl-3.0)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.arucard21.simplyrestful/SimplyRESTful-resources/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/com.github.arucard21.simplyrestful/SimplyRESTful-resources)

Contains some useful JAX-RS Providers that can be used both server-side and client-side. 
It currently only contains:
* `ObjectMapperProvider`: Provides an `ObjectMapper` configured for use with SimplyRESTful. It would automatically be used by `JacksonJsonProvider`, if both are registered with JAX-RS.
* `MediaTypeModule`: a Jackson module to serialize and deserialize a `jakarta.ws.rs.core.MediaType` object to and from a simple String representation. This can be registered automatically when using `findModules()` on the ObjectMapper (which is already done in `ObjectMapperProvider`).