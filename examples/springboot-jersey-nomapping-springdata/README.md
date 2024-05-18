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

This API supports server-sent events for streaming a list of API resources, as opposed to retrieving them with pagination. You can get the stream of API resources by performing a GET request on the collection endpoint with the `Accept` header set to `text/event-stream`. Once you do, you should see the API resources coming in one at a time. To make the streaming of data more noticeable, this API adds a 1 second delay before the retrieval of every API resource from the database.

You can do this with `curl` as follows:

```shell
> curl --no-buffer --http2 -H "Accept:text/event-stream" --silent http://localhost:8888/resources?fields=all
```
