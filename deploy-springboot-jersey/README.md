# deploy-springboot-jersey
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg?style=plastic)](https://opensource.org/licenses/Apache-2.0)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.arucard21.simplyrestful/deploy-springboot-jersey/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/com.github.arucard21.simplyrestful/deploy-springboot-jersey)

Convenience library for starting the SimplyRESTful API on a Tomcat server with Jersey using Spring Boot.

What this library does:
* Registers the JAX-RS providers required by SimplyRESTful.
* Makes the registered JAX-RS Web Resources available directly under the root of the path.
* Generates an OpenAPI Specification document at `/swagger.json`
* Provides a Swagger UI at `/api-docs`.
    * Tip: If you access Swagger UI with `/api-docs?url=../swagger.json` it automatically loads the generated OpenAPI Specification file of the API.

## Usage
To use it in your project you have to:
* *Prerequisite: Your project should be [set up as a Spring Boot application](https://docs.spring.io/spring-boot/docs/current/reference/html/).*
* Depend on [`deploy-springboot-jersey`](https://search.maven.org/artifact/com.github.arucard21.simplyrestful/deploy-springboot-jersey/).
* Implement your SimplyRESTful API according to the [standard instructions](/SimplyRESTful#usage).
    * You do not need to depend on the SimplyRESTful library directly as it is already pulled in by this library.
    * You do not have to follow the steps to configure your JAX-RS framework since this library takes care of most of that automatically.
* Register your implemented JAX-RS Web Resources in Jersey [as documented for Spring Boot](https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-jersey).
* Start your SimplyRESTful API by simply [running your Spring Boot application](https://docs.spring.io/spring-boot/docs/current/reference/html/using-spring-boot.html#using-boot-running-your-application).
    * By default, Spring Boot runs the API on `http://localhost:8080`.

See the [example project](/examples/springboot-jersey-nomapping-springdata) for a simple example of how this library can be used.
