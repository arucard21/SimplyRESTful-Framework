import { OpenAPIV3 } from "openapi-types";
import { stringify as uuidStringify } from 'uuid';

export class SimplyRESTfulClient<T extends HalResource<string, unknown>> {
    private resourceTypeName : string;
    private baseApiUri : URL;
    private resourceProfile : URL;
    private resourceMediaType : string;
    private resourceUriTemplate : string;
    totalAmountOfLastRetrievedCollection : number = -1;

    constructor(resourceType : T&Function, baseApiUri : URL, resourceProfile : URL) {
        this.resourceTypeName = resourceType.name;
        this.baseApiUri = baseApiUri;
        this.resourceMediaType = "application/hal+json";
        this.resourceProfile = resourceProfile;
    }

    async discoverApi(httpHeaders: Map<string, string>) {
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

    async read(resourceIdentifier: URL, httpHeaders: Map<string, string>) : T {
        if(!this.resourceUriTemplate){
            await this.discoverApi(httpHeaders);
        }
        // FIXME the ".toString()" part in fetch can be removed once a new jest-fetch-mock release is available (after 2021-03-31).
        // See https://github.com/jefflau/jest-fetch-mock/pull/193
        return fetch(resourceIdentifier.toString()).then(response => {
            if(!response.ok){
                throw new Error("failed to read");
            }
            return response.json() as T;
        })
    }

    async readWithUuid(resourceUuid: v4, httpHeaders: Map<string, string>) : T {
        if(!this.resourceUriTemplate){
            await this.discoverApi(httpHeaders);
        }
        const resourceUri = this.resourceUriTemplate.replace(/{[^}]*}/, uuidStringify(resourceUuid))
        return this.read(new URL(resourceUri));
    }
}