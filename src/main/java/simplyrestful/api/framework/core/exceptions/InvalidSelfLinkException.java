package simplyrestful.api.framework.core.exceptions;

public class InvalidSelfLinkException extends InvalidResourceException{
	/**
	 *
	 */
	private static final long serialVersionUID = -4492326488567095868L;

	public InvalidSelfLinkException(String errorMessage){
		super(errorMessage);
	}
}
