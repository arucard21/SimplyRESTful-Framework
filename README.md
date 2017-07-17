# SimplyRESTful-jetty
[![License: LGPL v3](https://img.shields.io/badge/License-LGPL%20v3-blue.svg?style=plastic)](https://www.gnu.org/licenses/lgpl-3.0)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.arucard21.simplyrestful/simplyrestful-jetty/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/com.github.arucard21.simplyrestful/simplyrestful-jetty)

A quick way to deploy your SimplyRESTful API on Jetty

Provides the configuration required to deploy a SimplyRESTful API on a Jetty server with Apache CXF. While this gets your API working quickly, for better security and stability you may want to customize your configuration or use one of the other [deployment methods documented by Apache CXF](https://cwiki.apache.org/confluence/display/CXF20DOC/JAX-RS+Deployment).

The configuration includes generation of an OpenAPI Specification file at `/swagger.json`. It enables Swagger-UI as well at `/api-docs`. You can access Swagger-UI with `/api-docs?url=../swagger.json` so it automatically loads the generated OpenAPI Specification file.

## Usage
To use it, in your project you have to: 
* Depend on SimplyRESTful-jetty
* Depend on [SimplyRESTful](https://github.com/arucard21/SimplyRESTful)
    * This is optional since you can serve any JAX-RS endpoint on Jetty with this library. Of course, it works best with a SimplyRESTful-based endpoint.
* [Implement your SimplyRESTful API](https://github.com/arucard21/SimplyRESTful#usage)
* Create a class with a main method that calls `APIServer.run(JAXRSEndpoint.class)` where `JAXRSEndpoint.class` is the endpoint you implemented (which extends ApiEndpointBase). This will deploy your API endpoint on `http://localhost:9000/`.

See the [SimplyRESTful-example](https://github.com/arucard21/SimplyRESTful-example) project for a very simple example of a SimplyRESTful API implementation. It is also a good template project for creating a new API. 
