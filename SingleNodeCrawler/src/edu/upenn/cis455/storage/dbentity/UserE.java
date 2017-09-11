package edu.upenn.cis455.storage.dbentity;

import java.util.HashSet;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;


/**
 * 
 * A class to create an entity of the user
 * 
 */

@Entity
public class UserE {

	/**
	 * @param args
	 */
	
	
/*	public UserE(String name, String password){
		this.name = name;
		this.password = password;		
	}*/
	
	public UserE(){
				
	}
	
	
	@PrimaryKey
	private String name;
	
	private String password;
	private long lastlogintime;
	private HashSet<String> channelsubscribed = new HashSet<String>();
	private HashSet<String> channelcreated = new HashSet<String>();
	
	
	
	
	public HashSet<String> getChannelsubscribed() {
		return channelsubscribed;
	}
	public void setChannelsubscribed(HashSet<String> channelsubscribed) {
		this.channelsubscribed = channelsubscribed;
	}
	
	public void putChannelsubscribed(String channelsubscribed) {
		this.channelsubscribed.add(channelsubscribed);
	}
	
	public void removesubscribed(String channel){
		this.channelsubscribed.remove(channel);
	}
	
	public HashSet<String> getChannelcreated() {
		return channelcreated;
	}
	public void setChannelcreated(HashSet<String> channelcreated) {
		this.channelcreated = channelcreated;
	}
	
	public void putChannelcreated(String channelcreated) {
		this.channelcreated.add(channelcreated);
	}
	
	public void removechannelcreated(String channelcreated){
		this.channelcreated.remove(channelcreated);
	}
	
	
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public long getLastlogintime() {
		return lastlogintime;
	}
	public void setLastlogintime(long lastlogintime) {
		this.lastlogintime = lastlogintime;
	}
	

}
