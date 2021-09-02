# Fields Filter JSON Servlet
[![License: LGPL v3](https://img.shields.io/badge/License-LGPL%20v3-blue.svg?style=plastic)](https://www.gnu.org/licenses/lgpl-3.0)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.arucard21.simplyrestful/fields-filter-json-servlet/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/com.github.arucard21.simplyrestful/fields-filter-json-servlet)

A Servlet Filter that can filter a JSON response based on fields provided in a query parameter.

The Servlet filter can be registered through resource scanning, simply by adding this library as dependency. The filter is configured for all requests but it only applies the filter if the response contains a JSON-compatible media type and a `fields` query parameter is provided.
