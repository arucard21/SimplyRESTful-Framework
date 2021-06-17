export class SimplyRESTfulClient<T extends HalResource<string, any>> {
    private resourceTypeName : string;
    private baseApiUri : string;
    private resourceProfile : string;
    private resourceMediaType : string;
    private resourceUri : string;
    private totalAmountOfLastRetrievedCollection : number;

    constructor(resourceType : T&Function, baseUri : string) {
        this.resourceTypeName = resourceType.name;
        this.baseApiUri = baseUri;
    }

    discoverApi() {
        console.log("test");
    }
}