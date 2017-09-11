package edu.upenn.cis455.storage.dbentity;

import java.util.LinkedList;
import java.util.Queue;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

/**
 * 
 * class to make entity for backup
 */

@Entity
public class RestoreEntity {

	@PrimaryKey
	String queueName;
	
	Queue<String> backupQueue;
	
	final int max = 500;
	
	public RestoreEntity(){
		queueName = "backup";
		backupQueue = new LinkedList<String>();
	}

	/*
	 * Keep only the latest 500 values
	 */
	public void setQueue(String s){
		//if(backupQueue.size() >= max){
			//backupQueue.remove();
		//}
		backupQueue.add(s);
	}
	
	public void setCompleteQueue(Queue k){
		backupQueue.addAll(k);
	}
	
	
	/*
	 * get queue
	 */
	public Queue<String> getQueue(){
		return backupQueue;
	}
}
