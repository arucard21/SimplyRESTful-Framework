# SimplyRESTful-Framework
A framework for easily creating RESTful APIs.

The main library in the framework is [`SimplyRESTful`](/SimplyRESTful). It contains a default web resource implementation (also called an endpoint) that maps the more complicated [HTTP](https://tools.ietf.org/html/rfc7231)-compliant access to simpler [CRUDL](https://en.wikipedia.org/wiki/Create,_read,_update_and_delete) functions. This SimplyRESTful framework only requires [JAX-RS](https://jakarta.ee/specifications/restful-ws/) and can be used with any JAX-RS framework.

Other than this, the other libraries are mostly for auto-configuration (e.g. `deploy-*` libraries ) or to provide additional features (e.g. `fields-filter-json-servlet`). 

There are [examples for the different deploy libraries](/examples/) available for more concrete information about how to implement this.

You can find more information about this framework on the [wiki](https://github.com/arucard21/SimplyRESTful-Framework/wiki). 
