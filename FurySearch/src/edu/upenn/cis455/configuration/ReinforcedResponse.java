package edu.upenn.cis455.configuration;

public class ReinforcedResponse {
	
	String url;
	Integer count;
	
	public ReinforcedResponse(){}
	
	public ReinforcedResponse(String url, Integer count) {
		this.url = url;
		this.count = count;
	}

	public String getWord() {
		return url;
	}

	public void setWord(String url) {
		this.url = url;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}
	
	

}
