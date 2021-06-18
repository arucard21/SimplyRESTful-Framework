import { SimplyRESTfulClient } from '../src/simplyrestful-client.ts';
import { ExampleResource } from './ExampleResource.ts';
import { parse as uuidParse } from 'uuid';
import fetchMock from 'jest-fetch-mock';

let exampleResourceClient : SimplyRESTfulClient<ExampleResource>;

beforeAll(() => {
    fetchMock.disableMocks()
    exampleResourceClient = new SimplyRESTfulClient(
        ExampleResource,
        new URL("http://localhost:8888"),
        new URL("https://arucard21.github.io/SimplyRESTful-Framework/ExampleResource/v1"));
});

test('Running API against which to run integration tests is available', async () => {
    fetch(new URL("http://localhost:8888")).then(response => expect(response.ok).toBeTruthy())
});

test('discoverApi correctly discovers the resource URI for this resource', async () => {
    await exampleResourceClient.discoverApi();
    expect(exampleResourceClient.resourceUriTemplate).toBe("http://localhost:8888/resources/{id}");
});

// FIXME The UUID in the URL in the running example API changes after every restart. This should read the URL from the list of resources (when implemented)
test('read with URL correctly retrieves the resource', async () => {
    const retrieved : ExampleResource = await exampleResourceClient.read(new URL("http://localhost:8888/resources/3d931e61-92e8-4d22-b932-61c11847ca94"));
    
    expect(retrieved.description).toBe("This is test resource 0");
    expect(retrieved.complexAttribute.name).toBe("complex attribute of test resource 0");
});

// FIXME The UUID in the running example API changes after every restart. This should read the UUID from the list of resources (when implemented)
test('read with UUID correctly retrieves the resource', async () => {
    const retrieved : ExampleResource = await exampleResourceClient.readWithUuid(uuidParse("3d931e61-92e8-4d22-b932-61c11847ca94"));
    
    expect(retrieved.description).toBe("This is test resource 0");
    expect(retrieved.complexAttribute.name).toBe("complex attribute of test resource 0");
});