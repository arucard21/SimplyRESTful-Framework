package simplyrestful.api.framework.core.exceptions;

public class InvalidSelfLinkException extends InvalidResourceException{
	public InvalidSelfLinkException(String errorMessage){
		super(errorMessage);
	}
}
