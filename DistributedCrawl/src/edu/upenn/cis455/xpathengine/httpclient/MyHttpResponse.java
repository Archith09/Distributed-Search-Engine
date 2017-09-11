package edu.upenn.cis455.xpathengine.httpclient;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/** class to make my on http response
 * 
 */

public class MyHttpResponse {

	private URLInfo responseURLInfo;
	private String responseURL;
	private String responsecontent;
	private HashMap<String, String> reponseheader = new HashMap<String, String>();
	private Map<String, List<String>> responsemap;	
	public Map<String, List<String>> getResponsemap() {
		return responsemap;
	}
	public void setResponsemap(Map<String, List<String>> responsemap) {
		this.responsemap = responsemap;
	}
	private int responsestatus;
	private ArrayList<String> reponsebodycontent;
	private long lastaccessed;
	
	
	
	public long getLastaccessed() {
		return lastaccessed;
	}
	public void setLastaccessed(long lastaccessed) {
		this.lastaccessed = lastaccessed;
	}
	public ArrayList<String> getReponsebodycontent() {
		return reponsebodycontent;
	}
	public void setReponsebodycontent(ArrayList<String> reponsebodycontent) {
		this.reponsebodycontent = reponsebodycontent;
	}
	public URLInfo getResponseURLInfo() {
		return responseURLInfo;
	}
	public void setResponseURLInfo(URLInfo responseURLInfo) {
		this.responseURLInfo = responseURLInfo;
	}	
	public String getResponseURL() {
		return responseURL;
	}
	public void setResponseURL(String responseURl) {
		this.responseURL = responseURl;
	}
	public String getResponsecontent() {
		return responsecontent;
	}
	public void setResponsecontent(String responsecontent) {
		this.responsecontent = responsecontent;
	}
	public HashMap<String, String> getReponseheader() {
		return reponseheader;
	}
	public void setReponseheader(HashMap<String, String> reponseheader) {
		this.reponseheader = reponseheader;
	}
	public int getResponsestatus() {
		return responsestatus;
	}
	public void setResponsestatus(int responsestatus) {
		this.responsestatus = responsestatus;
	}
	

}
