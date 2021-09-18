# SimplyRESTful-Framework
A framework for easily creating RESTful APIs.

The main library in the framework is [`SimplyRESTful`](/SimplyRESTful). It contains a default web resource implementation (also called an endpoint) that maps the more complicated [HTTP](https://tools.ietf.org/html/rfc7231)-compliant access to simpler [CRUD](https://en.wikipedia.org/wiki/Create,_read,_update_and_delete) functions. This SimplyRESTful framework only requires [JAX-RS](https://jakarta.ee/specifications/restful-ws/) and can be used with any JAX-RS framework.

There are 2 client libraries available, [one for Java](/client) and the [other for TypeScript](/client-ts). Both of these can be used to access any API created with SimplyRESTful. They only need to know the API resource specific to that API. For Java, that could be as simple as providing the same POJO as used in the API. For TypeScript, you would need to provide a type that corresponds to the API resource.

Other than this, the other libraries are mostly for convenience (e.g. auto-configuration with the `deploy-*` libraries ) or to provide additional features (e.g. JSON field filtering with `fields-filter-json-servlet`). 

There are [examples for the different deploy libraries](/examples/) available for more detailed information about how to implement this.

You can find more information about the idea behind this framework on the [wiki](https://github.com/arucard21/SimplyRESTful-Framework/wiki). 
