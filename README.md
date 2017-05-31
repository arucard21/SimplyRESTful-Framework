# SimplyRESTful
A framework for creating a RESTful API (still experimental)

Provides a simple and easy way to create a RESTful API using HAL+JSON as media type.
## Usage
To use it, you have to: 
* Create your HAL resource object (by extending HalResource)
* Create the API endpoint that will serve that resource (by extending ApiEndpointBase<T> where T should be the object you created)
* Implement the required methods, mapping your data appropriately in order to provide them as HAL-based objects to the endpoint
* Deploy the API (This framework uses Apache CXF to create JAX-RS API endpoints, so all of the Apache CXF deployment methods for JAX-RS should work)
    * Refer to the [documentation from Apache CXF on how to deploy services](http://cxf.apache.org/docs/deployment.html). 
    * There are also some [tips specifically for deploying JAX-RS services](https://cwiki.apache.org/confluence/display/CXF20DOC/JAX-RS+Deployment)
## Example
You can find an example in the `sample/main/java/example folder`, where:
* `data/` contains code needed to represent an existing data store containing data that will be exposed through the API. You likely already have some kind of data store available, so this is only needed to make this example work.
* `deployment/` contains code needed to deploy the API on `http://localhost:9000/` (on a minimally configured Jetty webserver). This can change entirely, depending on how you wish to deploy the API. For example, you could deploy using Spring Boot which uses XML files to configure the deployment. 
* `resources/` contains the actual example code. It has an API resource and an API endpoint with the required methods implemented for use with this example's data store. 
