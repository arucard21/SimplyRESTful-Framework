import { OpenAPIV3 } from 'openapi-types';
import { HALResource } from './HALResource';
import { HalCollectionV2 } from './HalCollectionV2'
import { SortOrder } from './SortOrder';

export class SimplyRESTfulClient<T extends HALResource> {
    readonly baseApiUri: URL;
    readonly resourceProfile: URL;
    readonly resourceMediaType: string;
    resourceUriTemplate: string | undefined;
    totalAmountOfLastRetrievedCollection: number = -1;

    constructor(baseApiUri: URL, resourceProfile: URL) {
        this.baseApiUri = baseApiUri;
        this.resourceMediaType = "application/hal+json";
        this.resourceProfile = resourceProfile;
    }

    /*
     Manually set the resource URI template.

     This disables discovering it automatically based on the OpenAPI Specification.
     Changes to the resource URI will not be detected automatically.
     This is only provided as a fallback mechanism, using the discovery mechanism is
     recommended (which happens automatically if this method is never used).
     */
    setResourceUriTemplate(resourceUriTemplate: string) {
        this.resourceUriTemplate = resourceUriTemplate;
    }

    async discoverApi(httpHeaders?: Headers): Promise<void> {
        if (!!this.resourceUriTemplate) {
            return;
        }
        return this.retrieveServiceDocument(httpHeaders)
            .then(openApiSpecificationUrl => this.retrieveOpenApiSpecification(openApiSpecificationUrl, httpHeaders))
            .then(openApiSpecification => this.configureResourceUriTemplate(openApiSpecification));
    }

    private async retrieveServiceDocument(this: this, httpHeaders?: Headers): Promise<URL> {
        return fetch(this.baseApiUri.toString(), { headers: httpHeaders }).then(async response => {
            if (!response.ok) {
                throw new Error(`The client could not access the API at ${this.baseApiUri}`);
            }
            return response.json().then(serviceDocument => {
                return new URL(serviceDocument["_links"]["describedBy"]["href"]);
            })
        });
    }

    private async retrieveOpenApiSpecification(openApiSpecificationUrl: URL, httpHeaders?: Headers): Promise<OpenAPIV3.Document> {
        return fetch(openApiSpecificationUrl.toString(), { headers: httpHeaders }).then(async response => {
            if (!response.ok) {
                throw new Error(`The client could not retrieve the OpenAPI Specification document at ${openApiSpecificationUrl}`);
            }
            return response.json().then((openApiSpecification: OpenAPIV3.Document) => openApiSpecification);
        });
    }

    private async configureResourceUriTemplate(this: this, openApiSpecification: OpenAPIV3.Document): Promise<void> {
        for (const path in openApiSpecification.paths) {
            if (!!path && openApiSpecification?.paths[path]?.get) {
                const content = (openApiSpecification?.paths[path]?.get?.responses?.default as OpenAPIV3.ResponseObject)?.content;
                const mediaType = `${this.resourceMediaType};profile="${this.resourceProfile}"`;
                for (const contentType in content) {
                    const contentTypeNoSpaces = contentType.replace(" ", "");
                    if (contentTypeNoSpaces === mediaType) {
                        this.resourceUriTemplate = decodeURI(new URL(path, this.baseApiUri).toString());
                    }
                }
            }
        }
    }

    async list(pageStart?: number, pageSize?: number, fields?: string[], query?: string, sort?: SortOrder[], httpHeaders?: Headers, additionalQueryParameters?: URLSearchParams): Promise<T[]> {
        return this.discoverApi(httpHeaders).then(() => {
            const resourceListUri = this.resolveResourceUriTemplate();

            let searchParams = new URLSearchParams();
            if (!!pageStart) {
                searchParams.append("pageStart", pageStart.toString());
            }
            if (!!pageSize) {
                searchParams.append("pageSize", pageSize.toString());
            }
            if (!!fields) {
                searchParams.append("fields", fields.join(","));
            }
            if (!!query) {
                searchParams.append("query", query);
            }
            if (!!sort) {
                let sortParameters: string[] = [];
                sort.forEach(field => {
                    sortParameters.push(`${field.fieldName}:${field.ascending ? "asc" : "desc"}`);
                });
                searchParams.append("sort", sortParameters.join(","));
            }
            if (!!additionalQueryParameters) {
                additionalQueryParameters.forEach((paramValue, paramName) => {
                    searchParams.append(paramName, paramValue);
                });
            }
            resourceListUri.search = searchParams.toString();

            if (!httpHeaders) {
                httpHeaders = new Headers();
            }
            httpHeaders.append("Accept", "application/hal+json;profile=\"https://arucard21.github.io/SimplyRESTful-Framework/HALCollection/v2\"");

            return fetch(resourceListUri.toString(), { headers: httpHeaders }).then(response => {
                if (!response.ok) {
                    throw new Error(`Failed to list the resource at ${resourceListUri}`);
                }
                return response.json();
            }).then((collection: HalCollectionV2<T>) => {
                this.totalAmountOfLastRetrievedCollection = !collection.total ? -1 : collection.total;
                if (!collection._embedded || !collection._embedded.item) {
                    return [];
                }
                return collection._embedded.item;
            })
        });
    }

    async create(resource: T, httpHeaders?: Headers, queryParameters?: URLSearchParams): Promise<URL> {
        return this.discoverApi(httpHeaders).then(() => {
            const resourceListUri = this.resolveResourceUriTemplate();
            if (!!queryParameters) {
                resourceListUri.search = queryParameters.toString();
            }

            if (!httpHeaders) {
                httpHeaders = new Headers();
            }
            httpHeaders.append("Content-Type", `application/hal+json;profile="${this.resourceProfile}"`);

            return fetch(resourceListUri.toString(), { method: "POST", headers: httpHeaders, body: JSON.stringify(resource) }).then(response => {
                if (response.status !== 201) {
                    throw new Error("Failed to create the new resource");
                }
                const locationOfCreatedResource = response.headers.get("Location");
                if (!locationOfCreatedResource) {
                    throw new Error("Resource seems to have been created but no location was returned. Please report this to the maintainers of the API");
                }
                return new URL(locationOfCreatedResource);
            });
        });
    }

    async read(resourceIdentifier: URL, httpHeaders?: Headers, queryParameters?: URLSearchParams): Promise<T> {
        return this.discoverApi(httpHeaders).then(() => {
            if (!!queryParameters) {
                resourceIdentifier.search = queryParameters.toString();
            }

            if (!httpHeaders) {
                httpHeaders = new Headers();
            }
            httpHeaders.append("Accept", `application/hal+json;profile="${this.resourceProfile}"`);

            return fetch(resourceIdentifier.toString(), { headers: httpHeaders }).then(response => {
                if (!response.ok) {
                    throw new Error(`Failed to read the resource at ${resourceIdentifier}`);
                }
                return response.json();
            })
        });
    }

    async update(resource: T, httpHeaders?: Headers, queryParameters?: URLSearchParams): Promise<void> {
        return this.discoverApi(httpHeaders).then(() => {
            const selfLink = resource?._links?.self?.href;
            if (!selfLink) {
                throw Error("The update failed because the resource does not contain a valid self link.")
            }
            let resourceIdentifier: URL = new URL(selfLink);
            if (!!queryParameters) {
                resourceIdentifier.search = queryParameters.toString();
            }

            if (!httpHeaders) {
                httpHeaders = new Headers();
            }
            httpHeaders.append("Content-Type", `application/hal+json;profile="${this.resourceProfile}"`);

            return fetch(resourceIdentifier.toString(), { method: "PUT", headers: httpHeaders, body: JSON.stringify(resource) }).then(response => {
                if (!response.ok) {
                    if (response.status === 404) {
                        throw new Error(`Resource at ${resourceIdentifier} could not be found`);
                    }
                    throw new Error(`Failed to update the resource at ${resourceIdentifier}`);
                }
            })
        });
    }

    async delete(resourceIdentifier: URL, httpHeaders?: Headers, queryParameters?: URLSearchParams) {
        return this.discoverApi(httpHeaders).then(() => {
            if (!!queryParameters) {
                resourceIdentifier.search = queryParameters.toString();
            }

            if (!httpHeaders) {
                httpHeaders = new Headers();
            }

            return fetch(resourceIdentifier.toString(), { method: "DELETE", headers: httpHeaders }).then(response => {
                if (response.status !== 204) {
                    if (response.status === 404) {
                        throw new Error(`Resource at ${resourceIdentifier} could not be found`);
                    }
                    throw new Error(`Failed to delete the resource at ${resourceIdentifier}`);
                }
            })
        });
    }

    async readWithUuid(resourceUuid: string, httpHeaders?: Headers, queryParameters?: URLSearchParams): Promise<T> {
        return this.discoverApi(httpHeaders).then(() => {
            const resourceUri = this.resolveResourceUriTemplate(resourceUuid);
            return this.read(resourceUri, httpHeaders, queryParameters);
        });
    }

    async deleteWithUuid(resourceUuid: string, httpHeaders?: Headers, queryParameters?: URLSearchParams): Promise<void> {
        return this.discoverApi(httpHeaders).then(() => {
            const resourceUri = this.resolveResourceUriTemplate(resourceUuid);
            return this.delete(resourceUri, httpHeaders, queryParameters);
        });
    }

    private resolveResourceUriTemplate(resourceUuid?: string): URL {
        if (!this.resourceUriTemplate) {
            throw new Error("The client needs to discover the resource URI template from the API before this method can be used. Use discoverApi() first.");
        }
        if (!resourceUuid) {
            return new URL(this.resourceUriTemplate.replace(/{id}/, ""));
        }
        return new URL(this.resourceUriTemplate.replace(/{id}/, resourceUuid));
    }
}
