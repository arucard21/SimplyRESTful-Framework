# SimplyRESTful TypeScript Client
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg?style=plastic)](https://opensource.org/licenses/Apache-2.0)
[![npm](https://img.shields.io/npm/v/simplyrestful-client?style=plastic)](https://www.npmjs.com/package/simplyrestful-client)

A TypeScript/JavaScript client library for programmatic access to any SimplyRESTful API.

This client library provides TS/JS-based programmatic access to any SimplyRESTful-based API.

* It requires only the TypeScript classes of the API resources (at runtime).
* The HTTP-based API access is mapped to simpler [CRUDL](https://en.wikipedia.org/wiki/Create,_read,_update_and_delete)-based access.

## Usage
For the API resource in the SimplyRESTful API you wish to access, create a TypeScript type that extends the `HALResource` type provided by `simplyrestful-client`.
```TypeScript
import { HALResource } from 'simplyrestful-client';

export type ExampleResource = HALResource & {
    name: string;
    someAttribute: {
        attributeName: string;
    };
}
```
Create the SimplyRESTful client for that API resource, using the type you created.
```TypeScript
import { SimplyRESTfulClient } from 'simplyrestful-client';
import { ExampleResource } from './ExampleResource';

// This should be the base URI of the SimplyRESTful API, where its Service Document is hosted.
const apiHostname = new URL('http://localhost/api');
// This should be the HAL+JSON profile of the API resource. This is a media type parameter for HAL+JSON that is required in SimplyRESTful APIs.
const resourceProfile = new URL('https://arucard21.github.io/SimplyRESTful-Framework/ExampleResource/v1');

const client: SimplyRESTfulClient<ExampleResource> = new SimplyRESTfulClient(apiHostname, resourceProfile);
```
Use the client to access the API through its create, read, update, delete and list methods, each of which returns a Promise.
```TypeScript
// Retrieve the list of API resources
client.list().then(resources => {
	// Use the retrieved API resources
})

// Retrieve a single API resource
const resourceId = new URL('http://localhost/api')
client.read(resourceId).then((exampleResource: ExampleResource) => {
	// Use the retrieved API resource
});
```
If you need to use specific HTTP headers or query parameters to access the API, you can also provide these to the client.
```TypeScript
// create headers necessary to access your API
const headers = new Headers();
headers.append('X-Custom-Header', 'some value');
// add custom query parameters
const customQueryParameters = new URLSearchParams({ "param1": "value1", "param2": "value2" });
// define the URL identifier for the resource you wish to access in the API
const resourceId = new URL('http://localhost/api')

// Retrieve the API resource, using the headers and custom query parameters as well
client.read(resourceId, headers, customQueryParameters).then((exampleResource: ExampleResource) => {
	// Use the retrieved API resource
});

// Delete the API resource, using the headers and custom query parameters as well
client.delete(resourceId, headers, customQueryParameters).then(() => {
	// Continue your application since delete() does not return anything. When the deletion succeeds, the Promise is resolved. Otherwise, it is rejected.
});

```
