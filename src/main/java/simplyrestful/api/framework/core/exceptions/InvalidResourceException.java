package simplyrestful.api.framework.core.exceptions;

public class InvalidResourceException extends Exception{
	public InvalidResourceException(String errorMessage){
		super(errorMessage);
	}
}
