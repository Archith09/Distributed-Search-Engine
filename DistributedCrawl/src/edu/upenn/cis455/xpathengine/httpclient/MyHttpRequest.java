package edu.upenn.cis455.xpathengine.httpclient;

import java.net.URL;
import java.util.HashMap;



/**
 *  class to make my own http request
 */

public class MyHttpRequest {

	private URLInfo requestURLInfo;
	private String requestURL;           //convert this to string url and use url info provided by professor
	private String requestmethod;
	private HashMap<String, String> requestinfo = new HashMap<String, String>();
	
	
	public MyHttpRequest(String url, String rmethod){
		this.requestURL = url;
		this.requestmethod = rmethod;
		
	}
	
	public MyHttpRequest(URLInfo url, String rmethod){
		this.requestURLInfo = url;
		this.requestmethod = rmethod;
		
	}

	
	public String getRequestURL() {
		return requestURL;
	}
	
	public URLInfo getRequestURLInfo() {
		return requestURLInfo;
	}


	public void setRequestURLInfo(URLInfo requestURLInfo) {
		this.requestURLInfo = requestURLInfo;
	}

	public void setRequestURL(String requestURL) {
		this.requestURL = requestURL;
	}

	public String getRequestmethod() {
		return requestmethod;
	}

	public void setRequestmethod(String requestmethod) {
		this.requestmethod = requestmethod;
	}

	public HashMap<String, String> getRequestinfo() {
		return requestinfo;
	}

	public void setRequestinfo(HashMap<String, String> requestinfo) {
		this.requestinfo = requestinfo;
	}



}
