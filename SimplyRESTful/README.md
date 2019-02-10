# SimplyRESTful
[![License: LGPL v3](https://img.shields.io/badge/License-LGPL%20v3-blue.svg?style=plastic)](https://www.gnu.org/licenses/lgpl-3.0)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.arucard21.simplyrestful/SimplyRESTful/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/com.github.arucard21.simplyrestful/SimplyRESTful)

A framework for creating a RESTful API

Provides a simple and easy way to create a RESTful API using HAL+JSON as media type.

The basic premise is that this framework will allow you to implement RESTful API's without requiring in-depth knowledge about REST. It does this by taking care of the REST-related parts of your API with common or best-practice implementations. This includes things like data formatting, collection paging, proper use of HTTP methods and versioning. Some of these can't be implemented (entirely) in the framework, in which case documentation and example code should provide guidance on how best to implement this.

This means you shouldn't have to think about how exactly a RESTful API should be implemented, as most of these details are already take care of. This leaves more time to focus on the design of your resources, which [should require significant effort](http://roy.gbiv.com/untangled/2008/rest-apis-must-be-hypertext-driven) when creating a RESTful API.

Using this framework, you would mainly just need to implement a mapping between your data entities and your API resources. Even this is not needed, if you choose to persist your API resource directly. This framework will make sure that these (HAL-based) API resources are made available as JAX-RS Web Resources that are part of your RESTful API.

## Usage
* Depend on the SimplyRESTful framework (which automatically pulls in SimplyRESTful-resources)
* Create at least one HAL-based resource objects (by extending [HALResource](/SimplyRESTful-resources/src/main/java/simplyrestful/api/framework/resources/HALResource.java)) to represent your API resource, providing it with a profile URI
    * It's strongly recommended to provide full documentation for your resource at the location indicated by the profile URI
* Create the Data Access Objects (by extending the [ResourceDAO](src/main/java/simplyrestful/api/framework/core/ResourceDAO.java) and [EntityDAO](src/main/java/simplyrestful/api/framework/core/EntityDAO.java) classes), mapping your data appropriately in order to provide them as HAL-based resource objects. You need to implement a [ResourceMapper](src/main/java/simplyrestful/api/framework/core/mapper/ResourceMapper.java) or you can use the provided mapper if both your resource and entity are the same.  Make sure these classes can be found by your dependency injection framework of choice.
* Create at least one JAX-RS Web Resource that will provide access to your API resource (by implementing [AbstractWebResource](src/main/java/simplyrestful/api/framework/core/AbstractWebResource.java). You need to specify both the object representing the API resource, T, and the object that will be persisted, E. If no mapping between the two is needed, you can specify both to be T. Provide the implemented WebResource class with the `@Path` annotation to define the resource name.
* Deploy the JAX-RS Web Resource
    * You can use any of the deploy libraries to more easily deploy the API, e.g. [deploy-jetty-cxf](/deploy-jetty-cxf) or [deploy-springboot-cxf](/deploy-springboot-cxf).
