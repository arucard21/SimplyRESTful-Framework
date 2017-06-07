# SimplyRESTful-jetty
A quick way to deploy your SimplyRESTful API with Jetty

Provides a quick way to deploy the API based on SimplyRESTful on a Jetty server. It includes generation of a swagger.json
file as well as Swagger-UI. While this allows you to easily get your API working, for any serious use, you should deploy it using one of the deployment methods documented by Apache CXF. The code here can serve as an example on what exactly needs to be configured.

## Usage
To use it, you have to: 
* Depend on the SimplyRESTful-jetty library (Note: not yet available through Maven)
* Implement your SimplyRESTful API
* Deploy the API by creating a class with a main method that calls `APIServer.run(Endpoint.class)` where Endpoint.class is the endpoint you created (which extends ApiEndpointBase). 

## Example
You can find an example in the `sample/main/java/example folder`, where:
* `data/` contains code needed to represent an existing data store containing data that will be exposed through the API. You likely already have some kind of data store available, so this is only needed to make this example work.
* `resources/` contains the actual example code. It has an API resource and an API endpoint with the required methods implemented for use with this example's data store.

You can easily run this example by executing the main class in ExampleAPIServer.java. 