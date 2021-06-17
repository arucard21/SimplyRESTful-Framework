import fetch from 'node-fetch';

export class SimplyRESTfulClient<T extends HalResource<string, any>> {
    private resourceTypeName : string;
    private baseApiUri : string;
    private resourceProfile : string;
    private resourceMediaType : string;
    private resourceUri : string;
    totalAmountOfLastRetrievedCollection : number = -1;

    constructor(resourceType : T&Function, baseApiUri : string, resourceProfile : string) {
        this.resourceTypeName = resourceType.name;
        this.baseApiUri = baseApiUri;
        this.resourceMediaType = "application/hal+json";
        this.resourceProfile = resourceProfile;
    }

    discoverApi(httpHeaders: Map<string, string>) {
        console.log("test");
        console.log("");
        const response = fetch(this.baseApiUri+"/openapi.json");
        if (response.ok){
            console.log(response.json())
        }
        else {
            console.log(response.ok);
        }
    }
}