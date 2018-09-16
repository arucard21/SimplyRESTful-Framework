# SimplyRESTful
[![License: LGPL v3](https://img.shields.io/badge/License-LGPL%20v3-blue.svg?style=plastic)](https://www.gnu.org/licenses/lgpl-3.0)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.arucard21.simplyrestful/simplyrestful/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/com.github.arucard21.simplyrestful/simplyrestful)

A framework for creating a RESTful API

Provides a simple and easy way to create a RESTful API using HAL+JSON as media type.

The basic premise is that this framework will allow you to implement RESTful API's without requiring in-depth knowledge about REST. It does this by taking care of the REST-related parts of your API with common or best-practice implementations. This includes things like data formatting, collection paging, proper use of HTTP methods and versioning. Some of these can't be implemented (entirely) in the framework, in which case documentation and example code should provide guidance on how best to implement this.

This means you shouldn't have to think about how exactly a RESTful API should be implemented, as most of these details are already take care of. This leaves more time to focus on the design of your resources, which [should require significant effort](http://roy.gbiv.com/untangled/2008/rest-apis-must-be-hypertext-driven) when creating a RESTful API.

Using this framework, you would mainly just need to implement a mapping between your data entities and your API resources. This framework will then make sure that these (HAL-based) API resources are made available as JAX-RS Web Resources that are part of your RESTful API.

## Usage
* Depend on the SimplyRESTful framework
* Create at least one HAL-based resource objects (by extending [HALResource](/src/main/java/simplyrestful/api/framework/core/hal/HALResource.java)) to represent your API resource
* Create at least one Data Access Object (by implementing the [HALResourceAccess](/src/main/java/simplyrestful/api/framework/core/HALResourceAccess.java) interface), mapping your data appropriately in order to provide them as HAL-based resource objects. Make sure this object can be found by your dependency injection framework of choice.
* Create at least one JAX-RS Web Resource that will provide access to your API resource (by extending [WebResourceBase\<T\>](/src/main/java/simplyrestful/api/framework/core/WebResourceBase.java) where T should be the HAL-based resource object you created). Provide it with the `@Path` annotation to define the resource name.
* Deploy the JAX-RS Web Resource
    * You can use [SimplyRESTful-jetty](https://github.com/arucard21/SimplyRESTful-jetty) to quickly deploy your API, or [SimplyRESTful-spring-boot](https://github.com/arucard21/SimplyRESTful-spring-boot) for a more customizable deployment.
