import { SimplyRESTfulClient } from '../src/simplyrestful-client.ts';
import { HalLink } from 'hal-types';
import { TestResource } from './TestResource.ts';
import { parse as uuidParse } from 'uuid';
import fetchMock from 'jest-fetch-mock';

let testResourceClient : SimplyRESTfulClient<TestResource>;

beforeAll(() => {
    fetchMock.enableMocks();
    testResourceClient = new SimplyRESTfulClient(
        TestResource,
        new URL("http://localhost"),
        new URL("https://arucard21.github.io/SimplyRESTful-Framework/TestResource/v1"));
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

test('read with URL correctly retrieves the resource', async () => {
    let mockResource = new TestResource();
    const additionalFieldValue = "test value";
    mockResource.additionalField = additionalFieldValue;
    const selfLink = "http://localhost/testresources/00000000-0000-0000-0000-000000000000";
    mockResource._links = {self: {href: selfLink}};
    fetchMock.mockResponse(JSON.stringify(mockResource));
    const retrievedResource : TestResource = await testResourceClient.read(new URL("http://localhost/testresources/00000000-0000-0000-0000-000000000000"));
    expect(retrievedResource.additionalField).toBe(additionalFieldValue);
    expect(retrievedResource._links.self.href).toBe(selfLink);
});

test('read with UUID correctly retrieves the resource', async () => {
    let mockResource = new TestResource();
    const additionalFieldValue = "test value";
    mockResource.additionalField = additionalFieldValue;
    const selfLink = "http://localhost/testresources/00000000-0000-0000-0000-000000000000";
    mockResource._links = {self: {href: selfLink}};
    fetchMock.mockResponse(JSON.stringify(mockResource));
    const retrievedResource : TestResource = await testResourceClient.readWithUuid(uuidParse("00000000-0000-0000-0000-000000000000"));
    expect(retrievedResource.additionalField).toBe(additionalFieldValue);
    expect(retrievedResource._links.self.href).toBe(selfLink);
});