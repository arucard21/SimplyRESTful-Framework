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
	* /core/naming-resources: Use nouns to name resources (API-05)
	* /core/naming-collections: Use plural nouns to name collection resources (API-54)
	* /core/interface-language: Define interfaces in Dutch unless there is an official English glossary available (API-04)
	* /core/hide-implementation: Hide irrelevant implementation details (API-53)
	* /core/http-safety: Adhere to HTTP safety and idempotency semantics for operations (API-01)
	* /core/stateless: Do not maintain session state on the server (API-02)
	* /core/nested-child: Use nested URIs for child resources (API-06)
	* /core/resource-operations: Model resource operations as a sub-resource or dedicated resource (API-10)
	* /core/doc-language: Publish documentation in Dutch unless there is existing documentation in English (API-17)
	* /core/deprecation-schedule: Include a deprecation schedule when deprecating features or versions (API-18)
	* /core/transition-period: Schedule a fixed transition period for a new major API version (API-19)
	* /core/changelog: Publish a changelog for API changes between versions (API-55)
	* /core/geospatial: Apply the geospatial module for geospatial data (New in ADR 2.0.0)
* Technical rules
	* /core/no-trailing-slash: Leave off trailing slashes from URIs (API-48)
		* Tested by Spectral linter ruleset
		* Tested by ADR-Validator
	* /core/http-methods: Only apply standard HTTP methods (API-03)
		* Tested by Spectral linter ruleset
		* Tested by ADR-Validator
	* /core/doc-openapi: Use OpenAPI Specification for documentation (API-16)
		* Tested by Spectral linter ruleset
		* Tested by ADR-Validator
	* /core/publish-openapi: Publish OAS document at a standard location in JSON-format (API-51)
		* Not tested by Spectral linter ruleset
		* Tested by ADR-Validator
	* /core/uri-version: Include the major version number in the URI (API-20)
		* Tested by Spectral linter ruleset
		* Tested by ADR-Validator
	* /core/semver: Adhere to the Semantic Versioning model when releasing API changes (API-56)
		* Not tested by Spectral linter ruleset
		* Tested by ADR-Validator
	* /core/version-header: Return the full version number in a response header (API-57)
		* Tested by Spectral linter ruleset
		* Tested by ADR-Validator
	* /core/transport-security: Apply the transport security module (New in ADR 2.0.0)
		* Tested by Spectral linter ruleset.
		* Tested by ADR-Validator with the `security-tls` ruleset (as opposed to the `core` ruleset)

The Spectral linter ruleset actually tests some rules that aren't explicitly mentioned in the Normative Design Rules of the ADR. These are the additional rules in the ruleset.

* The path names must be kebab-case.
* The schema names must be PascalCase (though this documented as "CamelCase" in the ruleset).
* The schema property names must be camelCase (though this is documented as "lower-case" in the ruleset).
* Any 4xx or 5xx response body must conform to [RFC 7807](https://www.rfc-editor.org/rfc/rfc7807) which defines a machine-readable format for describe problem details (though this RFC has been updated and obsoleted by [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457) in July 2023).
