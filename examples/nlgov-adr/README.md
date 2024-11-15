# NLGov API Design Rules (ADR) example
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg?style=plastic)](https://opensource.org/licenses/Apache-2.0)

Example API that adheres to the [NLGov REST API Design Rules (ADR)](https://logius-standaarden.github.io/API-Design-Rules/). This is linted and validated with the ADR tooling to see how closely SimplyRESTful can adhere to these rules and how that would work.

## Linter results
This is the output from the Spectral linter with the ADR ruleset applied.

```shell
$ spectral lint -r https://developer.overheid.nl/static/adr/ruleset.yaml http://localhost:8080/v0/openapi.json

http://localhost:8080/v0/openapi.json
  1:202  warning  servers-use-https  Server URL http://localhost:8080/v0 "http://localhost:8080/v0" must match the pattern "^https://.*".  servers[0].url
 1:5786  warning  schema-camel-case  Schema name should be CamelCase in #/components/schemas/APICollectionExampleResource                  components.schemas.APICollectionExampleResource

✖ 2 problems (0 errors, 2 warnings, 0 infos, 0 hints)
```
The first warning is simply a consequence of the dev environment where this is tested and should be resolved on any normal deployment.
The second warning is due to the naming of one of the Java classes, and renaming it would be a breaking change. It does not seem important enough to change for this.

The following changes were needed to achieve these results.
* Added an OpenAPI configuration file for general information that should be included in the OpenAPI Specification document.
* Added more OpenAPI annotations to the default implementations in the framework
* Added OpenAPI annotations in the Web Resource for information specific to its API resource
* Created a OpenAPI filter to provide the API-Version HTTP header in each response documented in the OpenAPI Specification

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
All the tests from the validator passed. 

The following changes were needed to achieve these results.
* Created a JAX-RS filter to provide the API-Version HTTP header
* Enabled Spring CORS functionality with a policy that allows all origins, methods, and headers.
* Created a JAX-RS filter to enforce that any request with a trailing slash returns a 404, instead of being considered equivalent to not having a trailing slash.

