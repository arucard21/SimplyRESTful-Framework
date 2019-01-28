# SimplyRESTful-spring-data
[![License: LGPL v3](https://img.shields.io/badge/License-LGPL%20v3-blue.svg?style=plastic)](https://www.gnu.org/licenses/lgpl-3.0)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.arucard21.simplyrestful/SimplyRESTful-spring-data/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/com.github.arucard21.simplyrestful/SimplyRESTful-spring-data)

A framework that connects Spring Data Rrepositories to SimplyRESTful APIs.

Provides convenience code connecting the SimplyRESTful API's ResourceDAO to a Spring Data Repository, using the same resource class for both the API and Spring Data. This means that there is no mapping or conversion possible between what the API exposes and what Spring Data stores in the database. This should be sufficient for simple, newly-created APIs. An alternative SimplyRESTful framework can be used when this mapping is required (which is not yet available).

This framework provides a customized base class for the Web Resource. It has both an ID, which is recommended for persisting it to the database, and a UUID, which is recommended for identifying it in the API. The database ID is not exposed through the API though the UUID is still used as part of the URI identifier for each resource. The framework also contains a customized base class for the resource DAO which connects it to a Spring Data Repository. The base class for this Spring Data Repository is also provided.

## Usage
To use it, in your project you have to:
* Depend on SimplyRESTful-spring-data
* [Implement your SimplyRESTful API](/SimplyRESTful#usage). However, you must extend [`NoMappingHALResource`](src/main/java/simplyrestful/springdata/repository/nomapping/NoMappingHALResource.java) instead of `HALResource` and implement [`NoMappingResourceDAO`](src/main/java/simplyrestful/springdata/repository/nomapping/NoMappingResourceDAO.java) instead of `ResourceDAO`.
* Extend the [`NoMappingRepository`](src/main/java/simplyrestful/springdata/repository/nomapping/NoMappingRepository.java) and provide the resource object you created by extending `NoMappingHALResource` as its generic object. This Repository interface will not require any additional code unless functionality that requires it was added to the class that extends `NoMappingResourceDAO`.
* Configure the Spring Data Repository to suit your needs. The documentation provided by Spring Data can elaborate on how exactly this can be done.
* Deploy your API as required by your chosen deployment method (e.g. depend on SimplyRESTful-jersey-spring-boot and [follow its instructions](/SimplyRESTful-jersey-spring-boot#usage) to deploy the API)

See the [SimplyRESTful-example](/SimplyRESTful-example/jersey-nomapping) project for a simple example of using Spring Data Repositories without mapping the resource.