# Fields Filter JSON Servlet
[![License: LGPL v3](https://img.shields.io/badge/License-LGPL%20v3-blue.svg?style=plastic)](https://www.gnu.org/licenses/lgpl-3.0)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.arucard21.simplyrestful/fields-filter-json-servlet/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/com.github.arucard21.simplyrestful/fields-filter-json-servlet)

DEPRECATED: Use the JAX-RS WriterInterceptor `JsonFieldsFilterInterceptor` instead. 

A Servlet Filter that can filter a JSON response based on fields provided in a query parameter.

The filter can be registered directly, e.g. in a `web.xml` file, or through resource scanning. By default, the filter is configured to apply to all requests though inside the filter itself, it checks whether the response contains a JSON-compatible media type and the `fields` query parameter is provided before continuing.

For registering the filter in in Spring boot, this library provides auto-configuration. So you only have to add this library as a dependency to your Spring Boot application.
