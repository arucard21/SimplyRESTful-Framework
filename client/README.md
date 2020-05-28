# SimplyRESTful Client
[![License: LGPL v3](https://img.shields.io/badge/License-LGPL%20v3-blue.svg?style=plastic)](https://www.gnu.org/licenses/lgpl-3.0)

A Java client library for programmatic access to any SimplyRESTful API.

This client library provides Java-based programmatic access to any SimplyRESTful-based API

* It requires only the Java classes of the API resources.
* The Java classes of the API resources are only required at runtime.
* The HTTP-based API access is mapped to simpler [CRUDL](https://en.wikipedia.org/wiki/Create,_read,_update_and_delete)-based access.

## Usage
Create the client by providing
1. the URI of this specific resource's API (e.g. `http://localhost/api/myresources/`)
1. the class to which the resource should be deserialized. (e.g. `MyResource.class`)

```java
SimplyRESTfulClient<MyResource> client = new SimplyRESTfulClient<>(URI.create("http://localhost/api/myresources/"), MyResource.class)
```
You can now use this client to retrieve resources from the API through the `create()`, `read()`, `update()`, `delete()` and `list()` methods. See the javadoc on each method for more information.

Since hypermedia controls may require a highly customized HTTP request, the client provides a `hypermediaControl()` method that simply provides you with a [`WebTarget`](https://jakarta.ee/specifications/platform/8/apidocs/javax/ws/rs/client/WebTarget.html) object that is pre-configured with the URI to the API for convenience. It can be further configured with query parameters, HTTP headers and anything else required to correctly configure the request for that hypermedia control.  
