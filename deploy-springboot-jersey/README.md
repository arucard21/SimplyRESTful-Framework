# deploy-springboot-jersey
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg?style=plastic)](https://opensource.org/licenses/Apache-2.0)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.arucard21.simplyrestful/deploy-springboot-jersey/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/com.github.arucard21.simplyrestful/deploy-springboot-jersey)

Deploy your SimplyRESTful API using Jersey with Spring Boot.

Provides the configuration required to deploy a SimplyRESTful API using Jersey with Spring Boot. This does not automatically scan for components but instead registers the JAX-RS classes used by SimplyRESTful directly. This should provide quite a flexible base from which to customize your deployment but if you want or need more advanced configuration, you can copy the code from this library and customize it as needed.

The configuration makes all JAX-RS resources available under the path `/services` (e.g. `/services/apiresource`). The configuration also includes generation of an OpenAPI Specification file at `/services/swagger.json`. It enables Swagger-UI as well at `/services/api-docs`. You can access Swagger-UI with `/services/api-docs?url=../swagger.json` so it automatically loads the generated OpenAPI Specification file. By default, Spring Boot runs the server on `http://localhost:8080`.

## Usage
To use it, in your project you have to:
* Depend on SimplyRESTful and SimplyRESTful-jersey-spring-boot
* [Implement your SimplyRESTful API](/SimplyRESTful#usage)
* Make sure your web resource class (which extends WebResourceBase) is registered in a class that extends Jersey's `ResourceConfig` annotated with `@Configuration` (additional JAX-RS resources should registered here as well, as required by Jersey)
* Create a Spring Boot application class

See the [SimplyRESTful-example](/examples/springboot-jersey-nomapping-springdata) project for a simple example of this deployment method.
