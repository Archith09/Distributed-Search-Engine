package edu.upenn.cis455.configuration;

/*
 * 
 */
public class CountResponse {
	
	String word;
	Integer count;
	
	public CountResponse(){}
	
	public CountResponse(String word, Integer count) {
		this.word = word;
		this.count = count;
	}

}
