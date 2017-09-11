package edu.upenn.cis455.storage.dbentity;

import java.util.ArrayList;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

import edu.upenn.cis455.xpathengine.RobotsTxtInfo;

/**
 * class to make domain host entity
 * 
 */

@Entity
public class HostEntity {
	
	@PrimaryKey
	String hostaddress;	
	private long delay;	
	private ArrayList<String> accessdenied = new ArrayList<String>();
	private RobotsTxtInfo robottxt;	
	private long count = 0;
	private boolean seed = false;
	
	
	
	public HostEntity(){
		
	}
	
	
	
	public long getCount() {
		return count;
	}



	public void setCount() {
		this.count = count+1;
	}



	public boolean isSeed() {
		return seed;
	}



	public void setSeed(boolean seed) {
		this.seed = seed;
	}



	public RobotsTxtInfo getRobottxt() {
		return robottxt;
	}


	public void setRobottxt(RobotsTxtInfo robottxt) {
		this.robottxt = robottxt;
	}


	public HostEntity(String address){
		this.hostaddress = address;
	}

	
	public ArrayList<String> getAccessdenied() {
		return accessdenied;
	}


	public void setAccessdenied(ArrayList<String> accessdenied) {
		this.accessdenied = accessdenied;
	}
	
	public String getHostaddress() {
		return hostaddress;
	}


	public void setHostaddress(String hostaddress) {
		this.hostaddress = hostaddress;
	}


	public long getDelay() {
		if(delay < 0){
			this.delay = 1;
			return delay;
		}
		return delay;
	}


	public void setDelay(long delay) {
		this.delay = delay;
	}



}
