# SimplyRESTful
[![License: LGPL v3](https://img.shields.io/badge/License-LGPL%20v3-blue.svg?style=plastic)](https://www.gnu.org/licenses/lgpl-3.0)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.arucard21.simplyrestful/simplyrestful/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/com.github.arucard21.simplyrestful/simplyrestful)

A framework for creating a RESTful API

Provides a simple and easy way to create a RESTful API using HAL+JSON as media type.

The basic premise is that this framework (using Apache CXF) will allow you to focus entirely on your API resource design by 
taking care of the technical details of your API. This includes things like data formatting, collection paging, proper use of HTTP methods and versioning. This means you don't have to think about how exactly a RESTful API should be implemented, as these details are already take care of. This leaves more time to focus on the design of you resource, which is where most of your efforts should go when creating an API (according to REST).

## Usage
To use it, you have to: 
* Depend on the SimplyRESTful library
* Create your HAL resource object (by extending HalResource)
* Create the API endpoint that will serve that resource (by extending ApiEndpointBase<T> where T should be the object you created)
* Implement the required methods, mapping your data appropriately in order to provide them as HAL-based objects to the endpoint
* Deploy the API (This framework uses Apache CXF to create JAX-RS API endpoints, so any of the Apache CXF deployment methods for JAX-RS should work)
    * Refer to the [documentation from Apache CXF on how to deploy services](http://cxf.apache.org/docs/deployment.html). 
    * There are also some [tips specifically for deploying JAX-RS services](https://cwiki.apache.org/confluence/display/CXF20DOC/JAX-RS+Deployment)
    
