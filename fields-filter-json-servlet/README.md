# Fields Filter JSON Servlet
[![License: LGPL v3](https://img.shields.io/badge/License-LGPL%20v3-blue.svg?style=plastic)](https://www.gnu.org/licenses/lgpl-3.0)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.arucard21.simplyrestful/fields-filter-json-servlet/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/com.github.arucard21.simplyrestful/fields-filter-json-servlet)

A Servlet Filter that can filter a JSON response based on fields provided in a query parameter.

The Servlet filter can be registered directly or through resource scanning. In the latter case, the filter is configured to apply to all requests though it only applies the filter if the response contains a JSON-compatible media type and the `fields` query parameter is provided.

This is still experimental. The underlying filter (fields-filter-json) still requires more testing to ensure its reliability.
