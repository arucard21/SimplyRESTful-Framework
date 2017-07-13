# SimplyRESTful-jetty
[![License: LGPL v3](https://img.shields.io/badge/License-LGPL%20v3-blue.svg?style=plastic)](https://www.gnu.org/licenses/lgpl-3.0)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.arucard21.simplyrestful/simplyrestful-jetty/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/com.github.arucard21.simplyrestful/simplyrestful-jetty)

A quick way to deploy your SimplyRESTful API with Jetty

Provides a quick way to deploy the API based on SimplyRESTful on a Jetty server. It includes generation of a swagger.json
file as well as Swagger-UI. While this allows you to easily get your API working, for any serious use, you should deploy it using one of the deployment methods documented by Apache CXF. The code here can serve as an example on what exactly needs to be configured.

## Usage
To use it, in your project you have to: 
* Depend on the SimplyRESTful-jetty library
* Depend on the SimplyRESTful library
* Implement your SimplyRESTful API
* Deploy the API by creating a class with a main method that calls `APIServer.run(Endpoint.class)` where Endpoint.class is the endpoint you created (which extends ApiEndpointBase). 

See the SimplyRESTful-example project for an example of how this can be implemented. 
