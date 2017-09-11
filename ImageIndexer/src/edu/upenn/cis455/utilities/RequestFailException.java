package edu.upenn.cis455.utilities;

public class RequestFailException extends Exception{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public RequestFailException(){
		super();
	}
	
	public RequestFailException(String message){
		super(message);
	}
}
