package edu.upenn.cis455.configuration;

import java.util.HashMap;
import java.util.Map.Entry;

import edu.upenn.cis455.utilities.IndexEntry;

/*
 * Simple wrapper class to return a querie's response to search master
 * 
 */
public class QueryResponse {
	
	private HashMap<String, IndexEntry> result;
	
	public QueryResponse(){}
	
	public QueryResponse(HashMap<String, IndexEntry> result){
		this.result = result;
	}
	
	public HashMap<String, IndexEntry> getResult() {
		return result;
	}

	public void setResult(HashMap<String, IndexEntry> result) {
		this.result = result;
	}
	
	public String toString(){
		
		String html = "";
		for(Entry<String, IndexEntry> entry : this.result.entrySet()){
			html = html + "<p>" + entry.getValue().toString() + "</p>"; 
		}
		
		return html;
	}

	
}
