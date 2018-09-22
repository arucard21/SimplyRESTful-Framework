# SimplyRESTful-example
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg?style=plastic)](https://opensource.org/licenses/Apache-2.0)

Examples on how to use the SimplyRESTful library, with different deployment methods.

The examples are organized in the following sub-projects, each of which contains an example for a specific deployment method (except `datastore` which is shared code to make the examples work):
* `datastore` contains code needed to represent an existing data store containing data that will be exposed through the API. You likely already have some kind of data store available, so this is only needed to make this example work. 
* `jetty` contains the example code for deploying with Jetty. You can run this by executing the main class in `ExampleAPIServer.java`.
* `spring-boot` contains the example code for deploying with Spring Boot. You can run this by executing the main class in `ExampleAPIApplication.java`.