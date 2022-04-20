# Apache Tomcat on Spring Boot using Jersey and Spring Data
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg?style=plastic)](https://opensource.org/licenses/Apache-2.0)

Example on how to create a SimplyRESTful API using the `deploy-springboot-jersey` library for deploying as well as the `persist-springdata` library to persist the API resources directly in a database.

In this example, the API is created with Jersey on Apache Tomcat using Spring Boot. The Web Resource requires almost no implementation. The resources are stored directly in a database using a DAO (Data Access Object) and a Spring Data Repository that also require almost no implementation.

You can run this example by executing the main class in `JerseyNoMappingApplication.java`.

These environment variables are recommended (but not required)

```
server.port=8888
SIMPLYRESTFUL_URI_HTTP_HEADER=xoriginalurl
```