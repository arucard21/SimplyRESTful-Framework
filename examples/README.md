# Examples
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg?style=plastic)](https://opensource.org/licenses/Apache-2.0)

Examples on how to use the SimplyRESTful suite of libraries and frameworks.

The examples are organized in the following sub-projects, each of which is explained below:
* `datastore` contains code needed to represent an existing data store containing data that will be exposed through the API. You likely already have some kind of data store available, so this is only needed to make these examples work and is itself not an example of how to use the SimplyRESTful suite of libraries and frameworks.
* `jetty` contains the example code for deploying with Jetty. You can run this by executing the main class in `ExampleAPIServer.java`.
* `spring-boot` contains the example code for deploying with Spring Boot. You can run this by executing the main class in `ExampleAPIApplication.java`.
* `jersey-nomapping` contains the example code for deploying with Spring Boot using Jersey as well as Spring Data to persist data in a database. You can run this by executing the main class in `JerseyNoMappingApplication.java`.