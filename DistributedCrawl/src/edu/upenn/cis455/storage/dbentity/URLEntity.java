package edu.upenn.cis455.storage.dbentity;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

/**
 * 
 * A class for URL as entities
 */

@Entity
public class URLEntity implements Delayed {
	
	@PrimaryKey
	private String URLPagename;	
	private String method;
	private String docCode;
	private ArrayList<String> extractedlinks;
	private long lastaccesstime;
	private boolean isHostSeed = false; 
	
	
	
	


	public boolean isHostSeed() {
		return isHostSeed;
	}


	public void setHostSeed(boolean isHostSeed) {
		this.isHostSeed = isHostSeed;
	}

	private long nextaccessetime;
	
	
		
	public ArrayList<String> getExtractedlinks() {
		return extractedlinks;
	}


	public void setExtractedlinks(ArrayList<String> extractedlinks) {
		this.extractedlinks = extractedlinks;
	}


	public URLEntity(){
		
	}
	
	
	public String getDocCode() {
		return docCode;
	}

	public void setDocCode(String docCode) {
		this.docCode = docCode;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public long getDelay(TimeUnit t){		
		return t.convert(nextaccessetime -System.currentTimeMillis(), TimeUnit.NANOSECONDS );		
	}
	
	public int compareTo(Delayed d){
		if(((URLEntity)d).nextaccessetime == this.nextaccessetime){
			return 0;
		}
		else if (((URLEntity)d).nextaccessetime < this.nextaccessetime){
			return 1;
		}
		else{
			return -1;
		}
		
	}
	
	public long getNextaccessetime() {
		return nextaccessetime;
	}

	public void setNextaccessetime(long nextaccessetime) {
		this.nextaccessetime = nextaccessetime;
	}

	public String getURLPagename() {
		return URLPagename;
	}

	public void setURLname(String uRLPagename) {
		URLPagename = uRLPagename;
	}

	public long getlastaccesstime() {
		return lastaccesstime;
	}

	public void setlastaccesstime(long uRLaccesstime) {
		lastaccesstime = uRLaccesstime;
	}
}
