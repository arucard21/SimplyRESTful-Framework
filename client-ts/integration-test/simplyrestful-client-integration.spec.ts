import { SimplyRESTfulClient } from '../src/simplyrestful-client.ts';
import { TestResource } from '../test/TestResource.ts';

test('integration test is run correctly', () => {
    const client = new SimplyRESTfulClient<TestResource>(TestResource, "http://localhost:8888", "");
    client.discoverApi();
});