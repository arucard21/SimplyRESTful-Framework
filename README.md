# SimplyRESTful-Framework
A framework for creating a RESTful API, along with some convenience libraries. 

* [`SimplyRESTful`](/SimplyRESTful): The core framework. 
    * It provides a default web resource (aka endpoint) implementation that adheres to the [HTTP 1.1 specification](https://tools.ietf.org/html/rfc7231). 
    * It can be used for any JAX-RS API on any JAX-RS framework.
* [`SimplyRESTful-resources`](/SimplyRESTful-resources): Contains the basic resources used by the framework and libraries
    * This will likely never need to be used directly. It is only needed by the framework and libraries. 
* [`deploy-jetty-cxf`](/deploy-jetty-cxf): Convenience library for starting the SimplyRESTful API on a Jetty server with Apache CXF.
* [`deploy-springboot-cxf`](/deploy-springboot-cxf): Convenience library for starting the SimplyRESTful API on a Tomcat server with Apache CXF using Spring Boot.
* [`deploy-springboot-jersey`](/deploy-springboot-jersey): Convenience library for starting the SimplyRESTful API on a Tomcat server with Jersey using Spring Boot.
* [`persist-springdata`](/persist-springdata): Convenience library for creating a SimplyRESTful API that stores its API resources directly in a database using Spring Data.
* [`client`](/client): Convenience library for easily accessing any SimplyRESTful API.
    * This library only requires access to the Java classes of the API resources in a SimplyRESTful-based API in order to provide programmatic access to it.
