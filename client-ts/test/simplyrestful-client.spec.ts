import { SimplyRESTfulClient } from '../src/simplyrestful-client.ts';
import { HalLink } from 'hal-types';
import { TestResource } from './TestResource.ts';
import { parse as uuidParse } from 'uuid';
import fetchMock from 'jest-fetch-mock';

let testResourceClient : SimplyRESTfulClient<TestResource>;

beforeAll(() => {
    fetchMock.enableMocks();
    testResourceClient = new SimplyRESTfulClient(
        new URL("http://localhost"),
        new URL("https://arucard21.github.io/SimplyRESTful-Framework/TestResource/v1"));
});

beforeEach(() => {
    fetchMock.resetMocks();
});

test('discoverApi correctly discovers the resource URI for this resource', async () => {
    fetchMock.mockResponses(
        [
            JSON.stringify({
                _links: {
                    describedBy: {
                        href: "http://localhost/openapi.json"
                    }}}),
            { status: 200 }
        ],
        [
            JSON.stringify({
                paths: {
                    "/testresources/{id}": {
                        get: {
                            responses: {
                                default: {
                                    content: {
                                        "application/hal+json;profile=\"https://arucard21.github.io/SimplyRESTful-Framework/TestResource/v1\"": {
                                            schema: {}
                                        }}}}}}}}), 
            { status: 200 }
        ]);
    await testResourceClient.discoverApi();
    expect(testResourceClient.resourceUriTemplate).toBe("http://localhost/testresources/{id}");
});

test('list correctly retrieves the list of resources', async () => {
    const pageSize = 3;
    const selfLink = "http://localhost/testresources/?pageStart=9&pageSize=" + pageSize;
    const firstLink = "http://localhost/testresources/?pageStart=0&pageSize=" + pageSize;
    const lastLink = "http://localhost/testresources/?pageStart=15&pageSize=" + pageSize;
    const prevLink = "http://localhost/testresources/?pageStart=6&pageSize=" + pageSize;
    const nextLink = "http://localhost/testresources/?pageStart=12&pageSize=" + pageSize;
    const total = 17;
    const selfLink0 = "http://localhost/testresources/00000000-0000-0000-0000-000000000000";
    const selfLink1 = "http://localhost/testresources/00000000-0000-0000-0000-000000000001";
    const selfLink2 = "http://localhost/testresources/00000000-0000-0000-0000-000000000002";
    const additionalFieldValue0 = "test value 0";
    const additionalFieldValue1 = "test value 1";
    const additionalFieldValue2 = "test value 2";

    fetchMock.mockResponse(JSON.stringify(
        {
            _links: {
                self: {href: selfLink},
                first: {href: firstLink},
                last: {href: lastLink},
                prev: {href: prevLink},
                next: {href: nextLink}
            },
            total: total,
            _embedded: {
                item: [
                    {_links: {self: {href: selfLink0}}, additionalField: additionalFieldValue0 },
                    {_links: {self: {href: selfLink1}}, additionalField: additionalFieldValue1 },
                    {_links: {self: {href: selfLink2}}, additionalField: additionalFieldValue2 }
                ]
            }
        }));
    expect(testResourceClient.totalAmountOfLastRetrievedCollection).toBe(-1);
    const retrievedListOfResources : List<TestResource> = await testResourceClient.list(new URL("http://localhost/testresources/"));
    expect(testResourceClient.totalAmountOfLastRetrievedCollection).toBe(total);
    expect(retrievedListOfResources.length).toBe(pageSize);
    expect(retrievedListOfResources[0]._links.self.href).toBe(selfLink0);
    expect(retrievedListOfResources[0].additionalField).toBe(additionalFieldValue0);
    expect(retrievedListOfResources[1]._links.self.href).toBe(selfLink1);
    expect(retrievedListOfResources[1].additionalField).toBe(additionalFieldValue1);
    expect(retrievedListOfResources[2]._links.self.href).toBe(selfLink2);
    expect(retrievedListOfResources[2].additionalField).toBe(additionalFieldValue2);
});

test('list correctly sets the paging query parameters', async () => {
    const resourceListUri = "http://localhost/testresources/";
    fetchMock.mockResponse(JSON.stringify({}));
    const retrievedListOfResources : List<TestResource> = await testResourceClient.list(new URL(resourceListUri));
    expect(fetchMock.mock.calls[0][0]).toBe(resourceListUri);
});

test('read with URL correctly retrieves the resource', async () => {
    const selfLink = "http://localhost/testresources/00000000-0000-0000-0000-000000000000";
    const additionalFieldValue = "test value";
    fetchMock.mockResponse(JSON.stringify({_links: {self: {href: selfLink}}, additionalField: additionalFieldValue }));
    const retrievedResource : TestResource = await testResourceClient.read(new URL("http://localhost/testresources/00000000-0000-0000-0000-000000000000"));
    expect(retrievedResource.additionalField).toBe(additionalFieldValue);
    expect(retrievedResource._links.self.href).toBe(selfLink);
});

test('read with UUID correctly retrieves the resource', async () => {
    const selfLink = "http://localhost/testresources/00000000-0000-0000-0000-000000000000";
    const additionalFieldValue = "test value";
    fetchMock.mockResponse(JSON.stringify({_links: {self: {href: selfLink}}, additionalField: additionalFieldValue }));
    const retrievedResource : TestResource = await testResourceClient.readWithUuid(uuidParse("00000000-0000-0000-0000-000000000000"));
    expect(retrievedResource.additionalField).toBe(additionalFieldValue);
    expect(retrievedResource._links.self.href).toBe(selfLink);
});