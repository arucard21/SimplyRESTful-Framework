# Fields Filter JSON Servlet
[![License: LGPL v3](https://img.shields.io/badge/License-LGPL%20v3-blue.svg?style=plastic)](https://www.gnu.org/licenses/lgpl-3.0)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.arucard21.simplyrestful/fields-filter-json-servlet/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/com.github.arucard21.simplyrestful/fields-filter-json-servlet)

A Servlet Filter that can filter a JSON response based on fields provided in a query parameter.

The filter can be registered directly, e.g. in a `web.xml` file, or through resource scanning. By default, the filter is configured to apply to all requests though inside the filter itself, it checks whether the response contains a JSON-compatible media type and the `fields` query parameter is provided before continuing.

For registering in Spring boot, you can define a `FilterRegistrationBean<JsonFieldsServletFilter>` bean in order to register the filter. This bean can, among other things, be used to override the default URL pattern, if you want to restrict the filter to specific paths.
