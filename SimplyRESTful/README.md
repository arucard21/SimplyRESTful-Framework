# SimplyRESTful
[![License: LGPL v3](https://img.shields.io/badge/License-LGPL%20v3-blue.svg?style=plastic)](https://www.gnu.org/licenses/lgpl-3.0)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.arucard21.simplyrestful/SimplyRESTful/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/com.github.arucard21.simplyrestful/SimplyRESTful)

A framework for creating a RESTful API.

The framework provides a default implementation of a JAX-RS Web Resource (often called an endpoint) that adheres to the [HTTP 1.1 specification](https://tools.ietf.org/html/rfc7231) and uses [HAL+JSON](https://tools.ietf.org/html/draft-kelly-json-hal-08) as media type. The HTTP methods are translated to a simple CRUDL format (adding Listing to the Create, Read, Update and Delete for CRUD).

## Usage
* *Prerequisite: You have designed your API resources*  
* Add a dependency on [`SimplyRESTful`](https://search.maven.org/artifact/com.github.arucard21.simplyrestful/SimplyRESTful/) to your project
* For each of your API resources, create a [POJO](https://en.wikipedia.org/wiki/Plain_old_Java_object) that extends [HALResource](/SimplyRESTful-resources/src/main/java/simplyrestful/api/framework/resources/HALResource.java), providing it with a profile URI.
    * *It's strongly recommended to provide full documentation for your resource at the location indicated by the profile URI*
* For each POJO, create a JAX-RS Web Resource (aka endpoint) that extends [DefaultWebResource](src/main/java/simplyrestful/api/framework/core/DefaultWebResource.java) and uses that POJO as its generic type T.
* In this Web Resource, you can now implement the `create()`, `read()`, `update()`, `delete()` and `listing()` methods to connect to your backend as needed.
