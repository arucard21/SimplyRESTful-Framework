package simplyrestful.api.framework.core.exceptions;

import javax.ws.rs.BadRequestException;

public class InvalidResourceException extends BadRequestException{
	/**
	 *
	 */
	private static final long serialVersionUID = -4170046634966114763L;
	
	public InvalidResourceException() {
		this("The provided resource is invalid, most likely due to a missing self-link");
	}

	public InvalidResourceException(String errorMessage){
		super(errorMessage);
	}
}
