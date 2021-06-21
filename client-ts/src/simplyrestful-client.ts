import { OpenAPIV3 } from "openapi-types";
import { stringify as uuidStringify } from 'uuid';
import { HalCollectionV2 } from './HalCollectionV2.ts'

export class SimplyRESTfulClient<T extends HalResource<string, unknown>> {
    private baseApiUri : URL;
    private resourceProfile : URL;
    private resourceMediaType : string;
    private resourceUriTemplate : string;
    totalAmountOfLastRetrievedCollection : number = -1;

    constructor(baseApiUri : URL, resourceProfile : URL) {
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
    setResourceUriTemplate(resourceUriTemplate : string){
        this.resourceUriTemplate = resourceUriTemplate;
    }

    async discoverApi(httpHeaders: Headers) {
        if(!!this.resourceUriTemplate){
            return;
        }
        // FIXME the ".toString()" part in fetch can be removed once a new jest-fetch-mock release is available (after 2021-03-31).
        // See https://github.com/jefflau/jest-fetch-mock/pull/193
        const openapiSpecUrl = await fetch(this.baseApiUri.toString()).then(response => {
            if(!response.ok){
                throw new Error(`The client could not access the API at ${this.baseApiUri}`);
            }
            return response.json().then(serviceDocument => {
                return serviceDocument["_links"]["describedBy"]["href"];
            })
        })
        const openapiSpec = await fetch(openapiSpecUrl)
        .then(response => {
            if (!response.ok){
                throw new Error(`The client could not retrieve the OpenAPI Specification document at ${openapiSpecUrl}`);
            }
            return response.json() as OpenAPIV3.Document;
        });
        for (const path in openapiSpec.paths){
            if(openapiSpec.paths[path].get){
                const content = openapiSpec.paths[path].get.responses.default.content;
                const mediaType = `${this.resourceMediaType};profile="${this.resourceProfile}"`;
                for (const contentType in content){
                    const contentTypeNoSpaces = contentType.replace(" ", "");
                    if(contentTypeNoSpaces === mediaType){
                        this.resourceUriTemplate = decodeURI(new URL(path, this.baseApiUri).toString());
                        return;
                    }
                }
            }
        }
    }

    async list(pageStart: number, pageSize: number, fields: List<string> , query: string, sort: List<{field: string, ascending: boolean}>, httpHeaders: Headers, additionalQueryParameters: URLSearchParams) : List<T> {
        await this.discoverApi(httpHeaders);
        const resourceListUri = this.resolveResourceUriTemplate();

        let searchParams = new URLSearchParams();
        if(!!pageStart){
            searchParams.append("pageStart", pageStart);
        }
        if(!!pageSize){
            searchParams.append("pageSize", pageSize);
        }
        if(!!fields){
            searchParams.append("fields", fields);
        }
        if(!!query){
            searchParams.append("query", query);
        }
        if(!!sort){
            let sortParameters = [];
            sort.forEach(field => {
                sortParameters.push(`${field.name}:${field.ascending ? "asc" : "desc"}`);
            });
            searchParams.append("sort", sortParameters);
        }
        if(!!additionalQueryParameters){
            additionalQueryParameters.forEach( (paramValue, paramName) => {
                searchParams.append(paramName, paramValue);
            });
        }
        resourceListUri.search = searchParams;

        if(!httpHeaders){
            httpHeaders = new Headers();
        }
        httpHeaders.append("Accept", "application/hal+json; profile=\"https://arucard21.github.io/SimplyRESTful-Framework/HALCollection/v2\"");

        // FIXME the ".toString()" part in fetch can be removed once a new jest-fetch-mock release is available (after 2021-03-31).
        // See https://github.com/jefflau/jest-fetch-mock/pull/193
        return fetch(resourceListUri.toString(), {headers: httpHeaders}).then(response => {
            if(!response.ok){
                throw new Error("failed to read");
            }
            return response.json() as HalCollectionV2;
        }).then(collection => {
            this.totalAmountOfLastRetrievedCollection = !collection.total ? -1 : collection.total;
            if(!collection._embedded || !collection._embedded.item){
                return [];
            }
            return collection._embedded.item;
        })
    }

    async read(resourceIdentifier: URL, httpHeaders: Headers, queryParameters: URLSearchParams) : T {
        await this.discoverApi(httpHeaders);
        resourceIdentifier.search = queryParameters;

        if(!httpHeaders){
            httpHeaders = new Headers();
        }
        httpHeaders.append("Accept", "application/hal+json");

        // FIXME the ".toString()" part in fetch can be removed once a new jest-fetch-mock release is available (after 2021-03-31).
        // See https://github.com/jefflau/jest-fetch-mock/pull/193
        return fetch(resourceIdentifier.toString(), {headers: httpHeaders}).then(response => {
            if(!response.ok){
                throw new Error("failed to read");
            }
            return response.json() as T;
        })
    }

    async readWithUuid(resourceUuid: v4, httpHeaders: Headers, queryParameters: URLSearchParams) : T {
        await this.discoverApi(httpHeaders);
        const resourceUri = this.resolveResourceUriTemplate(resourceUuid);
        return this.read(resourceUri, httpHeaders, queryParameters);
    }

    private resolveResourceUriTemplate(resourceUuid: v4): URL {
        if(!resourceUuid){
            return new URL(this.resourceUriTemplate.replace(/{[^}]*}/, ""));    
        }
        return new URL(this.resourceUriTemplate.replace(/{[^}]*}/, uuidStringify(resourceUuid)));
    }
}