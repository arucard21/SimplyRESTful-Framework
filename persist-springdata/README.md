# persist-springdata (Deprecated)
[![License: LGPL v3](https://img.shields.io/badge/License-LGPL%20v3-blue.svg?style=plastic)](https://www.gnu.org/licenses/lgpl-3.0)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.arucard21.simplyrestful/persist-springdata/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/com.github.arucard21.simplyrestful/persist-springdata)

Convenience library for creating a SimplyRESTful API that stores its API resources directly in a database using Spring Data.

What this library does:
* Extends SimplyRESTful's default implementation of a JAX-RS Web Resource to persist the API resources directly to a database using a Spring Data Repository.
* Extends SimplyRESTful's `HALResource` to directly include the UUID used in the resource's self-link.
* Ensures that the self-link and UUID remain consistent (only the UUID is stored in the database, not the self-link).

## Deprecation Notice
This library is now considered deprecated and will be removed soon. The usefulness of having API resources directly stored to a database has turned out to too little to merit maintaining this convenience library. The main reason is due to the different best practices when dealing with links in the API and in the database. 

In the API, a link should be a represented as an absolute URI but in the database you should store only the UUID part of the URI. The rest of the URI, representing where the API being linked to is actually running, should be removed. This information is available within the API implementation and absolute URIs can be built up dynamically for the links when providing it in a response. this means that any API resource that contains a link will need to have at least some mapping between the API resource and the entity stored in the database. 

Given that some mapping is almost always required, the convenience of automating the mapping between API resource and entity does not weigh up against the cost of maintaining this convenience library. Instead, you should use the standard [`SimpyRESTful`](/SimplyRESTful/) library.

## Usage
To use it, in your project you have to:
* *Prerequisite: You have designed your API resources*
* Add a dependency on [`persist-springdata`](https://search.maven.org/artifact/com.github.arucard21.simplyrestful/persist-springdata/) to your project
* For each of your API resources, create a [POJO](https://en.wikipedia.org/wiki/Plain_old_Java_object) that extends [`SpringDataHALResource`](src/main/java/simplyrestful/springdata/resources/SpringDataHALResource.java), providing it with a profile URI.
    * *It's strongly recommended to provide full documentation for your resource at the location indicated by the profile URI.*
* For each POJO, create an interface class that extends [`SpringDataRepository`](src/main/java/simplyrestful/springdata/repository/SpringDataRepository.java) and uses that POJO as its generic type E.
    * *This class does not require any code.*
* For each POJO, create a class that extends [`SpringDataEntityDAO`](src/main/java/simplyrestful/springdata/repository/SpringDataEntityDAO.java) and uses that POJO as its generic type E. 
    * *In this class, you only need to define the constructor and provide it with your implementation of the `SpringDataRepository`.*
* For each POJO, create a JAX-RS Web Resource (aka endpoint) that extends [`SpringDataWebResource`](src/main/java/simplyrestful/springdata/resources/SpringDataWebResource.java) and uses that POJO as its generic type T.
    * *In this Web Resource, you only need to define the constructor and provide it with your implementation of the `SpringDataEntityDAO`.*
* In your JAX-RS framework, register:
    * a `JacksonJsonProvider` instance that uses `HALMapper` (`io.openapitools.jackson.dataformat.hal.HALMapper`) as `ObjectMapper`.
    * the [`WebResourceRoot`](/SimplyRESTful/src/main/java/simplyrestful/api/framework/core/servicedocument/WebResourceRoot.java) class.
    * the Web Resources you created for each POJO.
* Deploy and run your SimplyRESTful API as any other JAX-RS API.

See the [example project](/examples/springboot-jersey-nomapping-springdata) for a simple example of how this library can be used.
