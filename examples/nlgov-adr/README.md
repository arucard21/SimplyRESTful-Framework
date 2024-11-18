# NLGov API Design Rules (ADR) example
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg?style=plastic)](https://opensource.org/licenses/Apache-2.0)

Example API that adheres to the [NLGov REST API Design Rules (ADR)](https://logius-standaarden.github.io/API-Design-Rules/). This is linted and validated with the ADR tooling to see how closely SimplyRESTful can adhere to these rules and how that would work.

## Linter results
This is the output from the Spectral linter with the ADR ruleset applied.

```shell
$ spectral lint -r https://developer.overheid.nl/static/adr/ruleset.yaml http://localhost:8080/v0/openapi.json

http://localhost:8080/v0/openapi.json
 1:207  warning  servers-use-https  Server URL /v0 "/v0" must match the pattern "^https://.*".  servers[0].url

✖ 1 problem (0 errors, 1 warning, 0 infos, 0 hints)
```
The remaining warning could be resolved on a normal deployment where it would most likely be running with HTTPS. This was running locally for this Proof-of-Concept so it didn't make much sense to add HTTPS just for this warning.

The following changes were needed to achieve these results.
* Added an OpenAPI configuration file for general information that should be included in the OpenAPI Specification document.
* Set the JAX-RS application path to the major version of the API so that the API URI includes the major version number
* Added more OpenAPI annotations to the default implementations in the framework
* Added OpenAPI annotations in the Web Resource for information specific to its API resource
* Created a OpenAPI filter to provide the API-Version HTTP header in each response documented in the OpenAPI Specification
* Changed all class names to be fully consistent with PascalCase

## Validator results
This is the output from the ADR Validator.

```shell
$ adr-validator validate core http://localhost:8080/v0
Rule ID  Passed  Message
-------  ------  -------
API-03   Yes     
API-16   Yes     
API-20   Yes     
API-48   Yes     
API-51   Yes     
API-56   Yes     
API-57   Yes     

Validation passed
```
There is also a validator to check that TLS security is applied, i.e. that it is using HTTPS with sufficient security.
```shell

$ adr-validator validate security-tls http://localhost:8080/v0
Rule ID        Passed  Message
-------        ------  -------
tls-version    No      Secure versions MUST be supported
cipher-suites  No      Skipped: TLS 1.2 or TLS 1.3 MUST be supported.

Validation failed

```
All the core tests from the validator passed. 

The TLS security tests fail. As mentioned before, the API is only running locally for this Proof-of-Concept and moving to a normal deployment scenario would easily make these tests pass.

The following changes were needed to achieve these results.
* Created a JAX-RS filter to provide the API-Version HTTP header
* Enabled Spring CORS functionality with a policy that allows all origins, methods, and headers.
* Created a JAX-RS filter to enforce that any request with a trailing slash returns a 404, instead of being considered equivalent to not having a trailing slash.

## ADR Rules

An interesting difference between the linter and the validator, is that the Spectral linter validates the ADR rules according to [version 2.0.0](https://logius-standaarden.github.io/API-Design-Rules/#normative-design-rules) while the ADR-Validator checks the rules according to [ADR 1.0](https://gitdocumentatie.logius.nl/publicatie/api/adr/1.0/#normative-design-rules). This shouldn't matter much for the actual validation since ADR 2.0.0 contains all rules from ADR 1.0 and only adds to them. The main difference is that the naming syntax was changed from something like `API-00` to something like `/core/some-rule-name`. They are also grouped by functional and technical rules where only the technical rules can be tested by the linter or the validator.

I'll list all rules below according to version 2.0.0 and add the corresponding rule ID from 1.0 where possible. The rule description from version 1.0 and 2.0.0 are exactly the same. I'll also include whether that rules is tested by the linter and the validator.

* Functional rules
	* `/core/naming-resources`: Use nouns to name resources (API-05)
	* `/core/naming-collections`: Use plural nouns to name collection resources (API-54)
	* `/core/interface-language`: Define interfaces in Dutch unless there is an official English glossary available (API-04)
	* `/core/hide-implementation`: Hide irrelevant implementation details (API-53)
	* `/core/http-safety`: Adhere to HTTP safety and idempotency semantics for operations (API-01)
	* `/core/stateless`: Do not maintain session state on the server (API-02)
	* `/core/nested-child`: Use nested URIs for child resources (API-06)
	* `/core/resource-operations`: Model resource operations as a sub-resource or dedicated resource (API-10)
	* `/core/doc-language`: Publish documentation in Dutch unless there is existing documentation in English (API-17)
	* `/core/deprecation-schedule`: Include a deprecation schedule when deprecating features or versions (API-18)
	* `/core/transition-period`: Schedule a fixed transition period for a new major API version (API-19)
	* `/core/changelog`: Publish a changelog for API changes between versions (API-55)
	* `/core/geospatial`: Apply the geospatial module for geospatial data (New in ADR 2.0.0)
* Technical rules (can be tested by both the linter and validator, unless stated otherwise)
	* `/core/no-trailing-slash`: Leave off trailing slashes from URIs (API-48)
	* `/core/http-methods`: Only apply standard HTTP methods (API-03)
	* `/core/doc-openapi`: Use OpenAPI Specification for documentation (API-16)
	* `/core/publish-openapi`: Publish OAS document at a standard location in JSON-format (API-51)
		* Can not be tested with Spectral linter ruleset
	* `/core/uri-version`: Include the major version number in the URI (API-20)
	* `/core/semver`: Adhere to the Semantic Versioning model when releasing API changes (API-56)
		* Can not be tested by Spectral linter ruleset
	* `/core/version-header`: Return the full version number in a response header (API-57)
	* `/core/transport-security`: Apply the transport security module (New in ADR 2.0.0)
		* Can be tested with ADR-Validator using the `security-tls` ruleset (as opposed to the `core` ruleset)

The Spectral linter ruleset actually tests some rules that aren't explicitly mentioned in the Normative Design Rules of the ADR. These are the additional rules in the ruleset.

* The path names must be kebab-case.
* The schema names must be PascalCase (though this documented as "CamelCase" in the ruleset).
* The schema property names must be camelCase (though this is documented as "lower-case" in the ruleset).
* Any 4xx or 5xx response body must conform to [RFC 7807](https://www.rfc-editor.org/rfc/rfc7807) which defines a machine-readable format for describing problem details (though this RFC has been updated and obsoleted by [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457) in July 2023).

## Evaluation

### Functional ADR rules

The functional ADR rules are not included in the tests of the linter and validator but I could manually verify that these rules are consistent with the design choices of SimplyRESTful. These ADR rules are more extensive than what has been considered for SimplyRESTful so far but those also seem to make sense for SimplyRESTful.

The only exceptions are `/core/interface-language` and `/core/doc-language` which require the use of Dutch in both the API and its documentation. This makes sense for the ADR rules defined by the Dutch government but less so for any international organization where English is the norm. The suggested use of a glossary to avoid confusion does seem useful in any language though. With "glossary", this rule seems to refer to what's also called schema, a vocabulary, or an ontology. This is more closely related to Linked Data though, and maybe JSON-LD might make more sense when integrating a glossary into the API.

For links to other resources or to resource actions (hypermedia controls on resource), you can also describe exactly what it means by using Link Relations. There is a set of registered Link Relations that is clearly defined (like `self` or `next`) and more can be registered. But the standard also allows for custom Link Relations to be defined, which do not need to be registered. These custom Link Relations use a URI as name, with the intent that documentation describing that Link Relation is provided at that URI. The use of something like [Compact URIs](https://en.wikipedia.org/wiki/CURIE) can also be used to keep the Link Relation readable despite being a URI.

### Technical ADR rules

All tests for the linter and validator have passed, except the HTTPS tests. As explained alongside the results, the HTTPS tests can easily pass those once deployed to a production environment and it was not worth the time and effort of setting this up for this Proof-of-Concept. But while I could make the other tests pass, I don't agree with all of them and I provide my feedback on them below.

#### [/core/no-trailing-slash](https://gitdocumentatie.logius.nl/publicatie/api/adr/#/core/no-trailing-slash)

The reason for this rule is the avoid confusion and ambiguity in the context of an API when you can access it both with and without a trailing slash. So you could access the API with either `http://localhost/resources` or `http://localhost/resources/` and both would work exactly the same. The confusion seems to refer to not knowing whether to include the trailing slash or not and this rule attempts to avoid that confusion by requiring that URIs with a trailing slash would lead to a 404 status code. But according to the [URI specification](https://www.rfc-editor.org/rfc/rfc3986#section-6.2.3), these 2 URIs should be considered equivalent. So there should be no confusion or ambiquity as both URIs are the same, only their string representation is different. There are also more string representations of this same URI that are not covered by this rule, like `http://localhost:80/resources` or http://localhost:/resources`. 

This rule also violates the URI specification since URIs that are equivalent do not have the same effect. This can cause compatibility issues with software tooling, depending on whether the default string representation of a URI adds a trailing slash or not. It also means that standard URI tooling cannot be used for comparison of URIs, or additional checks have to be added on top. In my opinion, the possible confusion and ambiguity does not weigh up against the violation of the URI standard and the incompatibility issues with tooling.

#### [/core/uri-version](https://gitdocumentatie.logius.nl/publicatie/api/adr/#/core/uri-version)

The reason for this rule is to allow the exploration of multiple versions of an API. By doing this, this seems to violate the functional rule `/core/hide-implementation` by showing an irrelevant implementation detail, i.e. the API version number. It also causes confusion about what the URI to an API resource actually is. Since `http://localhost/v1/resources/1234` would refer to the same API resource as `http://localhost/v2/resources/1234`, just in different versions of the API. The recommendation from the W3C is [to assign a distinct URI to distinct resource](https://www.w3.org/TR/2004/REC-webarch-20041215/#id-resources), and that we [should not associate arbitrarily different URIs with the same resource](https://www.w3.org/TR/2004/REC-webarch-20041215/#uri-aliases).

Aside from the theoretical aspect, the practical consequence of this rule is that every client must be updated whenever a new version of the API is released. Even if the breaking changes in the API have nothing to do with the functionality that this client is using from the API. Another practical consequence is that the entire API codebase for the older version must be maintained alongside the newer version. This also risks a lot of double work, when changes need to happen in both the old version and the new one, like security fixes. Of course, this aspect can be mitigated somewhat by how the API is implemented. 

A better alternative to achieve what this rule is for, is to apply versioning to the API resource instead of the API's URI. The API resource should be described by a media type that includes version information, which could be a custom JSON media type like `application/x.custom-resource-v1+json`. If a new major version is needed for the API resource, you would increment the version in the media type, e.g. `application/x.custom-resource-v2+json`. You can now use [HTTP Content Negotiation](https://developer.mozilla.org/en-US/docs/Web/HTTP/Content_negotiation) with the `Accept` HTTP header to negotiate between these 2 versions from the exact same URI. Since HTTP Content Negotiation is standardized, this functionality is often already included in API tooling. This provides a much more flexible approach to keeping multiple versions of an API resource available in the API. 

#### [/core/version-header](https://gitdocumentatie.logius.nl/publicatie/api/adr/#/core/version-header)

The reason for this rule is to provide the full API version number, as opposed to just the major version number in the URL. As mentioned above, I think versioning makes more sense on the API resource and not the whole API. I do understand that it can be useful to provide a version number for the API as a whole, as it indicates if any changes have been made to it. But the OpenAPI Specification already provides [a suitable place to document the version of the API](https://spec.openapis.org/oas/latest.html#fixed-fields-0), i.e. the `info.version` field. This is intended to document either the version of the OpenAPI Specification document itself or of the API it describes. I am suggesting to use it for the latter, of course. 

So the OpenAPI Specification already provides a location for the API version number. Using that would also avoid the work of documenting the `API-Version` HTTP header in the OpenAPI Specification document as well as actually adding that HTTP header to every API response.

#### RFC 7807

The use of RFC 7807 is checked in the ADR Validator but is not a rule documented in the ADR. As such, the reason for it is unclear. But RFC 7807 defines a machine-readable format for describing problem details. It has actually been updated and obsoleted by RFC 9457 though it doesn't seem to have changed significantly. The goal of this specification is to provide more detailed information about what went wrong, beyond what the HTTP status codes can convey. So if a `400 Bad Request` is returned, the response can include the details about why it went wrong. And this specification provides a way to describe those details in a machine-readable way. That allows those details to be parsed by the client and used in its software, most likely to provide it as information to the user on what went wrong. 

I agree that providing details about what went wrong is important and should always be done. But I would not recommend doing that in a machine-readable way. That tends to put the responsibility for validity checks on the server and makes it easier for clients to avoid implementing such checks themselves. In the context of APIs, the client should have its own checks for anything that it can check as it allows for quicker feedback towards the user when something fails those checks. With machine-readable problem details, it is easier to shift the responsibility for those checks to the server. This would result in a more traffic to the API and higher resource usage.

Another problem with machine-readable problem details returned by the API, is that those problem details are more likely to be shown to a user. But the problem details returned by an API are not necessarily useful to an end-user. The client might have a slightly different context than the API itself, so what that problem in the API means to the user could be slightly different. Aside from this, the description of the problem may not be in the user's native language. This introduces a problem with translating these problem details. In general, it makes more sense to ensure that everything that is shown to users is clearly defined by the client.

If the machine-readable problem details are not used to avoid checks in the client or to provide those details to the user, the main use case for these problem details would be to specify additional types of problems. More detailed problems than the HTTP status code they are returned with. But problems for which the client has a solution implemented would be known beforehand and should not have occurred. And problems for which the client has no solution implemented could not be solved by knowing the problem details. The problem would need to be checked by a human, like the user troubleshooting in their browser or the developer debugging the code. So the problem details would only be useful when a human is looking at it. Making the problem details machine-readable actually makes it harder for human to read. So instead, the problem details should just be returned in human-readable form, like plain text or HTML. Though it should still have the same level of detail, describing exactly what went wrong. In human-readable form, there is no need to standardize how the problem is described, as long as it is clear and understandable.
