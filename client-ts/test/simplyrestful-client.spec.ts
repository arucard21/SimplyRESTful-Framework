import { SimplyRESTfulClient } from '../src/simplyrestful-client.ts'

test('discoverApi is called correctly', () => {
    console.log = jest.fn();
    const client = new SimplyRESTfulClient();
    client.discoverApi();
    expect(console.log).toHaveBeenCalledWith("test");
});