# Examples
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg?style=plastic)](https://opensource.org/licenses/Apache-2.0)

Contains some examples on how to create a SimplyRESTful API, using the available convenience libraries for deploying.

* [`jetty-cxf`](/examples/jetty-cxf): Uses the `deploy-jetty-cxf` library.
    * You can run this by executing the main class in `ExampleCXFJettyServer.java`.
* [`springboot-cxf`](/examples/springboot-cxf): Uses the `deploy-springboot-cxf` library.
    * You can run this by executing the main class in `ExampleCXFApplication.java`.
* [`springboot-jersey-nomapping-springdata`](/examples/springboot-jersey-nomapping-springdata): Uses the `deploy-springboot-jersey` library for deploying as well.
    * You can run this by executing the main class in `JerseyNoMappingApplication.java`.
* [`microprofile-openliberty`](/examples/microprofile-openliberty): Uses the MicroProfile 4.1 standard through the OpenLiberty library for implementing the API.
    * You can run this using Gradle with `./gradlew clean :examples:microprofile-openliberty:libertyRun --no-daemon` from the root of the project.
