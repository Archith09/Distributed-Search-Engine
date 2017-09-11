package edu.upenn.cis455.configuration;

import java.util.HashMap;
import java.util.Map.Entry;

import edu.upenn.cis455.utilities.ImageEntry;

public class ImageResponse {
	
	private HashMap<String, ImageEntry> result;
	
	public ImageResponse(){}
	
	public ImageResponse(HashMap<String, ImageEntry> result){
		this.result = result;
	}
	
	public HashMap<String, ImageEntry> getResult() {
		return result;
	}

	public void setResult(HashMap<String, ImageEntry> result) {
		this.result = result;
	}
	
	public String toString(){
		
		String html = "";
		for(Entry<String, ImageEntry> entry : this.result.entrySet()){
			html = html + "<p>" + entry.getValue().toString() + "</p>"; 
		}
		
		return html;
	}
}
