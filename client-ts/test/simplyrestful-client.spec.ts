import { SimplyRESTfulClient } from '../src/simplyrestful-client.ts';
import { HalLink } from 'hal-types';
import { TestResource } from './TestResource.ts';
import { parse as uuidParse } from 'uuid';
import fetchMock from 'jest-fetch-mock';

let testResourceClient : SimplyRESTfulClient<TestResource>;
const baseUri = "http://localhost/";
const testResourceProfile = "https://arucard21.github.io/SimplyRESTful-Framework/TestResource/v1";
const testResourceMediaType = `application/hal+json;profile="${testResourceProfile}"`;

beforeAll(() => {
    fetchMock.enableMocks();
    testResourceClient = new SimplyRESTfulClient(
        new URL(baseUri),
        new URL(testResourceProfile));
    testResourceClient.setResourceUriTemplate("http://localhost/testresources/{id}");
});

beforeEach(() => {
    fetchMock.resetMocks();
});

test('discoverApi correctly discovers the resource URI for this resource', async () => {
    const openApiUri = "http://localhost/openapi.json";
    const testResourceClientWithDiscovery = new SimplyRESTfulClient(
        new URL(baseUri),
        new URL(testResourceProfile));
    fetchMock.mockResponses(
        [
            JSON.stringify({
                _links: {
                    describedBy: {
                        href: openApiUri
                    }}}),
            { status: 200 }
        ],
        [
            JSON.stringify({
                paths: {
                    "/discoveredtestresources/{id}": {
                        get: {
                            responses: {
                                default: {
                                    content: {
                                        "application/hal+json;profile=\"https://arucard21.github.io/SimplyRESTful-Framework/TestResource/v1\"": {
                                            schema: {}
                                        }}}}}}}}), 
            { status: 200 }
        ]);
    await testResourceClientWithDiscovery.discoverApi();
    expect(testResourceClientWithDiscovery.resourceUriTemplate).toBe("http://localhost/discoveredtestresources/{id}");
    expect(fetchMock.mock.calls[0][0]).toBe(baseUri);
    expect(fetchMock.mock.calls[1][0]).toBe(openApiUri);
});

test('discoverApi throws an error when the API can not be accessed', async () => {
    const testResourceClientWithDiscovery = new SimplyRESTfulClient(
        new URL(baseUri),
        new URL(testResourceProfile));
    fetchMock.mockResponseOnce(null, { status: 500 });
    await expect(testResourceClientWithDiscovery.discoverApi()).rejects.toThrow("The client could not access the API at ");
    expect(fetchMock.mock.calls[0][0]).toBe(baseUri);
});

test('discoverApi throws an error when the OpenAPI Specification URI can not be accessed', async () => {
    const openApiUri = "http://localhost/openapi.json";
    const testResourceClientWithDiscovery = new SimplyRESTfulClient(
        new URL(baseUri),
        new URL(testResourceProfile));
    fetchMock.mockResponses(
        [
            JSON.stringify({
                _links: {
                    describedBy: {
                        href: openApiUri
                    }}}),
            { status: 200 }
        ],
        [
            null, { status: 500 }
        ]);
    await expect(testResourceClientWithDiscovery.discoverApi()).rejects.toThrow("The client could not retrieve the OpenAPI Specification document at ");
    expect(fetchMock.mock.calls[0][0]).toBe(baseUri);
    expect(fetchMock.mock.calls[1][0]).toBe(openApiUri);
});

test('list correctly retrieves the list of resources', async () => {
    const total = 17;
    const selfLink0 = "http://localhost/testresources/00000000-0000-0000-0000-000000000000";
    const selfLink1 = "http://localhost/testresources/00000000-0000-0000-0000-000000000001";
    const selfLink2 = "http://localhost/testresources/00000000-0000-0000-0000-000000000002";
    const additionalFieldValue0 = "test value 0";
    const additionalFieldValue1 = "test value 1";
    const additionalFieldValue2 = "test value 2";
    const item0 = {_links: {self: {href: selfLink0}}, additionalField: additionalFieldValue0 };
    const item1 = {_links: {self: {href: selfLink1}}, additionalField: additionalFieldValue1 };
    const item2 = {_links: {self: {href: selfLink2}}, additionalField: additionalFieldValue2 };

    fetchMock.mockResponse(JSON.stringify(
        {
            total: total,
            _embedded: {
                item: [item0, item1, item2]
            }
        }));
    expect(testResourceClient.totalAmountOfLastRetrievedCollection).toBe(-1);
    const retrievedListOfResources : List<TestResource> = await testResourceClient.list();

    expect(testResourceClient.totalAmountOfLastRetrievedCollection).toBe(total);
    expect(retrievedListOfResources).toContainEqual(item0);
    expect(retrievedListOfResources).toContainEqual(item1);
    expect(retrievedListOfResources).toContainEqual(item2);
    expect(fetchMock.mock.calls[0][0]).toBe("http://localhost/testresources/");
});

test('list correctly sets the paging query parameters', async () => {
    const resourceListUri = "http://localhost/testresources/";
    const pageStart = 10;
    const pageSize = 100;
    const fields = ["fieldA", "fieldB"];
    const query = "fieldA==valueA,(fieldB==ValueB;fieldB==ValueC)";
    const sort = [{name: "fieldA", ascending: true}, {name: "fieldD", ascending: false}];
    const additional = new URLSearchParams({"param1": "value1", "param2": "value2"});
    
    fetchMock.mockResponse(JSON.stringify({}));
    const retrievedListOfResources : List<TestResource> = await testResourceClient.list(pageStart, pageSize, fields, query, sort, undefined, additional);
    
    const actualUri = fetchMock.mock.calls[0][0];
    expect(actualUri).toContain(resourceListUri);
    const actualSearchParams : URLSearchParams = new URL(actualUri).searchParams;
    expect(actualSearchParams.has("pageStart")).toBeTruthy();
    expect(parseInt(actualSearchParams.get("pageStart"))).toEqual(pageStart);
    expect(actualSearchParams.has("pageSize")).toBeTruthy();
    expect(parseInt(actualSearchParams.get("pageSize"))).toEqual(pageSize);
    expect(actualSearchParams.has("fields")).toBeTruthy();
    expect(actualSearchParams.get("fields")).toBe(fields.join(","));
    expect(actualSearchParams.has("query")).toBeTruthy();
    expect(actualSearchParams.get("query")).toBe(query);
    expect(actualSearchParams.has("sort")).toBeTruthy();
    expect(actualSearchParams.get("sort")).toBe("fieldA:asc,fieldD:desc");
    expect(actualSearchParams.has("param1")).toBeTruthy();
    expect(actualSearchParams.get("param1")).toBe("value1");
    expect(actualSearchParams.has("param2")).toBeTruthy();
    expect(actualSearchParams.get("param2")).toBe("value2");
});

test('list correctly sets the HTTP headers', async () => {
    const headers = new Headers({"header1": "value1", "header2": "value2"});
    const expectedHeaders = new Headers({"header1": "value1", "header2": "value2", "Accept": "application/hal+json;profile=\"https://arucard21.github.io/SimplyRESTful-Framework/HALCollection/v2\""});
    fetchMock.mockResponse(JSON.stringify({}));
    const retrievedListOfResources : List<TestResource> = await testResourceClient.list(undefined, undefined, undefined, undefined, undefined, headers, undefined);
    expect(fetchMock.mock.calls[0][1]).toHaveProperty("headers", expectedHeaders);
});

test('list throws an error when a bad request is made (HTTP 400 status)', async () => {
    fetchMock.mockResponse(null, {status: 400});
    await expect(testResourceClient.list()).rejects.toThrow("Failed to list the resource at ");
});

test('create correctly creates the resource', async () => {
    const selfLink = "http://localhost/testresources/00000000-0000-0000-0000-000000000000";
    const additionalFieldValue = "test value";
    fetchMock.mockResponse(null, {status: 201, headers: {Location: selfLink}});
    const newResource : TestResource = {additionalField: additionalFieldValue}
    const newResourceUri : URL = await testResourceClient.create(newResource);
    expect(newResourceUri.toString()).toBe(selfLink);
    expect(fetchMock.mock.calls[0][0]).toBe("http://localhost/testresources/");
});

test('create throws an error when a bad request is made (HTTP 400 status)', async () => {
    fetchMock.mockResponse(null, {status: 400});
    await expect(testResourceClient.create()).rejects.toThrow("Failed to create the new resource");
});

test('create throws an error when the location is not returned', async () => {
    fetchMock.mockResponse(null, {status: 201});
    await expect(testResourceClient.create()).rejects.toThrow("Resource seems to have been created but no location was returned. Please report this to the maintainers of the API");
});

test('read correctly retrieves the resource when provided with a URL', async () => {
    const selfLink = "http://localhost/testresources/00000000-0000-0000-0000-000000000000";
    const additionalFieldValue = "test value";
    fetchMock.mockResponse(JSON.stringify({_links: {self: {href: selfLink}}, additionalField: additionalFieldValue }));
    const retrievedResource : TestResource = await testResourceClient.read(new URL("http://localhost/testresources/00000000-0000-0000-0000-000000000000"));
    expect(retrievedResource.additionalField).toBe(additionalFieldValue);
    expect(retrievedResource._links.self.href).toBe(selfLink);
});

test('readWithUuid correctly retrieves the resource when provided with a UUID', async () => {
    const selfLink = "http://localhost/testresources/00000000-0000-0000-0000-000000000000";
    const additionalFieldValue = "test value";
    fetchMock.mockResponse(JSON.stringify({_links: {self: {href: selfLink}}, additionalField: additionalFieldValue }));
    const retrievedResource : TestResource = await testResourceClient.readWithUuid(uuidParse("00000000-0000-0000-0000-000000000000"));
    expect(retrievedResource.additionalField).toBe(additionalFieldValue);
    expect(retrievedResource._links.self.href).toBe(selfLink);
});

test('read throws an error when a bad request is made (HTTP 400 status)', async () => {
    const resourceUri = "http://localhost/testresources/00000000-0000-0000-0000-000000000000";
    fetchMock.mockResponse(null, {status: 400});
    await expect(testResourceClient.read(new URL(resourceUri))).rejects.toThrow("Failed to read the resource at ");
    expect(fetchMock.mock.calls[0][0]).toBe(resourceUri);
});

test('update correctly updates the resource', async () => {
    const selfLink = "http://localhost/testresources/00000000-0000-0000-0000-000000000000";
    const additionalFieldValue = "test value";
    fetchMock.mockResponse(null, {status: 201, headers: {Location: selfLink}});
    const newResource : TestResource = {_links: {self: {href: selfLink}}, additionalField: additionalFieldValue}
    await expect(testResourceClient.update(newResource)).resolves.not.toThrow();
    const expectedHeaders = new Headers({"Content-Type": testResourceMediaType});
    expect(fetchMock.mock.calls[0][0]).toBe(selfLink);
    expect(fetchMock.mock.calls[0][1]).toHaveProperty("headers", expectedHeaders);
});

test('update throws an error when a bad request is made (HTTP 400 status)', async () => {
    const resourceUri = "http://localhost/testresources/00000000-0000-0000-0000-000000000000";
    fetchMock.mockResponse(null, {status: 400});
    await expect(testResourceClient.update({_links: {self: {href: resourceUri}}})).rejects.toThrow("Failed to update the resource at ");
    expect(fetchMock.mock.calls[0][0]).toBe(resourceUri);
});

test('update throws an error when the resource can not be found (HTTP 404 status)', async () => {
    const resourceUri = "http://localhost/testresources/00000000-0000-0000-0000-000000000000";
    fetchMock.mockResponse(null, {status: 404});
    await expect(testResourceClient.update({_links: {self: {href: resourceUri}}})).rejects.toThrow(" could not be found");
    expect(fetchMock.mock.calls[0][0]).toBe(resourceUri);
});

test('delete correctly deletes the resource when provided with a URL', async () => {
    const selfLink = "http://localhost/testresources/00000000-0000-0000-0000-000000000000";
    const additionalFieldValue = "test value";
    fetchMock.mockResponse(null, {status: 204, headers: {Location: selfLink}});
    const newResource : TestResource = {_links: {self: {href: selfLink}}, additionalField: additionalFieldValue};
    await expect(testResourceClient.delete(new URL(selfLink))).resolves.not.toThrow();
    const expectedHeaders = new Headers({"Content-Type": testResourceMediaType});
    expect(fetchMock.mock.calls[0][0]).toBe(selfLink);
});

test('deleteWithUuid correctly deletes the resource when provided with a UUID', async () => {
    const selfLink = "http://localhost/testresources/00000000-0000-0000-0000-000000000000";
    const additionalFieldValue = "test value";
    fetchMock.mockResponse(null, {status: 204, headers: {Location: selfLink}});
    const newResource : TestResource = {_links: {self: {href: selfLink}}, additionalField: additionalFieldValue};
    await expect(testResourceClient.deleteWithUuid(uuidParse("00000000-0000-0000-0000-000000000000"))).resolves.not.toThrow();
    const expectedHeaders = new Headers({"Content-Type": testResourceMediaType});
    expect(fetchMock.mock.calls[0][0]).toBe(selfLink);
});

test('delete throws an error when a bad request is made (HTTP 400 status)', async () => {
    const resourceUri = "http://localhost/testresources/00000000-0000-0000-0000-000000000000";
    fetchMock.mockResponse(null, {status: 400});
    await expect(testResourceClient.delete(new URL(resourceUri))).rejects.toThrow("Failed to delete the resource at ");
    expect(fetchMock.mock.calls[0][0]).toBe(resourceUri);
});

test('delete throws an error when the resource can not be found (HTTP 404 status)', async () => {
    const resourceUri = "http://localhost/testresources/00000000-0000-0000-0000-000000000000";
    fetchMock.mockResponse(null, {status: 404});
    await expect(testResourceClient.delete(new URL(resourceUri))).rejects.toThrow(" could not be found");
    expect(fetchMock.mock.calls[0][0]).toBe(resourceUri);
});