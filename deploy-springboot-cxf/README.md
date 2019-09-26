# deploy-springboot-cxf
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg?style=plastic)](https://opensource.org/licenses/Apache-2.0)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.arucard21.simplyrestful/deploy-springboot-cxf/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/com.github.arucard21.simplyrestful/deploy-springboot-cxf)

Convenience library for starting the SimplyRESTful API on a Tomcat server with Apache CXF using Spring Boot.

What this library does:
* Registers the JAX-RS providers required by SimplyRESTful.
* Makes the registered JAX-RS Web Resources available directly under the path `/`.
* Generates an OpenAPI Specification document at `/swagger.json`
* Provides a Swagger UI at `/api-docs`. 
    * Tip: If you access Swagger UI with `/api-docs?url=../swagger.json` it automatically loads the generated OpenAPI Specification file of the API. Or you can go to `/services`, which provides you with this link.

## Usage
To use it in your project you have to:
* *Prerequisite: Your project should be [set up as a Spring Boot application](https://docs.spring.io/spring-boot/docs/current/reference/html/).* 
* Depend on `[deploy-springboot-cxf](https://search.maven.org/artifact/com.github.arucard21.simplyrestful/deploy-springboot-cxf/)`
* Implement your SimplyRESTful API according to the [standard instructions](/SimplyRESTful#usage).
* Make sure your implemented JAX-RS Web Resources can be detected by a Spring Component Scan.
    * An easy way to do this is to annotate your Web Resource class with `@Named` (`javax.inject.Named`) and make sure it is located in a subpackage of the package containing your Spring Boot Application class (or the same package).
* Start your SimplyRESTful API by simply [running your Spring Boot application](https://docs.spring.io/spring-boot/docs/current/reference/html/#using-boot-running-your-application).
    * By default, Spring Boot runs the API on `http://localhost:8080`.

See the [example project](/examples/springboot-cxf) for a simple example of how this library can be used.
