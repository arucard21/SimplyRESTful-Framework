import { SimplyRESTfulClient } from '../src/simplyrestful-client.ts';
import { ExampleResource } from './ExampleResource.ts';
import { parse as uuidParse } from 'uuid';
import fetchMock from 'jest-fetch-mock';

let exampleResourceClient : SimplyRESTfulClient<ExampleResource>;

beforeAll(() => {
    fetchMock.disableMocks()
    exampleResourceClient = new SimplyRESTfulClient(
        new URL("http://localhost:8888"),
        new URL("https://arucard21.github.io/SimplyRESTful-Framework/ExampleResource/v1"));
});

beforeEach(() => {
    fetchMock.resetMocks();
});

test('Running API against which to run integration tests is available', async () => {
    fetch(new URL("http://localhost:8888")).then(response => expect(response.ok).toBeTruthy())
});

test('discoverApi correctly discovers the resource URI for this resource', async () => {
    await exampleResourceClient.discoverApi();
    expect(exampleResourceClient.resourceUriTemplate).toBe("http://localhost:8888/resources/{id}");
});

test('list correctly retrieves the list of resources', async () => {
    expect(exampleResourceClient.totalAmountOfLastRetrievedCollection).toBe(-1);
    
    const listOfResources : List<ExampleResource> = await exampleResourceClient.list();
    
    expect(exampleResourceClient.totalAmountOfLastRetrievedCollection).toBe(3);
    expect(listOfResources.length).toBe(3);
    expect(listOfResources[0].description).toBe("This is test resource 0");
    expect(listOfResources[0].complexAttribute.name).toBe("complex attribute of test resource 0");
    expect(listOfResources[1].description).toBe("This is test resource 1");
    expect(listOfResources[1].complexAttribute.name).toBe("complex attribute of test resource 1");
    expect(listOfResources[2].description).toBe("This is test resource 2");
    expect(listOfResources[2].complexAttribute.name).toBe("complex attribute of test resource 2");
});

test('read with URL correctly retrieves the resource', async () => {
    const listOfResources : List<ExampleResource> = await exampleResourceClient.list();
    expect(listOfResources.length).toBeGreaterThan(0);
    const resourceIdentifierFirstResource = new URL(listOfResources[0]._links.self.href);
    const retrieved : ExampleResource = await exampleResourceClient.read(resourceIdentifierFirstResource);
    
    expect(retrieved.description).toBe("This is test resource 0");
    expect(retrieved.complexAttribute.name).toBe("complex attribute of test resource 0");
});

test('read with UUID correctly retrieves the resource', async () => {
    const listOfResources : List<ExampleResource> = await exampleResourceClient.list();
    expect(listOfResources.length).toBeGreaterThan(0);
    const resourceIdentifierFirstResource = new URL(listOfResources[0]._links.self.href);
    const resourceUuidFirstResource = resourceIdentifierFirstResource.pathname.split("/").pop();

    const retrieved : ExampleResource = await exampleResourceClient.readWithUuid(uuidParse(resourceUuidFirstResource));
    
    expect(retrieved.description).toBe("This is test resource 0");
    expect(retrieved.complexAttribute.name).toBe("complex attribute of test resource 0");
});