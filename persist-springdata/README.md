# persist-springdata
[![License: LGPL v3](https://img.shields.io/badge/License-LGPL%20v3-blue.svg?style=plastic)](https://www.gnu.org/licenses/lgpl-3.0)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.arucard21.simplyrestful/persist-springdata/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/com.github.arucard21.simplyrestful/persist-springdata)

A framework that connects Spring Data Rrepositories to SimplyRESTful APIs.

Provides convenience code connecting the SimplyRESTful API's EntityDAO to a Spring Data Repository.

## Usage
To use it, in your project you have to:
* Depend on persist-springdata
* [Implement your SimplyRESTful API](/SimplyRESTful#usage). However, you must extend [`SpringDataEntityDAO`](src/main/java/simplyrestful/springdata/repository/SpringDataEntityDAO.java) instead of `EntityDAO`.
* Extend the [`SpringDataEntityDAO`](src/main/java/simplyrestful/springdata/repository/SpringDataEntityDAO.java) and provide an object you created for persistence as its generic object. This Repository interface will not require any additional code unless functionality that requires it was added to the class that extends `SpringDataEntityDAO`.
* Configure the Spring Data Repository to suit your needs. The documentation provided by Spring Data can elaborate on how exactly this can be done.
* Deploy your API as required by your chosen deployment method

See the [example](/examples/springboot-jersey-nomapping-springdata) project for a simple example of using Spring Data Repositories without mapping the resource.