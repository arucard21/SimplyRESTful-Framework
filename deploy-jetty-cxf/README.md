# deploy-jetty-cxf
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg?style=plastic)](https://opensource.org/licenses/Apache-2.0)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.arucard21.simplyrestful/deploy-jetty-cxf/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/com.github.arucard21.simplyrestful/deploy-jetty-cxf)

A quick way to deploy your SimplyRESTful API using Apache CXF

Provides the configuration required to deploy a SimplyRESTful API on a Jetty server using Apache CXF. This should provide a quick and easy way to get your API working, but it isn't likely to be suited for production environments. If you wish to use Jetty in your production environment, you can copy the code from this library and customize it as needed.

The configuration makes all JAX-RS resources available under the root (e.g. `https://hostname/apiresource`)The configuration includes generation of an OpenAPI Specification file at `/swagger.json`. It enables Swagger-UI as well at `/api-docs`. You can access Swagger-UI with `/api-docs?url=../swagger.json` so it automatically loads the generated OpenAPI Specification file.

## Usage
To use it, in your project you have to:
* Depend on SimplyRESTful and SimplyRESTful-cxf
* [Implement your SimplyRESTful API](/SimplyRESTful#usage)
* Star the server in your code with a call like `Server myServer = new APIServer(JAXRSWebResource.class)` where `JAXRSWebResource.class` is the JAX-RS Web Resource you implemented (which extends WebResourceBase). This will deploy your Web Resource on `http://localhost:9000/`, though this address can be customized. You can stop the running server with `myServer.destroy()`.

See the [SimplyRESTful-example](/examples/jetty-cxf) project for a simple example of this deployment method.