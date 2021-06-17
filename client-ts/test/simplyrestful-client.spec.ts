import { SimplyRESTfulClient } from '../src/simplyrestful-client.ts';
import { TestResource } from './TestResource.ts';

test('discoverApi is called correctly', () => {
    console.log = jest.fn();
    const client = new SimplyRESTfulClient<TestResource>(TestResource, "");
    client.discoverApi();
    expect(console.log).toHaveBeenCalledWith("test");
});