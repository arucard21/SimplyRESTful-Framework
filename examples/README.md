# Examples
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg?style=plastic)](https://opensource.org/licenses/Apache-2.0)

Contains some examples on how to create a SimplyRESTful API, using the available convenience libraries for deploying.

* [`jetty-cxf`](/examples/jetty-cxf): Uses the `deploy-jetty-cxf` library.
    * You can run this by executing the main class in `ExampleCXFJettyServer.java`.
* [`springboot-cxf`](/examples/springboot-cxf): Uses the `deploy-springboot-cxf` library. 
    * You can run this by executing the main class in `ExampleCXFApplication.java`.
* [`springboot-jersey-nomapping-springdata`](/examples/springboot-jersey-nomapping-springdata): Uses the `deploy-springboot-jersey` library for deploying as well as the `persist-springdata` library to persist the API resources directly in a database. 
    * You can run this by executing the main class in `JerseyNoMappingApplication.java`.
