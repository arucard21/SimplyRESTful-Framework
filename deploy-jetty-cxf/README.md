# deploy-jetty-cxf
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg?style=plastic)](https://opensource.org/licenses/Apache-2.0)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.arucard21.simplyrestful/deploy-jetty-cxf/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/com.github.arucard21.simplyrestful/deploy-jetty-cxf)

Convenience library for starting the SimplyRESTful API on a Jetty server with Apache CXF.

What this library does:
* Registers the JAX-RS providers required by SimplyRESTful.
* Provides convenience to register the JAX-RS Web Resources.
* Makes the registered JAX-RS Web Resources available directly under the root of the path.
* Generates an OpenAPI Specification document at `/swagger.json`
* Provides a Swagger UI at `/api-docs`.
    * Tip: If you access Swagger UI with `/api-docs?url=../swagger.json` it automatically loads the generated OpenAPI Specification file of the API.

## Usage
To use it in your project you have to:
* Depend on [`deploy-jetty-cxf`](https://search.maven.org/artifact/com.github.arucard21.simplyrestful/deploy-jetty-cxf/)
* Implement your SimplyRESTful API according to the [standard instructions](/SimplyRESTful#usage).
    * You do not need to depend on the SimplyRESTful library directly as it is already pulled in by this library.
    * You do not have to follow the steps to configure your JAX-RS framework since this library takes care of most of that automatically.
* Build the Jetty `Server` object using the [`ServerBuilder`](/deploy-jetty-cxf/src/main/java/simplyrestful/jetty/deploy/ServerBuilder.java).
    * Use `withWebResource()` to register your JAX-RS Web Resources in Apache CXF.
    * You can also provide the address where the server should be hosted. (default: `http://localhost:9000`).
* Use the `Server` object to start your SimplyRESTful API.

See the [example project](/examples/jetty-cxf) for a simple example of how this library can be used.
