import { SimplyRESTfulClient } from '../src/simplyrestful-client.ts';
import { TestResource } from '../test/TestResource.ts';
import 'isomorphic-fetch';

test('Running API against which to run integration tests is available', async () => {
    fetch(new URL("http://localhost:8888")).then(response => expect(response.ok).toBeTruthy())
});

test('integration test is run correctly', async () => {
    const client = new SimplyRESTfulClient(TestResource, new URL("http://localhost:8888"), "");
    await client.discoverApi();
});