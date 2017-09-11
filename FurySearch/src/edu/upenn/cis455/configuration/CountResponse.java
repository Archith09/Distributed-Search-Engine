package edu.upenn.cis455.configuration;

/*
 * Simple wrapper to move word count across network
 */
public class CountResponse {
	
	String word;
	Integer count;
	
	public CountResponse(){}
	
	public CountResponse(String word, Integer count) {
		this.word = word;
		this.count = count;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}
	
	

}
