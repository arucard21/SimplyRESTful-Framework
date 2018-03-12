package simplyrestful.api.framework.core.exceptions;

public class InvalidResourceException extends Exception{
	/**
	 *
	 */
	private static final long serialVersionUID = -4170046634966114763L;

	public InvalidResourceException(String errorMessage){
		super(errorMessage);
	}
}
