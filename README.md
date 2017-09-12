# SimplyRESTful
[![License: LGPL v3](https://img.shields.io/badge/License-LGPL%20v3-blue.svg?style=plastic)](https://www.gnu.org/licenses/lgpl-3.0)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.arucard21.simplyrestful/simplyrestful/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/com.github.arucard21.simplyrestful/simplyrestful)

A framework for creating a RESTful API

Provides a simple and easy way to create a RESTful API using HAL+JSON as media type.

The basic premise is that this framework will allow you to implement RESTful API's without requiring in-depth knowledge about REST. It does this by taking care of the REST-related parts of your API with common or best-practice implementations. This includes things like data formatting, collection paging, proper use of HTTP methods and versioning. Some of these can't be implemented (entirely) in the framework, in which case documentation and example code should provide guidance on how best to implement this.

This means you shouldn't have to think about how exactly a RESTful API should be implemented, as most of these details are already take care of. This leaves more time to focus on the design of your resources, which [should require significant effort](http://roy.gbiv.com/untangled/2008/rest-apis-must-be-hypertext-driven) when creating a RESTful API.

This framework includes [FIQL-based search](https://cxf.apache.org/docs/jax-rs-search.html#JAX-RSSearch-FeedItemQueryLanguage) capabilities, through the availability of a SearchContext object in each JAX-RS Web Resource. This SearchContext object can be used to directly filter a collection of resources or it can be converted to a different type of query. Converters for several query types are already available in the Apache CXF framework, like SQL, JPA 2.0, LDAP, Lucene, etc. You can also create a custom converter to suit your needs.

Using this framework you would mainly just need to implement a mapping between your data entities and your API resources. This framework will then make sure that these (HAL-based) API resources are made available as JAX-RS Web Resources that are part of your RESTful API.

## Usage
* Depend on the SimplyRESTful framework
* Create at least one HAL-based resource objects (by extending HALResource) to represent your API resource
* Create at least one (`@Path`-annotated) JAX-RS Web Resource that will provide access to your API resource (by extending WebResourceBase<T> where T should be the HAL-based resource object you created)
* Implement the required methods in the Web Resource, mapping your data appropriately in order to provide them as HAL-based resource objects
* Deploy the JAX-RS Web Resource
    * Any of the Apache CXF deployment methods for JAX-RS should work. Refer to the [documentation from Apache CXF on how to deploy services](http://cxf.apache.org/docs/deployment.html) (includes documentation for JAX-WS services).
    * There is also some [documentation specifically for deploying JAX-RS services](https://cwiki.apache.org/confluence/display/CXF20DOC/JAX-RS+Deployment)
    * You can also use [SimplyRESTful-jetty](https://github.com/arucard21/SimplyRESTful-jetty) to quickly deploy your API, or [SimplyRESTful-spring-boot](https://github.com/arucard21/SimplyRESTful-spring-boot) for a more customizable deployment. 