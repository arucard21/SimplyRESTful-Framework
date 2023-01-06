# SimplyRESTful
[![License: LGPL v3](https://img.shields.io/badge/License-LGPL%20v3-blue.svg?style=plastic)](https://www.gnu.org/licenses/lgpl-3.0)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.arucard21.simplyrestful/SimplyRESTful/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/com.github.arucard21.simplyrestful/SimplyRESTful)

A framework for creating a RESTful API.

This framework provides a default implementation of a JAX-RS Web Resource (sometimes called an endpoint) that maps the more complicated [HTTP](https://tools.ietf.org/html/rfc7231)-compliant access to simpler [CRUD](https://en.wikipedia.org/wiki/Create,_read,_update_and_delete) functions. This has the benefit of providing standards-compliant as well as consistent behavior for your API without needing to understand the complexity of standards like HTTP. As API developer you only have to implement CRUD functions which are much simpler and more concretely defined. This level of consistency in the API also allows for additional convenience and features to be implemented generically, with little to no effort required from the API developer to enable it.
* The implementation is based on [JAX-RS](https://jakarta.ee/specifications/restful-ws/) and can be used with any JAX-RS framework.
* The HTTP methods are mapped to simple CRUD functions (Create, Read, Update, Delete and List by default). The exact details of how each of these methods should behave is provided in the accompanying javadoc.

## Usage
### Prerequisites
* Make sure you have designed your API resources and identified their attributes, links, embedded resources and hypermedia controls
* Add a dependency on [`SimplyRESTful`](https://search.maven.org/artifact/com.github.arucard21.simplyrestful/SimplyRESTful/) to your project

### Implement your API resource
For each of your API resources, create a [POJO](https://en.wikipedia.org/wiki/Plain_old_Java_object) that extends [APIResource](/SimplyRESTful-resources/src/main/java/simplyrestful/api/framework/resources/APIResource.java) and provide it with a custom JSON media type.

```Java
public class MyResource extends APIResource {  
  public static final String MEDIA_TYPE_JSON = "application/x.myresource-v1+json";

  @Override
  public MediaType getCustomJsonMediaType(){
  	return MediaType.valueOf(MEDIA_TYPE_JSON);
  }

  // Add the rest of your resource as well
}
```
### Implement your Web Resource
For each POJO, create a JAX-RS Web Resource (sometimes called an endpoint) that extends [DefaultWebResource](src/main/java/simplyrestful/api/framework/core/DefaultWebResource.java) and uses that POJO as its generic type T.

```Java
public class MyWebResource extends DefaultWebResource<MyResource>{
  @Override
  public MyResource create(MyResource resource, UUID resourceUUID){/* Add your implementation*/}

  @Override
  public MyResource read(UUID resourceUUID){/* Add your implementation*/}

  @Override
  public MyResource update(MyResource resource, UUID resourceUUID){/* Add your implementation*/}

  @Override
  public MyResource delete(UUID resourceUUID){/* Add your implementation*/}

  @Override
  public MyResource list(long pageNumber, long pageSize, List<String> fields, String query, List<SortOrder> sort){/* Add your implementation*/}
}
```
In this Web Resource, you can now implement the `create()`, `read()`, `update()`, `delete()` and `list()` methods to connect to your backend as needed. Though you should mostly implement these methods as you would expect, there are some things to keep in mind.
* The `create()` method should return the exact resource that was stored, not what was provided (since storing the resource may sometimes change it).
* If the `read()` method cannot find a resource with the given ID, it should return `null`.
* Same as with `create()`, the `update()` method should return the exact resource that was updated.
* Same as with `read()`, the `delete()` method should return `null` if it could not find the resource with the given ID.
* The `list()` method works as expected and just returns a list of resources.
  * The list should be filtered according to the `query` parameter, which is specified as a [FIQL](https://tools.ietf.org/html/draft-nottingham-atompub-fiql-00) query.
  * It should also be sorted according to the `sort` parameter, which is specified as an ordered list of fields, along with their sort direction.
  * While the `fields` parameter can be used to restrict which fields are retrieved, it is optional. The fields filtering can be done outside of this implementation, unlike querying and sorting. This is implemented in the framework [as a Jakarta Servlet filter](/fields-filter-json-servlet).
* For improved performance, you should also override the `count()` and `exists()` methods with a more efficient implementation for your specific backend. By default, it wil use the `list()` and `read()` methods, respectively, to provide this functionality.

### Configure your JAX-RS framework
You can configure your JAX-RS framework manually, as described below, or you can use one of the convenience deploy libraries provided by the framework (see the [main README](/../..) for more details).

In your JAX-RS framework, register:
* the `JacksonJsonProvider` class (to correctly serialize and deserialize our plain JSON documents).
```Java
// Example for Jersey (in ResourceConfig)
register(JacksonJsonProvider.class);
```
* the Swagger classes (to generate the OpenAPI Specification document).
```Java
// Example for Jersey (in ResourceConfig)
register(OpenApiResource.class);
register(AcceptHeaderOpenApiResource.class);
```
* the `UriCustomizer` class (optional, allows the original URL used by the client to be retrieved from an HTTP header and used in the API when creating links).
```Java
// Example for Jersey (in ResourceConfig)
register(UriCustomizer.class);
```
* the [`WebResourceRoot`](src/main/java/simplyrestful/api/framework/core/servicedocument/WebResourceRoot.java) class (to provide the ServiceDocument at the root of your API).
```Java
// Example for Jersey (in ResourceConfig) with JAX-RS-managed lifecycle
register(WebResourceRoot.class);
```
* the Web Resources you created for each POJO (to make them available in the API).
```Java
// Example for Jersey (in ResourceConfig) with package scanning
packages("com.name.of.my.root.package");
```

### Deploy your API
If you registered the `UriCustomizer` class, you will need to provide `SIMPLYRESTFUL_URI_HTTP_HEADER` as environment variable with the HTTP header name containing the original URL as its value, e.g. `SIMPLYRESTFUL_URI_HTTP_HEADER=xoriginalurl` with the `xoriginalurl` HTTP header containing the absolute URL used by the client. 

Deploy and run your SimplyRESTful API as you would any other JAX-RS API, e.g. [start your Jetty server](https://www.eclipse.org/jetty/documentation/current/startup.html) or [run your Spring Boot application](https://docs.spring.io/spring-boot/docs/current/reference/html/using-spring-boot.html#using-boot-running-your-application).

The SimplyRESTful framework will ensure that there is a service document at the root of your deployed API as the main entry point to your API. This service document provides discoverable documentation, i.e. a link to the OpenAPI Specification where the API is described fully. You can then use the API as you normally would, or you can use one of the provided clients, in [Java](/client) or [TypeScript](/client-ts), to access the API more easily.
