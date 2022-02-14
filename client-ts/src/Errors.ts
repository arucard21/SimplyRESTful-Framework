/**
 * Errors matching common HTTP status codes.
 * (Modeled after JAX-RS exceptions)
 *
 * The correct type of Error can be created based on the status code, using the WebApplication.fromStatus() method.
 * This will also verify that the status code is a valid one (according to RFC 7231 and 6585). Such verification does not occur
 * when creating the Error directly.
 */
import { CustomError } from 'ts-custom-error'

export class WebApplicationError extends CustomError {
	public constructor(
		public status: number,
		message?: string,
		public cause?: Error,
		public response?: Response
	) {
		super(message);
	}

	public static fromResponse(response: Response, cause?: Error){
		const location = response.headers.get("Location");
		return this.fromStatus(
			response.status,
			location === null ? undefined : location,
			response.statusText,
			cause,
			response)
	}

	public static fromStatus(status: number, location?: string, message?: string, cause?: Error, response?: Response): WebApplicationError {
        if (status < 300){
            throw new Error("Status code below 300 does not imply an error");
        }
        if (status < 400){
            if (!location){
                throw new Error("When status code is in range 3xx, a location URI must be included");
            }
            else {
                switch(status){
                    case 300: return new RedirectionError(300, location, "Multiple Choices", cause, response);
                    case 301: return new RedirectionError(301, location, "Moved Permanently", cause, response);
                    case 302: return new RedirectionError(302, location, "Found", cause, response);
                    case 303: return new RedirectionError(303, location, "See Other", cause, response);
                    case 304: return new RedirectionError(304, location, "Not Modified", cause, response);
                    case 305: return new RedirectionError(305, location, "Use Proxy", cause, response);
                    case 307: return new RedirectionError(307, location, "Temporary Redirect", cause, response);
                }
            }
        }
		switch(status){
            case 400: return new BadRequestError(message, cause, response);
            case 401: return new NotAuthorizedError(message, cause, response);
            case 402: return new ClientError(402, "Payment Required", cause, response);
            case 403: return new ForbiddenError(message, cause, response);
            case 404: return new NotFoundError(message, cause, response);
            case 405: return new NotAllowedError(message, cause, response);
            case 406: return new NotAcceptableError(message, cause, response);
            case 407: return new ClientError(407, "Proxy Authentication Required", cause, response);
            case 408: return new ClientError(408, "Request Timeout", cause, response);
            case 409: return new ClientError(409, "Conflict", cause, response);
            case 410: return new ClientError(410, "Gone", cause, response);
            case 411: return new ClientError(411, "Length Required", cause, response);
            case 412: return new ClientError(412, "Precondition Failed", cause, response);
            case 413: return new ClientError(413, "Payload Too Large", cause, response);
            case 414: return new ClientError(414, "URI Too Long", cause, response);
            case 415: return new NotSupportedError(message, cause, response);
            case 416: return new ClientError(416, "Range Not Satisfiable", cause, response);
            case 417: return new ClientError(417, "Expectation Failed", cause, response);
			case 426: return new ClientError(426, "Upgrade Required", cause, response);
			case 428: return new ClientError(428, "Precondition Required", cause, response);
			case 429: return new ClientError(429, "Too Many Requests", cause, response);
			case 431: return new ClientError(431, "Request Header Fields Too Large", cause, response);
            case 500: return new InternalServerError(message, cause, response);
            case 501: return new NotImplementedError(message, cause, response);
            case 502: return new BadGatewayError(message, cause, response);
            case 503: return new ServiceUnavailableError(message, cause, response);
            case 504: return new GatewayTimeoutError(message, cause, response);
			case 505: return new ServerError(505, "HTTP Version Not Supported", cause, response);
			case 511: return new ServerError(511, "Network Authentication Required", cause, response);
            default: throw new Error("Status code does not match known HTTP status codes. You can still create the error with this status code directly");
		}
	}
}

export class RedirectionError extends WebApplicationError {
	public constructor(
		status: number,
		public location: string,
		message?: string,
		cause?: Error,
		response?: Response
	) {
		super(status, message, cause, response);
		if (status < 300 || status >= 400){
			throw new Error("Status code for redirect error must be in the 3xx range");
		}
	}
}

export class ClientError extends WebApplicationError {
	public constructor(
		status: number,
		message?: string,
		cause?: Error,
		response?: Response
	) {
		super(status, message, cause, response);
		if (status < 400 || status >= 500){
			throw new Error("Status code for client error must be in the 4xx range");
		}
	}
}

export class BadRequestError extends ClientError {
	public constructor(
		message: string = "Bad Request",
		cause?: Error,
		response?: Response
	) {
		super(400, message, cause, response);
	}
}

export class NotAuthorizedError extends ClientError {
	public constructor(
		message: string = "Unauthorized",
		cause?: Error,
		response?: Response
	) {
		super(401, message, cause, response);
	}
}

export class ForbiddenError extends ClientError {
	public constructor(
		message: string = "Forbidden",
		cause?: Error,
		response?: Response
	) {
		super(403, message, cause, response);
	}
}

export class NotFoundError extends ClientError {
	public constructor(
		message: string = "Not Found",
		cause?: Error,
		response?: Response
	) {
		super(404, message, cause, response);
	}
}

export class NotAllowedError extends ClientError {
	public constructor(
		message: string = "Method Not Allowed",
		cause?: Error,
		response?: Response
	) {
		super(405, message, cause, response);
	}
}

export class NotAcceptableError extends ClientError {
	public constructor(
		message: string = "Not Acceptable",
		cause?: Error,
		response?: Response
	) {
		super(406, message, cause, response);
	}
}

export class NotSupportedError extends ClientError {
	public constructor(
		message: string = "Unsupported Media Type",
		cause?: Error,
		response?: Response
	) {
		super(415, message, cause, response);
	}
}

export class ServerError extends WebApplicationError {
	public constructor(
		status: number,
		message?: string,
		cause?: Error,
		response?: Response
	) {
		super(status, message, cause, response);
		if (status < 500 || status >= 600){
			throw new Error("Status code for server error must be in the 5xx range");
		}
	}
}

export class InternalServerError extends ServerError {
	public constructor(
		message: string = "Internal Server Error",
		cause?: Error,
		response?: Response
	) {
		super(500, message, cause, response);
	}
}

export class NotImplementedError extends ServerError {
	public constructor(
		message: string = "Not Implemented",
		cause?: Error,
		response?: Response
	) {
		super(501, message, cause, response);
	}
}

export class BadGatewayError extends ServerError {
	public constructor(
		message: string = "Bad Gateway",
		cause?: Error,
		response?: Response
	) {
		super(502, message, cause, response);
	}
}

export class ServiceUnavailableError extends ServerError {
	public constructor(
		message: string = "Service Unavailable",
		cause?: Error,
		response?: Response
	) {
		super(503, message, cause, response);
	}
}

export class GatewayTimeoutError extends ServerError {
	public constructor(
		message: string = "Gateway Timeout",
		cause?: Error,
		response?: Response
	) {
		super(504, message, cause, response);
	}
}
