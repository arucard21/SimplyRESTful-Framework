import { OpenAPIV3 } from "openapi-types";

export class SimplyRESTfulClient<T extends HalResource<string, unknown>> {
    private resourceTypeName : string;
    private baseApiUri : URL;
    private resourceProfile : string;
    private resourceMediaType : string;
    private resourceUri : string;
    totalAmountOfLastRetrievedCollection : number = -1;

    constructor(resourceType : T&Function, baseApiUri : URL, resourceProfile : string) {
        this.resourceTypeName = resourceType.name;
        this.baseApiUri = baseApiUri;
        this.resourceMediaType = "application/hal+json";
        this.resourceProfile = resourceProfile;
    }

    async discoverApi(httpHeaders: Map<string, string>) {
        const openapiSpec = await fetch(new URL("openapi.json", this.baseApiUri))
        .then(response => {
            if (!response.ok){
                console.log("Response was not ok");
                return;
            }
            return response.json() as OpenAPIV3.Document;
        });
        const paths = openapiSpec.paths;
        for (const path in paths){
            if(openapiSpec.paths[path].get){
                console.log("path: " + path);
                console.log(openapiSpec.paths[path].get.responses.default.content);
            }
        }
    }

    read(httpHeaders: Map<string, string>) : T {}
}