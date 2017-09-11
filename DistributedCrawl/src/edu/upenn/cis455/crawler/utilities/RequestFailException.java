package edu.upenn.cis455.crawler.utilities;

public class RequestFailException extends Exception{
	
	/**
	 * Utility class for distributed framework
	 */
	private static final long serialVersionUID = 1L;

	public RequestFailException(){
		super();
	}
	
	public RequestFailException(String message){
		super(message);
	}
}
