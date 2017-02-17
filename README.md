# SimplyRESTful
A framework for creating a RESTful API (still experimental)

Provides a simple and easy way to create a RESTful API using HAL+JSON as media type.

To use it, you have to: 
* Create your HAL resource object (by extending HalResource)
* Create the API endpoint that will serve that resource (by extending ApiEndpointBase<T> where T should be the object you created)
* Implement the required methods, mapping your data appropriately in order to provide them as HAL-based objects to the endpoint
* Deploy the API (it's based on Apache CXF so any of its deployment methods should work)
