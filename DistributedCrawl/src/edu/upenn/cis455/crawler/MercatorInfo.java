package edu.upenn.cis455.crawler;



/**
 * class to provide download info i.e. the number of pages down-loaded by the crawler
 */

public class MercatorInfo {

	
	//number of docs for which we got 200OK for GET for all formats either pdf, xml , html etc. 
	static long allformats = 0;
	static long xmldownloaded = 0;	
	
	//total number of html docs saved 
	static long total = 0;	
	
	
	//Total number of http client request sent for robot, head and get
	static long totalhits = 0;
	
	
	public synchronized static long getTotalhits() {
		return totalhits;
	}
	public synchronized static void setTotalhits() {
		MercatorInfo.totalhits = MercatorInfo.totalhits + 1;
	}
	public synchronized static long getTotal() {
		return total;
	}
	public synchronized static void setTotal() {
		MercatorInfo.total = MercatorInfo.total + 1;
	}
	public synchronized static long getallformats() {
		return allformats;
	}
	public synchronized static void setallformats() {
		MercatorInfo.allformats = MercatorInfo.allformats + 1;
	}
	public synchronized static long getXmldownloaded() {
		return xmldownloaded;
	}
	public synchronized static void setXmldownloaded() {
		MercatorInfo.xmldownloaded = MercatorInfo.xmldownloaded + 1;
	}	
}
