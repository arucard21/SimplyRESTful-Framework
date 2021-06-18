import { SimplyRESTfulClient } from '../src/simplyrestful-client.ts';
import { TestResource } from './TestResource.ts';
import fetchMock from 'jest-fetch-mock'

test('discoverApi is called correctly', async () => {
    fetchMock.mockResponse("{}");
    console.log = jest.fn();
    const client = new SimplyRESTfulClient<TestResource>(TestResource, new URL("http://localhost:8888"), "");
    await client.discoverApi();
    // expect(console.log).toHaveBeenCalledWith("test");
});