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
The first warning is simply a consequence of the dev environment and should be resolved on any normal deployment.
The second warning is due to the naming of one of the Java classes, and renaming it would be a breaking change.

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
API-51   No      CORS policy is invalid
API-56   Yes     
API-57   Yes     

Validation failed
```
There is currently nothing configured for CORS in the API so it is expected to fail that test.
