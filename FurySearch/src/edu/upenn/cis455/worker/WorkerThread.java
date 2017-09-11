package edu.upenn.cis455.worker;

import java.util.ArrayList;

import edu.upenn.cis455.configuration.Constants;
import edu.upenn.cis455.storage.IndexDataAccessor;
import edu.upenn.cis455.utilities.IndexEntry;

/*
 * This is a worker thread implementation
 * The worker takes object from the process queue and performs various tasks on it like putting it 
 * into the database
 * 
 * 
 * 
 */
public class WorkerThread extends Thread{
	
	private String name;
	int count = 0;
	private ArrayList<IndexEntry> batch = new ArrayList<IndexEntry>();
	public WorkerThread(String name){
		this.name = name;
	}
	
	public void run(){
		
		while(true){
			String input;
			try {
				
				if(!Worker.getQueue().isEmpty()){
					
					input = Worker.getQueue().poll();
					if(input != null){
						Worker.increment();
						IndexEntry entry = new IndexEntry(input);
						addToBatch(entry);
						count++;
						System.out.println(Thread.currentThread().getName() + "From Main = Insert count: " + count+ " :::" + Worker.getCountBDB());
					}
					
				} else {
					
					input = WorkerServer.getRemoteQueue().take();
					Worker.increment();
					// System.out.println("Worker recieved input from queue: " + input);
					IndexEntry entry = new IndexEntry(input);
					addToBatch(entry);
					count++;
					System.out.println(Thread.currentThread().getName() + "From Remote = Insert count: " + count+ " :::" + Worker.getCountBDB());
				}
				
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	public String toString(){
		return this.name;
	}
	
	public synchronized void addToBatch(IndexEntry entry) throws InterruptedException{
		this.batch.add(entry);
		if(batch.size() == Constants.BATCH_SIZE){
			insert();
		}
		lastPush();
	}
	
	public synchronized void lastPush() throws InterruptedException{
		if(Worker.getQueue().isEmpty() && WorkerServer.getRemoteQueue().isEmpty() && Worker.isFileReadingComplete){
			insert();
		}
	}
	
	public synchronized void insert() throws InterruptedException{
		IndexDataAccessor.putAll(batch);
		batch.clear();
		if(!Worker.isFileReadingComplete){
			Thread.sleep(Constants.WRITE_SLEEP);
		}
	}
	
}
