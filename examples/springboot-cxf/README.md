# Apache Tomcat on Spring Boot using Apache CXF
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg?style=plastic)](https://opensource.org/licenses/Apache-2.0)

Example on how to create a SimplyRESTful API using the `deploy-springboot-cxf` library.

In this example, the API is created with Apache CXF on Apache Tomcat using Spring Boot. The API resources are stored through a DAO (Data Access Object) in a Java List object in memory. This DAO maps the API resources to another POJO that is specifically created for storing the data. The DAO also maps the stored data back to an API resource.

You can run this example by executing the main class in `ExampleCXFApplication.java`.