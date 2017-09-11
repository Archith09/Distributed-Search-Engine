package edu.upenn.cis455.utilities;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class CountEntry {

	@PrimaryKey
	String word;
	
	Integer count;
	
	public CountEntry() {};
	
	public CountEntry(String entry){
		String[] temp = entry.split("\t");
		this.word = temp[0];
		this.count = Integer.parseInt(temp[1]);
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}
	
	
}
