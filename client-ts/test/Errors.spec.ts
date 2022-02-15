import { WebApplicationError, RedirectionError, ClientError, ServerError, fromResponse, NotFoundError, BadGatewayError, fromStatus, BadRequestError, NotAuthorizedError, ForbiddenError, NotAllowedError, NotAcceptableError, NotSupportedError, InternalServerError, NotImplementedError, ServiceUnavailableError, GatewayTimeoutError } from "../src/Errors";
import fetchMock from 'jest-fetch-mock';

beforeAll(() => {
    fetchMock.enableMocks();
});

test('WebApplicationError can be created correctly with the minimal parameters provided', () => {
	const status = 400;
	const error = new WebApplicationError(status);
	expect(error.status).toBe(status);
	expect(error.message).toBeUndefined;
	expect(error.cause).toBeUndefined;
	expect(error.response).toBeUndefined;
});

test('WebApplicationError can be created correctly with all parameters provided', () => {
	const status = 400;
	const message = "custom message";
	const causeMessage = "custom cause message";
	const cause = new Error(causeMessage);
	const body = new Blob();
	const init = { "status" : status , "statusText" : "Not Found" };
	const response = new Response(body, init);

	const error = new WebApplicationError(status, message, cause, response);
	expect(error.status).toBe(status);
	expect(error.message).toBe(message);
	expect(error.cause).toBe(cause);
	expect(error.cause.message).toBe(causeMessage);
	expect(error.response).toBe(response);
});

test('WebApplicationError throws an error if the status is below 300', () => {
	expect(() => new WebApplicationError(201)).toThrowError();
});

test('WebApplicationError throws an error if the status is 600 or above', () => {
	expect(() => new WebApplicationError(600)).toThrowError();
});

test('RedirectionError can be created correctly with all parameters provided', () => {
	const status = 300;
	const location = "http://localhost/incorrect-location";
	const message = "custom message";
	const causeMessage = "custom cause message";
	const cause = new Error(causeMessage);
	const body = new Blob();
	const init = { "status" : status , "statusText" : "Multiple Choices" };
	const response = new Response(body, init);

	const error = new RedirectionError(status, location, message, cause, response);
	expect(error.status).toBe(status);
	expect(error.location).toBe(location);
	expect(error.message).toBe(message);
	expect(error.cause).toBe(cause);
	expect(error.cause.message).toBe(causeMessage);
	expect(error.response).toBe(response);
});

test('RedirectionError throws an error if the status is below 300', () => {
	expect(() => new RedirectionError(201, "http://localhost")).toThrowError();
});

test('RedirectionError throws an error if the status is 400 or above', () => {
	expect(() => new RedirectionError(400, "http://localhost")).toThrowError();
});

test('ClientError can be created correctly with all parameters provided', () => {
	const status = 400;
	const message = "custom message";
	const causeMessage = "custom cause message";
	const cause = new Error(causeMessage);
	const body = new Blob();
	const init = { "status" : status , "statusText" : "Not Found" };
	const response = new Response(body, init);

	const error = new ClientError(status, message, cause, response);
	expect(error.status).toBe(status);
	expect(error.message).toBe(message);
	expect(error.cause).toBe(cause);
	expect(error.cause.message).toBe(causeMessage);
	expect(error.response).toBe(response);
});

test('ClientError throws an error if the status is below 400', () => {
	expect(() => new ClientError(301)).toThrowError();
});

test('ClientError throws an error if the status is 500 or above', () => {
	expect(() => new ClientError(500)).toThrowError();
});

test('ServerError can be created correctly with all parameters provided', () => {
	const status = 500;
	const message = "custom message";
	const causeMessage = "custom cause message";
	const cause = new Error(causeMessage);
	const body = new Blob();
	const init = { "status" : status , "statusText" : "Internal Server Error" };
	const response = new Response(body, init);

	const error = new ServerError(status, message, cause, response);
	expect(error.status).toBe(status);
	expect(error.message).toBe(message);
	expect(error.cause).toBe(cause);
	expect(error.cause.message).toBe(causeMessage);
	expect(error.response).toBe(response);
});

test('ServerError throws an error if the status is below 500', () => {
	expect(() => new ServerError(401)).toThrowError();
});

test('ServerError throws an error if the status is 600 or above', () => {
	expect(() => new ServerError(600)).toThrowError();
});

test('fromResponse correctly creates a NotFoundError for a 404 status code', () => {
	const status = 404;
	const message = "custom message";
	const body = new Blob();
	const init = { "status" : status , "statusText" : message };
	const response = new Response(body, init);

	const error = fromResponse(response);
	expect(error).toBeInstanceOf(NotFoundError);
	expect(error.status).toBe(status);
	expect(error.message).toBe(message);
	expect(error.cause).toBeUndefined();
	expect(error.response).toBe(response);
});

test('fromResponse correctly creates a BadGatewayError for a 502 status code', () => {
	const status = 502;
	const message = "custom message";
	const body = new Blob();
	const init = { "status" : status , "statusText" : message };
	const response = new Response(body, init);

	const error = fromResponse(response);
	expect(error).toBeInstanceOf(BadGatewayError);
	expect(error.status).toBe(status);
	expect(error.message).toBe(message);
	expect(error.cause).toBeUndefined();
	expect(error.response).toBe(response);
});

test('fromStatus correctly creates a BadGatewayError for a 502 status code and ignores the status and message from the provided response', () => {
	const status = 502;
	const message = "custom message";
	const body = new Blob();
	const responseStatus = 404;
	const responseMessage = "Not Found"
	const init = { "status" : responseStatus , "statusText" : responseMessage };
	const response = new Response(body, init);

	const error = fromStatus(status, undefined, message, undefined, response);
	expect(error).toBeInstanceOf(BadGatewayError);
	expect(error.status).toBe(status);
	expect(error.status).not.toBe(responseStatus);
	expect(error.message).toBe(message);
	expect(error.message).not.toBe(responseMessage);
	expect(error.cause).toBeUndefined();
	expect(error.response).toBe(response);
});

test('fromStatus correctly creates a RedirectionError for a 301 status code when the location is also provided', () => {
	const status = 301;
	const location = "http://localhost/redirected-location";

	const error = fromStatus(status, location);
	expect(error).toBeInstanceOf(RedirectionError);
	expect(error.status).toBe(status);
	expect(error.message).toBe("Moved Permanently");
});

test('fromStatus throws an error when the status code is in below 300 as this does not imply an error', () => {
	expect(() => fromStatus(299)).toThrowError();
});

test('fromStatus throws an error when the status code is in below 300 as this does not imply an error', () => {
	expect(() => fromStatus(600)).toThrowError();
});

test('fromStatus throws an error when the status code is in 3xx range but no location was provided', () => {
	expect(() => fromStatus(301)).toThrowError();
});

test('BadRequestError can be created correctly with the minimal parameters provided', () => {
	const error = new BadRequestError();
	expect(error.status).toBe(400);
});

test('NotAuthorizedError can be created correctly with the minimal parameters provided', () => {
	const error = new NotAuthorizedError();
	expect(error.status).toBe(401);
});

test('ForbiddenError can be created correctly with the minimal parameters provided', () => {
	const error = new ForbiddenError();
	expect(error.status).toBe(403);
});

test('NotFoundError can be created correctly with the minimal parameters provided', () => {
	const error = new NotFoundError();
	expect(error.status).toBe(404);
});

test('NotAllowedError can be created correctly with the minimal parameters provided', () => {
	const error = new NotAllowedError();
	expect(error.status).toBe(405);
});

test('NotAcceptableError can be created correctly with the minimal parameters provided', () => {
	const error = new NotAcceptableError();
	expect(error.status).toBe(406);
});

test('NotSupportedError can be created correctly with the minimal parameters provided', () => {
	const error = new NotSupportedError();
	expect(error.status).toBe(415);
});

test('InternalServerError can be created correctly with the minimal parameters provided', () => {
	const error = new InternalServerError();
	expect(error.status).toBe(500);
});

test('NotImplementedError can be created correctly with the minimal parameters provided', () => {
	const error = new NotImplementedError();
	expect(error.status).toBe(501);
});

test('BadGatewayError can be created correctly with the minimal parameters provided', () => {
	const error = new BadGatewayError();
	expect(error.status).toBe(502);
});

test('ServiceUnavailableError can be created correctly with the minimal parameters provided', () => {
	const error = new ServiceUnavailableError();
	expect(error.status).toBe(503);
});

test('GatewayTimeoutError can be created correctly with the minimal parameters provided', () => {
	const error = new GatewayTimeoutError();
	expect(error.status).toBe(504);
});
