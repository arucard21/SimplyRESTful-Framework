package simplyrestful.api.framework.core.exceptions;

public class InvalidSelfLinkException extends InvalidResourceException{
	/**
	 *
	 */
	private static final long serialVersionUID = -4492326488567095868L;
	
	public InvalidSelfLinkException() {
		this("The provided resource contains a self-link which invalid, possibly because the ID it contains is not a URI");
	}

	public InvalidSelfLinkException(String errorMessage){
		super(errorMessage);
	}
}
