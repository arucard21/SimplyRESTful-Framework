# JAX-RS Providers
[![License: LGPL v3](https://img.shields.io/badge/License-LGPL%20v3-blue.svg?style=plastic)](https://www.gnu.org/licenses/lgpl-3.0)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.arucard21.simplyrestful/SimplyRESTful-resources/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/com.github.arucard21.simplyrestful/SimplyRESTful-resources)

Contains some useful JAX-RS Providers that can be used both server-side and client-side. 
It currently only contains:
* HALMapperProvider: Provides a HALMapper (configured for use with SimplyRESTful) as ObjectMapper to JacksonJsonProvider when a HAL+JSON media type is used.
* ObjectMapperProvider: Provides an ObjectMapper (configured for use with SimplyRESTful) for use by JacksonJsonProvider when any other media type is used.