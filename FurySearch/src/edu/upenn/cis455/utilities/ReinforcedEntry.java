package edu.upenn.cis455.utilities;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

@Entity
public class ReinforcedEntry {
	@PrimaryKey
	String url;
	
	Integer count;
	
	public ReinforcedEntry() {};
	
	public ReinforcedEntry(String entry){
		String[] temp = entry.split("\t");
		this.url = temp[0];
		this.count = Integer.parseInt(temp[1]);
	}
	
	public synchronized void incrementCount(){
		this.count++;
	}
	
	public synchronized void decrementCount(){
		this.count--;
	}
	
	public String getUrld() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}
	
}
