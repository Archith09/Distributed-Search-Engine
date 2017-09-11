package edu.upenn.cis455.search;

/*
 * A simple wrapper to send url information to the UI for display
 * 
 * 
 */

public class UrlInformation {

	private String url;
	private String title;
	private String excerpt;
	private String log;
	private long dbTime;
	private long processingTime;
	
	public UrlInformation(String url, String title, String excerpt, String log, long dbTime, long processingTime){
		this.url = url;
		this.title = title;
		this.excerpt = excerpt;
		this.log = log;
		this.setDbTime(dbTime);
		this.setProcessingTime(processingTime);
	}
	
	public UrlInformation(){}
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getExcerpt() {
		return excerpt;
	}

	public void setExcerpt(String excerpt) {
		this.excerpt = excerpt;
	}

	public String getLog() {
		return log;
	}

	public void setLog(String log) {
		this.log = log;
	}

	public long getDbTime() {
		return dbTime;
	}

	public void setDbTime(long dbTime) {
		this.dbTime = dbTime;
	}

	public long getProcessingTime() {
		return processingTime;
	}

	public void setProcessingTime(long processingTime) {
		this.processingTime = processingTime;
	}

	

}
