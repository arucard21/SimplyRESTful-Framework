# SimplyRESTful-Framework
A framework for creating a RESTful API, along with some convenience libraries.

There are 2 main components in the framework.
1.  [`SimplyRESTful`](/SimplyRESTful): The main server-side component.
    * It contains a default web resource implementation (sometimes called an endpoint) that maps the more complicated [HTTP](https://tools.ietf.org/html/rfc7231)-compliant access to simpler [CRUDL](https://en.wikipedia.org/wiki/Create,_read,_update_and_delete) functions.
    * The implementation is based on [JAX-RS](https://jakarta.ee/specifications/restful-ws/) and can be used with any JAX-RS framework.
1. [`client`](/client): A client-side component (for convenience).
    * This Java-based client provides programmatic access to any SimplyRESTful-based API. It only requires access to the Java classes of the API resources which can be added at runtime.

The framework also contains convenience libraries to configure a JAX-RS framework on a specific server. This does not cover all possible combinations of JAX-RS frameworks and servers but should cover some typical use cases.
* [`deploy-jetty-cxf`](/deploy-jetty-cxf): Convenience library for starting the SimplyRESTful API on a Jetty server with Apache CXF.
* [`deploy-springboot-cxf`](/deploy-springboot-cxf): Convenience library for starting the SimplyRESTful API on a Tomcat server with Apache CXF using Spring Boot.
* [`deploy-springboot-jersey`](/deploy-springboot-jersey): Convenience library for starting the SimplyRESTful API on a Tomcat server with Jersey using Spring Boot.

## Usage
The documentation on how to use this framework is described extensively in the README of each component. Roughly, you should:
1. Add a dependency to one of the convenience deploy libraries.
1. Implement the API according to the instructions for the [main component](/SimplyRESTful).
1. Start your API server.

There are [examples for the different deploy libraries](/examples/) available for more concrete information about how to implement this.
