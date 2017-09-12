# SimplyRESTful-spring-boot
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg?style=plastic)](https://opensource.org/licenses/Apache-2.0)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.arucard21.simplyrestful/simplyrestful-spring-boot/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/com.github.arucard21.simplyrestful/simplyrestful-spring-boot)

Deploy your SimplyRESTful API with Spring Boot.

Provides the configuration required to deploy a SimplyRESTful API with Apache CXF using Spring Boot auto-scanning. This should provide quite a flexible base from which to customize your deployment but if you want or need more advanced configuration, you can always copy the code from this library and customize it as needed. 

The configuration makes all JAX-RS resources available under the path `/services` (e.g. `/services/apiresource`). The configuration also includes generation of an OpenAPI Specification file at `/services/swagger.json`. It enables Swagger-UI as well at `/services/api-docs`. You can access Swagger-UI with `/services/api-docs?url=../swagger.json` so it automatically loads the generated OpenAPI Specification file. By default, Spring Boot runs the server on `http://localhost:8080`.

## Usage
To use it, in your project you have to: 
* Depend on SimplyRESTful and SimplyRESTful-spring-boot
* [Implement your SimplyRESTful API](https://github.com/arucard21/SimplyRESTful#usage)
* Make sure your web resource class (which extends WebResourceBase) is annotated with `@javax.inject.Named` so it is detected by Spring Boot's auto-scan.
* Create a Spring Boot application class which scans the `simplyrestful.springboot` package as well as your own package(s), containing the web resource class(es).

See the [SimplyRESTful-example](https://github.com/arucard21/SimplyRESTful-example) project for simple examples of different SimplyRESTful API deployment methods.
