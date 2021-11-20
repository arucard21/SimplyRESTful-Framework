import { SimplyRESTfulClient } from '../src/simplyrestful-client';
import { ExampleResource } from './ExampleResource';
import fetchMock from 'jest-fetch-mock';

const hostname = "http://localhost:8888/";
let exampleResourceClient: SimplyRESTfulClient<ExampleResource>;

beforeAll(() => {
    fetchMock.disableMocks()
});

beforeEach(() => {
	fetchMock.resetMocks();
	exampleResourceClient = new SimplyRESTfulClient(hostname, "https://arucard21.github.io/SimplyRESTful-Framework/ExampleResource/v1");
});

test('Running API against which to run integration tests is available', async () => {
	await expect(fetch(hostname)).resolves.not.toThrow();
	await expect(fetch(hostname).then(response => response.ok)).resolves.toBeTruthy();
});

test('Integration tests cannot use relative base URI since fetch has no host against which to resolve it (which it does in a browser)', async () => {
	await expect(fetch("")).rejects.toThrow(new TypeError("Only absolute URLs are supported"));
});

test('discoverApi correctly discovers the resource URI for this resource', async () => {
    await exampleResourceClient.discoverApi();
    expect(exampleResourceClient.resourceUriTemplate).toBe(`${hostname}resources/{id}`);
});

test('list correctly retrieves the list of resources with the default fields visible', async () => {
    expect(exampleResourceClient.totalAmountOfLastRetrievedCollection).toBe(-1);

    const listOfResources: ExampleResource[] = await exampleResourceClient.list();

    expect(exampleResourceClient.totalAmountOfLastRetrievedCollection).toBe(3);
    expect(listOfResources.length).toBe(3);
    expect(listOfResources[0].description).toBeUndefined();
    expect(listOfResources[0].complexAttribute).toBeUndefined();
    expect(listOfResources[1].description).toBeUndefined();
    expect(listOfResources[1].complexAttribute).toBeUndefined();
    expect(listOfResources[2].description).toBeUndefined();
    expect(listOfResources[2].complexAttribute).toBeUndefined();
});

test('list correctly retrieves the list of resources when all fields are requested', async () => {
    expect(exampleResourceClient.totalAmountOfLastRetrievedCollection).toBe(-1);

    const listOfResources: ExampleResource[] = await exampleResourceClient.list({fields:["all"]});

    expect(exampleResourceClient.totalAmountOfLastRetrievedCollection).toBe(3);
    expect(listOfResources.length).toBe(3);
    expect(listOfResources[0].description).toBe("This is test resource 0");
    expect(listOfResources[0].complexAttribute.name).toBe("complex attribute of test resource 0");
    expect(listOfResources[1].description).toBe("This is test resource 1");
    expect(listOfResources[1].complexAttribute.name).toBe("complex attribute of test resource 1");
    expect(listOfResources[2].description).toBe("This is test resource 2");
    expect(listOfResources[2].complexAttribute.name).toBe("complex attribute of test resource 2");
});

test('create correctly creates the resource', async () => {
    const newResource: ExampleResource = { description: "This is a new resource", complexAttribute: { name: "complex attribute of the new resource" } };

    const newResourceUri: string = await exampleResourceClient.create(newResource);

    const createdResource: ExampleResource = await exampleResourceClient.read(newResourceUri);
    expect(createdResource.description).toBe(createdResource.description);
    expect(createdResource.complexAttribute).toBe(createdResource.complexAttribute);

    await expect(exampleResourceClient.delete(newResourceUri)).resolves.not.toThrow();
});

test('read retrieves the resource when the URL is provided', async () => {
    const listOfResources: ExampleResource[] = await exampleResourceClient.list();
    expect(listOfResources.length).toBeGreaterThan(0);
    const resourceIdentifierFirstResource = listOfResources[0]._links.self.href;
    const retrieved: ExampleResource = await exampleResourceClient.read(resourceIdentifierFirstResource);

    expect(retrieved.description).toBe("This is test resource 0");
    expect(retrieved.complexAttribute.name).toBe("complex attribute of test resource 0");
});

test('read correctly retrieves the resource when only the UUID is provided', async () => {
    const listOfResources: ExampleResource[] = await exampleResourceClient.list();
    expect(listOfResources.length).toBeGreaterThan(0);
    const resourceIdentifierFirstResource = new URL(listOfResources[0]._links.self.href);
    const resourceUuidFirstResource = resourceIdentifierFirstResource.pathname.split("/").pop();

    const retrieved: ExampleResource = await exampleResourceClient.readWithUuid(resourceUuidFirstResource);

    expect(retrieved.description).toBe("This is test resource 0");
    expect(retrieved.complexAttribute.name).toBe("complex attribute of test resource 0");
});

test('update correctly updates the resource', async () => {
    const newResource: ExampleResource = { description: "This is a resource created for updating", complexAttribute: { name: "complex attribute of the new resource" } };
    const newResourceUri: string = await exampleResourceClient.create(newResource);
    const createdResource: ExampleResource = await exampleResourceClient.read(newResourceUri);
    expect(createdResource.description).toBe("This is a resource created for updating")
    createdResource.description = "This is a resource that has been updated";

    await expect(exampleResourceClient.update(createdResource)).resolves.not.toThrow();

    const updatedResource: ExampleResource = await exampleResourceClient.read(newResourceUri);
    expect(updatedResource.description).toBe("This is a resource that has been updated");

    await expect(exampleResourceClient.delete(newResourceUri)).resolves.not.toThrow();
});

test('delete correctly deletes the resource', async () => {
    const toBeDeletedResource: ExampleResource = { description: "This is a resource created to be deleted", complexAttribute: { name: "complex attribute of the resource created to be deleted" } };
    const toBeDeletedResourceUri: string = await exampleResourceClient.create(toBeDeletedResource);
    await expect(exampleResourceClient.read(toBeDeletedResourceUri)).resolves.not.toThrow();

    await expect(exampleResourceClient.delete(toBeDeletedResourceUri)).resolves.not.toThrow();

    await expect(exampleResourceClient.read(toBeDeletedResourceUri)).rejects.toThrow("Failed to read the resource at");
});
